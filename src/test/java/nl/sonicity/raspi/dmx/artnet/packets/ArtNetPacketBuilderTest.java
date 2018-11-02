package nl.sonicity.raspi.dmx.artnet.packets;

import org.junit.Test;

import static org.junit.Assert.*;

public class ArtNetPacketBuilderTest {
    @Test
    public void buildArtPollPacket() {
        ArtNetPacketBuilder.ArtPollBuilder builder = ArtNetPacketBuilder.getBuilderInstanceForOpCode(ARTNET_OP_POLL)
    }
}