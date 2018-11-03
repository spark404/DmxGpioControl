/**
 * Copyright © 2018 Sonicity (info@sonicity.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.sonicity.raspi.dmx.artnet;

import lombok.extern.slf4j.Slf4j;
import nl.sonicity.raspi.dmx.artnet.packets.ArtDmx;
import nl.sonicity.raspi.dmx.artnet.packets.ArtNetPacket;
import nl.sonicity.raspi.dmx.artnet.packets.ArtPoll;
import nl.sonicity.raspi.dmx.artnet.packets.ArtPollReply;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ArtNetNode implements ArtNetNodeMBean {
    private static final int DMX_PORT = 6454;

    private List<DmxHandler> handlers = new ArrayList<>();
    private Thread handlerThread;
    private Map<String, ArtNetNodeInfo> discoveredNodes = new ConcurrentHashMap<>();

    private volatile boolean terminate = false;

    private ArtNetNodeConfig artNetNodeConfig;

    private NetworkInterface networkInterface;
    private InterfaceAddress interfaceAddress;

    public ArtNetNode(ArtNetNodeConfig config) {
        this.artNetNodeConfig = config;

        try {
            configureNetworkFromInterfaceName(artNetNodeConfig.getNetworkInterface());
        } catch (ArtNetException e) {
            log.error("Failed to start ArtNetNode", e);
        }

    }

    public void start() {
        if (handlerThread != null && handlerThread.isAlive())  {
            throw new ArtNetException("Node already started");
        }
        log.info("Configuring ArtNetNode with interface:{}, address:{}, network:{}, subnet:{}",
                networkInterface.getDisplayName(), interfaceAddress.getAddress().getHostAddress(),
                artNetNodeConfig.getNetwork(), artNetNodeConfig.getSubnet());
        log.info("Starting ArtNetNode on {}", interfaceAddress.toString());
        Runnable artNetRunner = () -> {
            try {
                handler();
            } catch (Exception e) {
                log.error("Exception in handler function", e);
            }
        };
        handlerThread = new Thread(artNetRunner);
        handlerThread.setName("ArtNetHandler-" + interfaceAddress.toString());
        handlerThread.setDaemon(true);
        handlerThread.start();
        log.info("ArtNetNode on " + interfaceAddress.toString() + " started");
    }

    public void stop() {
        if (handlerThread != null && handlerThread.isAlive()) {
            log.info("Stopping ArtNetNode on {}", interfaceAddress.toString());
            terminate = true;

            try {
                handlerThread.join(5000L);
            } catch (InterruptedException e) {
                log.error("Thread was interrupted while waiting for it to stop", e);
                Thread.currentThread().interrupt();
            }

            log.info("Sending shutdown signal to handlers");
            handlers.forEach(DmxHandler::shutdown);

            handlerThread = null;
            terminate  = false;
            log.info("ArtNetNode on {} stopped", interfaceAddress.toString());
        }
    }

    public Collection<DmxHandler> getHandlers() {
        return handlers;
    }

    public Collection<ArtNetNodeInfo> getDiscoveredNodes() {
        return discoveredNodes.values();
    }

    private void handler() throws IOException {
        try (DatagramChannel server = DatagramChannel.open()) {
            InetSocketAddress sAddr = new InetSocketAddress("0.0.0.0", DMX_PORT);
            server.bind(sAddr);
            server.configureBlocking(false);

            // According to the spec, start off with ArtPollReply broadcast
            sendArtPollReply();

            ByteBuffer buffer = ByteBuffer.allocate(8196);
            var lastDmxSeen = System.currentTimeMillis();
            var timeout = false;

            while (!terminate) {
                long loopStart = System.currentTimeMillis();

                SocketAddress source = server.receive(buffer);
                if (source != null) {
                    ArtNetPacket artNetPacket;
                    try {
                        artNetPacket = ArtNetPacket.parseBytes(Arrays.copyOf(buffer.array(), buffer.position()));
                    } catch (ArtNetException e) {
                        log.warn("Invalid packet received from {}: {}", source.toString(), e.getMessage());
                        continue;
                    } finally {
                        buffer.clear();
                    }

                    if (artNetPacket == null) {
                        log.warn("Received something, but i don't recognize it");
                        continue;
                    }

                    if (artNetPacket instanceof ArtPoll) {
                        log.info("Poll received from {}", source.toString());
                        sendArtPollReply();
                    }

                    if (artNetPacket instanceof ArtPollReply) {
                        ArtPollReply artPollReply = (ArtPollReply) artNetPacket;
                        handleArtPollReply(artPollReply);
                    }

                    if (artNetPacket instanceof ArtDmx) {
                        ArtDmx dmxPacket = (ArtDmx) artNetPacket;
                        log.trace("DMX data received for {}:{}:{}, {} bytes", dmxPacket.getNetwork(), dmxPacket.getSubnet(), dmxPacket.getUniverse(), dmxPacket.getDmxLength());
                        if (dmxPacket.getNetwork() == artNetNodeConfig.getNetwork() && dmxPacket.getSubnet() == artNetNodeConfig.getSubnet()) {
                            lastDmxSeen = System.currentTimeMillis();
                            timeout = false;
                            handleDmxData(dmxPacket);
                        }
                    }
                }

                if (lastDmxSeen + 10000 < System.currentTimeMillis() && !timeout) {
                    // No DMX data for 10 seconds
                    log.warn("No DMX data received for 10 seconds");
                    timeout = true;
                    handlers.forEach(DmxHandler::timeout);
                }

                long loopEnd = System.currentTimeMillis();
                if (loopEnd + 500 < loopStart) {
                    try {
                        Thread.sleep(loopEnd + 500 - loopStart);
                    } catch (InterruptedException e) {
                        log.error("End-of-loop sleep interrupted");
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }

    private void sendArtPollReply() throws IOException {
        try (DatagramChannel replyChannel = DatagramChannel.open()) {
            replyChannel.socket().setBroadcast(true);
            ArtPollReply artPollReply = generateArtPollReply();


            // Send on local network broadcast
            ByteBuffer reply = ByteBuffer.wrap(artPollReply.toBytes());
            replyChannel.send(reply, new InetSocketAddress(interfaceAddress.getBroadcast(), DMX_PORT));

            // Send on wire broadcast
            reply = ByteBuffer.wrap(artPollReply.toBytes());
            replyChannel.send(reply, new InetSocketAddress(InetAddress.getByName("255.255.255.255"), DMX_PORT));
        }
    }

    private void handleArtPollReply(ArtPollReply artPollReply) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS", Locale.getDefault());
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getDefault());
        calendar.setTimeInMillis(System.currentTimeMillis());
        String lastSeen = sdf.format(calendar.getTime());

        if (discoveredNodes.containsKey(artPollReply.getShortName())) {
            discoveredNodes.get(artPollReply.getShortName()).setLastSeen(lastSeen);
        } else {
            log.info("First poll reply seen from \"{}\"", artPollReply.getShortName());
            discoveredNodes.put(artPollReply.getShortName(), new ArtNetNodeInfo(artPollReply.getShortName(), lastSeen));
        }
        new ArtNetNodeInfo(artPollReply.getShortName(), lastSeen);
    }

    private ArtPollReply generateArtPollReply() throws SocketException {
        return new ArtPollReply.Builder()
                .firmwareVersion(0)
                .shortName("ArtNetNode")
                .longName("ArnNetNode")
                .ipAddress((Inet4Address) interfaceAddress.getAddress())
                .netswitch(artNetNodeConfig.getNetwork())
                .subswitch(artNetNodeConfig.getSubnet())
                .macAddress(networkInterface.getHardwareAddress())
                .port(0, false, true, 0)
                .swIn(0, artNetNodeConfig.getUniverse())
                .build();
    }

    private void handleDmxData(ArtDmx dmxPacket) {
        for (DmxHandler handlerEntry : handlers) {
            if (handlerEntry.getUniverse() != dmxPacket.getUniverse()) {
                return;
            }

            int startPosition = handlerEntry.getAddress();
            int length = handlerEntry.getWidth();

            // DMX addresses are from 1 to 512, offset by -1 for array indices
            int from = startPosition - 1;
            byte[] dataPart = Arrays.copyOfRange(dmxPacket.getDmxData(), from, from + length);
            handlerEntry.onDmx(dataPart);
        }
    }

    private void configureNetworkFromInterfaceName(String network) {
        try {
            NetworkInterface artNetInterface = NetworkInterface.getByName(network);
            InterfaceAddress artNetInterfaceAddress = null;
            for (InterfaceAddress address : artNetInterface.getInterfaceAddresses()) {
                if (address.getAddress() instanceof Inet4Address) {
                    artNetInterfaceAddress = address;
                    break;
                }
            }

            this.networkInterface = artNetInterface;
            this.interfaceAddress = artNetInterfaceAddress;
        } catch (SocketException e) {
            throw new ArtNetException("Unable to determine the interface");
        }

    }
}
