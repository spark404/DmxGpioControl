package nl.sonicity.raspi.dmx.artnet.packets;

import nl.sonicity.raspi.dmx.artnet.ArtNetOpCodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ArtNetPacket {
    private final static Logger logger = LoggerFactory.getLogger(ArtNetPacket.class);

    public static final byte[] ARTNET_ID = { 'A', 'r', 't', '-', 'N', 'e', 't', 0x0};
    public static final int ARTNET_VERSION = 14;
    public static final int OEM = 0x4242;

    private final ArtNetOpCodes opCode;
    private byte[] data;
    private int length;

    public ArtNetPacket(ArtNetOpCodes opCode) {
        this.opCode = opCode;
    }

    public abstract ArtNetPacket parse(byte [] data);

    public ArtNetOpCodes getOpCode() {
        return opCode;
    }

    protected void setHeader() {
        System.arraycopy(ARTNET_ID, 0, data, 0, 8);
        data[8] = (byte)(opCode.getOpCode() & 0xff);
        data[9] = (byte)((opCode.getOpCode() >> 8) & 0xff);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int readIntMsb(byte[] data, int startPos) {
        return data[startPos] + (data[startPos + 1] << 8);
    }

    public int readIntLsb(byte[] data, int startPos) {
        return (data[startPos] << 8) + data[startPos + 1];
    }

    public void writeIntMsb(byte[] data, int startPos, int value) {
        data[startPos] = (byte)(value & 0xff);
        data[startPos + 1] = (byte)((value >> 8) & 0xff);
    }

    public void writeIntLsb(byte[] data, int startPos, int value) {
        data[startPos] = (byte)((value >> 8) & 0xff);
        data[startPos + 1] = (byte)(value & 0xff);
    }
}
