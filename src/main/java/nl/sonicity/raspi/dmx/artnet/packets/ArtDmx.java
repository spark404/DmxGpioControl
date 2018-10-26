package nl.sonicity.raspi.dmx.artnet.packets;

import nl.sonicity.raspi.dmx.artnet.ArtNetOpCodes;

import java.util.Arrays;

public class ArtDmx extends ArtNetPacket {

    public static final int DMX_ADDRESS = 14;
    public static final int DMX_LENGTH_OFFSET = 16;
    public static final int DMX_DATA_OFFSET = 18;
    private int network;
    private int subnet;
    private int universe;

    public ArtDmx() {
        super(ArtNetOpCodes.OpDmx);
    }

    @Override
    public ArtNetPacket parse(byte[] data) {
        setData(data);
        network = data[DMX_ADDRESS+1];
        subnet = data[DMX_ADDRESS] >> 3;
        universe = data[DMX_ADDRESS] & 0x7;
        return this;
    }

    public byte[] getDmxData() {
        byte[] data = getData();
        int dmxLength = readIntLsb(data, DMX_LENGTH_OFFSET);
        return Arrays.copyOfRange(data, DMX_DATA_OFFSET, DMX_DATA_OFFSET + dmxLength);
    }

    public int getDmxLength() {
        byte[] data = getData();
        return readIntLsb(data, DMX_LENGTH_OFFSET);
    }

    public int getNetwork() {
        return network;
    }

    public int getSubnet() {
        return subnet;
    }

    public int getUniverse() {
        return universe;
    }
}
