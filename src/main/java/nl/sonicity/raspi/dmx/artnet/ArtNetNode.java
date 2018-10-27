package nl.sonicity.raspi.dmx.artnet;

import lombok.extern.slf4j.Slf4j;
import nl.sonicity.raspi.dmx.artnet.packets.ArtDmx;
import nl.sonicity.raspi.dmx.artnet.packets.ArtNetPacket;
import nl.sonicity.raspi.dmx.artnet.packets.ArtPollReply;

import java.io.IOException;
import java.net.DatagramSocket;
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
import java.util.Collections;
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

    public ArtNetNode(ArtNetNodeConfig config) {
        this.artNetNodeConfig = config;

        try {
            configureNetworkFromInterfaceName(artNetNodeConfig.getNetworkInterface());
        } catch (ArtNetException e) {
            log.error("Failed to start ArtNetNode", e);
        }
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

    private void handler() throws Exception {
        DatagramChannel server = null;
        server = DatagramChannel.open();
        InetSocketAddress sAddr = new InetSocketAddress("0.0.0.0", DMX_PORT);
        server.bind(sAddr);

        // According to the spec, start off with ArtPollReply broadcast
        sendArtPollReply(server);

        ByteBuffer buffer = ByteBuffer.allocate(1024);
        while (!terminate) {

            SocketAddress source = server.receive(buffer);
            ArtNetPacket artNetPacket = ArtNetPacketParser.parse(buffer.array());

            if (artNetPacket == null) {
                log.warn("Received something, but i don't recognize it");
                continue;
            }

            if (artNetPacket.getOpCode() == ArtNetOpCodes.OpPoll) {
                log.info("Poll received from {}", source.toString());
                sendArtPollReply(server);
            }

            if (artNetPacket.getOpCode() == ArtNetOpCodes.OpPollReply) {
                ArtPollReply artPollReply = (ArtPollReply) artNetPacket;
                handleArtPollReply(artPollReply);
            }

            if (artNetPacket.getOpCode() == ArtNetOpCodes.OpDmx) {
                ArtDmx dmxPacket = (ArtDmx) artNetPacket;
                log.trace("DMX data received for {}:{}:{}, {} bytes",
                        dmxPacket.getNetwork(), dmxPacket.getSubnet(), dmxPacket.getUniverse(),
                        dmxPacket.getDmxLength());
                if (dmxPacket.getNetwork() == artNetNodeConfig.getNetwork() &&
                        dmxPacket.getSubnet() == artNetNodeConfig.getSubnet()) {
                    handleDmxData(dmxPacket);
                }
            }

            buffer.clear();
        }

        if (server.isOpen()) {
            server.close();
        }
    }

    private void sendArtPollReply(DatagramChannel server) throws ArtNetException, IOException {
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
        ArtPollReply artPollReply = (ArtPollReply) ArtNetPacketParser.generatePacketByOpCode(ArtNetOpCodes.OpPollReply);
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
