package nl.sonicity.raspi.dmx.artnet.packets;


import nl.sonicity.raspi.dmx.artnet.ArtNetOpCodes;

import java.nio.charset.Charset;

public class ArtPollReply extends ArtNetPacket {

    private static final int OFFSET_IPADDRESS = 10;
    private static final int OFFSET_UDPPORT = 14;
    private static final int OFFSET_VERSION = 16;
    private static final int OFFSET_NETSWITCH = 18;
    private static final int OFFSET_SUBNETSWITCH = 19;
    private static final int OFFSET_OEM = 20;
    private static final int OFFSET_UBEA_VERSION = 22;
    private static final int OFFSET_STATUS1 = 23;
    private static final int OFFSET_ESTAMAN = 24;
    private static final int OFFSET_SHORTNAME = 26;
    private static final int OFFSET_LONGNAME = 44;
    private static final int OFFSET_NODEREPORT = 108;
    private static final int OFFSET_PORTS = 172;
    private static final int OFFSET_PORTTYPE = 174;
    private static final int OFFSET_GOODINPUT = 178;
    private static final int OFFSET_GOODOUTPUT = 182;
    private static final int OFFSET_SWIN = 186;
    private static final int OFFSET_SWOUT = 190;
    private static final int OFFSET_SWVIDEO = 194;
    private static final int OFFSET_SWMACRO = 185;
    private static final int OFFSET_SWREMOTE = 196;
    private static final int OFFSET_STYLE = 200;
    private static final int OFFSET_MACADDR = 201;
    private static final int OFFSET_STATUS2 = 212;

    public ArtPollReply() {
        super(ArtNetOpCodes.OpPollReply);

        byte[] packetData = new byte[238];
        setData(packetData);
        setLength(238);

        // Fill fixed values
        setHeader();
        writeIntMsb(packetData, OFFSET_UDPPORT, 0x1936); // fixed port
        writeIntLsb(packetData, OFFSET_VERSION, ArtNetPacket.ARTNET_VERSION);
        writeIntLsb(packetData, OFFSET_OEM, ArtNetPacket.OEM);
        packetData[OFFSET_UBEA_VERSION] = 0;
        packetData[OFFSET_STATUS1] = (byte)0xf0;
        writeIntMsb(packetData, OFFSET_ESTAMAN, ArtNetPacket.OEM);

        // Shortname 18 chars
        System.arraycopy("DmxGpioControl\0".getBytes(), 0, packetData, OFFSET_SHORTNAME, 15);

        // Longname 64 chars
        String longname = "DmxGpioControl@" +
                System.getenv("HOSTNAME") +
                ".local" +
                "\0";
        System.arraycopy(longname.getBytes(), 0, packetData, OFFSET_LONGNAME, longname.length());

        // one port, DMX 512
        writeIntLsb(getData(), OFFSET_PORTS, 1);
        packetData[OFFSET_PORTTYPE] = (byte)0x80;
        packetData[OFFSET_PORTTYPE + 1] = (byte)0x00;
        packetData[OFFSET_PORTTYPE + 2] = (byte)0x00;
        packetData[OFFSET_PORTTYPE + 3] = (byte)0x00;

        packetData[OFFSET_GOODINPUT] = (byte)0x80;
        packetData[OFFSET_GOODOUTPUT] = (byte)0x00;

        packetData[OFFSET_SWIN] = (byte)0x00;
        packetData[OFFSET_SWOUT] = (byte)0x00;
        packetData[OFFSET_SWVIDEO] = (byte)0x00;
        packetData[OFFSET_SWMACRO] = (byte)0x00;
        packetData[OFFSET_SWREMOTE] = (byte)0x00;
        packetData[OFFSET_STYLE] = (byte)0x00;// stNode

        packetData[OFFSET_STATUS2] = (byte)0x08;
    }

    @Override
    public ArtNetPacket parse(byte[] data) {
        setData(data);
        return this;
    }

    public ArtPollReply setIpAddress(byte[] ipAddress) {
        System.arraycopy(ipAddress, 0, getData(), OFFSET_IPADDRESS, 4);
        return this;
    }

    public ArtPollReply setNetSwitch(int net, int subnet) {
        byte[] packetData = getData();
        packetData[OFFSET_NETSWITCH] = (byte)(net & 0x7f); // Net 0 - 127
        packetData[OFFSET_SUBNETSWITCH] = (byte)(subnet & 0xf); // Subnet 0 - 15
        return this;
    }

    public ArtPollReply setMacAddress(byte[] hwaddr) {
        System.arraycopy(hwaddr, 0, getData(), OFFSET_MACADDR, 6);
        return this;
    }

    public ArtPollReply setUniverseForInputPort(int port, int universe) {
        byte[] packetData = getData();
        packetData[OFFSET_SWIN + port - 1] = (byte)(universe & 0x3); // Universe 0 - 7
        return this;
    }

    public ArtPollReply setUniverseForOutputPort(int port, int universe) {
        byte[] packetData = getData();
        packetData[OFFSET_SWOUT + port - 1] = (byte)(universe & 0x3); // Universe 0 - 7
        return this;
    }

    public String getShortName() {
        byte[] shortName = new byte[18];
        System.arraycopy(getData(), OFFSET_SHORTNAME, shortName, 0, 18);
        int i;
        for (i = 0; i < 18 && shortName[i] != 0x0; i++) { }

        return new String(shortName, 0, i, Charset.forName("ASCII"));
    }

}
