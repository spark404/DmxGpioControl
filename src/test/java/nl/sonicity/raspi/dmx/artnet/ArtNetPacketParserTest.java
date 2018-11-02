package nl.sonicity.raspi.dmx.artnet;

import org.junit.Test;

import static org.junit.Assert.*;
import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers.hasMessage;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;

public class ArtNetPacketParserTest {
    @Test
    public void testInvalidPacket() throws Exception {
        ArtNetPacketParser parser = new ArtNetPacketParser();
        catchException(parser).parse(new byte[] { 0x00 });

        assertThat(caughtException(), allOf(
                instanceOf(ArtNetException.class),
                hasMessage("Malformed packet")
        ));
    }

    @Test
    public void testUnknownPacketType() throws Exception{
        ArtNetPacketParser parser = new ArtNetPacketParser();
        catchException(parser).parse(new byte[] { 'A', 'r', 't', '-', 'N', 'e', 't', 0x0, 0x51, 0x51, 0x00, 0x14 });

        assertThat(caughtException(), allOf(
                instanceOf(ArtNetException.class),
                hasMessage("Unknown packet type received")
        ));
    }
}