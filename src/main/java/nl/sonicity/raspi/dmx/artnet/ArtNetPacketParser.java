package nl.sonicity.raspi.dmx.artnet;


import nl.sonicity.raspi.dmx.artnet.packets.ArtDmx;
import nl.sonicity.raspi.dmx.artnet.packets.ArtNetPacket;
import nl.sonicity.raspi.dmx.artnet.packets.ArtPoll;
import nl.sonicity.raspi.dmx.artnet.packets.ArtPollReply;

import java.util.Arrays;

public class ArtNetPacketParser {
    private ArtNetPacketParser() {}

    public static ArtNetPacket generatePacketByOpCode(ArtNetOpCodes opCode) throws ArtNetException {
        ArtNetPacket artNetPacket;
        try {
            artNetPacket = getTypeForOpCode(opCode).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ArtNetException(e);
        }

        return artNetPacket;
    }

    public static ArtNetPacket parse(byte[] data) throws ArtNetException{
        final byte[] packetData = data.clone();

        byte[] id = Arrays.copyOfRange(packetData, 0, 8);
        if (!Arrays.equals(ArtNetPacket.ARTNET_ID, id)) {
            throw new ArtNetException("Invalid magic string, expected " + new String(ArtNetPacket.ARTNET_ID));
        }

        int opCodeValue = (packetData[9] << 8) + packetData[8];
        ArtNetOpCodes opCode = ArtNetOpCodes.fromInt(opCodeValue);

        ArtNetPacket artNetPacket;
        try {
            artNetPacket = getTypeForOpCode(opCode).newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ArtNetException(e);
        }

        artNetPacket.setLength(data.length);
        return artNetPacket.parse(data);
    }

    public static Class<? extends ArtNetPacket> getTypeForOpCode(ArtNetOpCodes opCode) {
        Class clazz;
        switch (opCode) {
            case OpPoll:
                clazz = ArtPoll.class;
                break;
            case OpPollReply:
                clazz = ArtPollReply.class;
                break;
            case OpDmx:
                clazz = ArtDmx.class;
                break;
            default:
                clazz = null;
        }
        return clazz;
    }
}
