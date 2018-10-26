package nl.sonicity.raspi.dmx.artnet.util;

public class Utils {
    final public static byte[] OOM_CODE = { 0x42, 0x42 };

    public static byte[] calculateArtNetAddress(byte[] macAddress, byte[] oemCode, boolean networkSwitch) {
        byte[] rawAddress = new byte[4];
        if (!networkSwitch) {
            rawAddress[0] = 2;
        } else {
            rawAddress[0] = 10;
        }
        // force to bytes
        byte b = (byte)((byte)(macAddress[3] + oemCode[0]) + oemCode[1]);
        byte c = macAddress[4];
        byte d = macAddress[5];

        rawAddress[1] = b;
        rawAddress[2] = c;
        rawAddress[3] = d;

        return rawAddress;
    }
}
