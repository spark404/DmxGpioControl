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
import nl.sonicity.raspi.dmx.artnet.packets.ArtPollReply;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
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

    private ArtNetPacketParser artNetPacketParser;

    public ArtNetNode(ArtNetNodeConfig config) {
        this.artNetNodeConfig = config;

        try {
            configureNetworkFromInterfaceName(artNetNodeConfig.getNetworkInterface());
        } catch (ArtNetException e) {
            log.error("Failed to start ArtNetNode", e);
        }

        artNetPacketParser = new ArtNetPacketParser();
    }

    public void start() throws ArtNetException {
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

    private void handler() throws ArtNetException, IOException {
        try (DatagramChannel server = DatagramChannel.open()) {
            InetSocketAddress sAddr = new InetSocketAddress("0.0.0.0", DMX_PORT);
            server.bind(sAddr);

            // According to the spec, start off with ArtPollReply broadcast
            sendArtPollReply();

            ByteBuffer buffer = ByteBuffer.allocate(1024);
            while (!terminate) {

                SocketAddress source = server.receive(buffer);
                ArtNetPacket artNetPacket = artNetPacketParser.parse(buffer.array());

                if (artNetPacket == null) {
                    log.warn("Received something, but i don't recognize it");
                    continue;
                }

                if (artNetPacket.getOpCode() == ArtNetOpCodes.ARTNET_OP_POLL) {
                    log.info("Poll received from {}", source.toString());
                    sendArtPollReply();
                }

                if (artNetPacket.getOpCode() == ArtNetOpCodes.ARNET_OP_POLLREPLY) {
                    ArtPollReply artPollReply = (ArtPollReply)artNetPacket;
                    handleArtPollReply(artPollReply);
                }

                if (artNetPacket.getOpCode() == ArtNetOpCodes.ARTNET_OP_DMX) {
                    ArtDmx dmxPacket = (ArtDmx)artNetPacket;
                    log.trace("DMX data received for {}:{}:{}, {} bytes", dmxPacket.getNetwork(), dmxPacket.getSubnet(), dmxPacket.getUniverse(), dmxPacket.getDmxLength());
                    if (dmxPacket.getNetwork() == artNetNodeConfig.getNetwork() && dmxPacket.getSubnet() == artNetNodeConfig.getSubnet()) {
                        handleDmxData(dmxPacket);
                    }
                }

                buffer.clear();
            }
        }
    }

    private void sendArtPollReply() throws ArtNetException, IOException {
        try (DatagramChannel replyChannel = DatagramChannel.open()) {
            replyChannel.socket().setBroadcast(true);
            ArtPollReply artPollReply = generateArtPollReply();


            // Send on local network broadcast
            ByteBuffer reply = ByteBuffer.wrap(artPollReply.getData());
            replyChannel.send(reply, new InetSocketAddress(interfaceAddress.getBroadcast(), DMX_PORT));
            // Send on wire broadcast
            reply = ByteBuffer.wrap(artPollReply.getData());
            replyChannel.send(reply, new InetSocketAddress(InetAddress.getByName("255.255.255.255"), DMX_PORT));
        }
    }

    private void handleArtPollReply(ArtPollReply artPollReply) {
        log.info("Poll reply seen from {}", artPollReply.getShortName());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss,SSS", Locale.getDefault());
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getDefault());
        calendar.setTimeInMillis(System.currentTimeMillis());
        String lastSeen = sdf.format(calendar.getTime());

        if (discoveredNodes.containsKey(artPollReply.getShortName())) {
            discoveredNodes.get(artPollReply.getShortName()).setLastSeen(lastSeen);
        } else {
            discoveredNodes.put(artPollReply.getShortName(), new ArtNetNodeInfo(artPollReply.getShortName(), lastSeen));
        }
        new ArtNetNodeInfo(artPollReply.getShortName(), lastSeen);
    }

    private ArtPollReply generateArtPollReply() throws ArtNetException, SocketException {
        ArtPollReply artPollReply = (ArtPollReply) artNetPacketParser.generatePacketByOpCode(ArtNetOpCodes.ARNET_OP_POLLREPLY);
        artPollReply
                .setNetSwitch(artNetNodeConfig.getNetwork(), artNetNodeConfig.getSubnet())
                .setIpAddress(interfaceAddress.getAddress().getAddress())
                .setMacAddress(networkInterface.getHardwareAddress())
                .setUniverseForInputPort(1, artNetNodeConfig.getUniverse());
        return artPollReply;
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

    private void configureNetworkFromInterfaceName(String network) throws ArtNetException {
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
