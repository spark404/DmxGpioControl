package nl.sonicity.raspi.dmx.artnet.packets;

import nl.sonicity.raspi.dmx.artnet.ArtNetOpCodes;

public class ArtPoll extends ArtNetPacket {

    public ArtPoll() {
        super(ArtNetOpCodes.OpPoll);
    }

    @Override
    public ArtNetPacket parse(byte[] data) {
        setData(data);
        return this;
    }
}
