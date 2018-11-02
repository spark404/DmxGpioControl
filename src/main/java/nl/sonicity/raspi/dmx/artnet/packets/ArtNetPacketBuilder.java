package nl.sonicity.raspi.dmx.artnet.packets;

import nl.sonicity.raspi.dmx.artnet.ArtNetOpCodes;

import java.util.Arrays;

public class ArtNetPacketBuilder {
    private static final byte[] ARTNET_ID = { 'A', 'r', 't', '-', 'N', 'e', 't', 0x0};
    private static final byte[] ARTNET_VERSION = { 0x00, 0x0E };

    private ArtNetPacketBuilder() {
    }

    private byte[] createArtNetPacket(int opCode, int size) {
        // Create the default structure with the tag, opcode and version
        byte[] data = new byte[size];
        System.arraycopy(ARTNET_ID, 0, data, 0, 8);

        data[8] = (byte)(opCode & 0xff);
        data[9] = (byte)((opCode >> 8) & 0xff);

        data[10] = ARTNET_VERSION[0];
        data[11] = ARTNET_VERSION[1];

    }

    public static <T> T getBuilderInstanceForOpCode(ArtNetOpCodes artNetOpCode) {

    }


    private class ArtPollBuilder {
        private byte[] packet;

        ArtPollBuilder newInstance() {
            ArtPollBuilder builder = new ArtPollBuilder();
            packet = createArtNetPacket(ArtNetOpCodes.ARTNET_OP_POLL.getOpCode(), 14);
        }

        byte[] build() {
            return Arrays.copyOf(packet, packet.length);
        }

    }
}
