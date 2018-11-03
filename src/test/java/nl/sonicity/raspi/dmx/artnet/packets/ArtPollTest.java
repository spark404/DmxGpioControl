/**
 * Copyright © 2018 Sonicity (info@sonicity.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.sonicity.raspi.dmx.artnet.packets;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;

public class ArtPollTest {
    @Test
    public void testBuilder() {
        ArtPoll poll = new ArtPoll.Builder()
                .talkToMe(5)
                .priority(7)
                .build();

        byte[] packet = poll.toBytes();

        byte[] id = Arrays.copyOfRange(packet, 0, 8);
        assertThat(id, equalTo(ArtNetPacket.ARTNET_ID));

        int opCodeValue = ArtNetPacket.readUint16Msb(packet, 8);
        assertThat(opCodeValue, equalTo(0x2000));

        int version = ArtNetPacket.readUint16Lsb(packet, 10);
        assertThat(version, equalTo(14));

        int talkToMe = ArtNetPacket.readUint8(packet, 12);
        assertThat(talkToMe, equalTo(5));

        int priority = ArtNetPacket.readUint8(packet, 13);
        assertThat(priority, equalTo(7));
    }

    @Test
    public void testParser() {
        byte[] data = new byte[] {
            'A', 'r', 't', '-', 'N', 'e', 't', 0x00, 0x00, 0x20, 0x00, 0x0e, 0x05, 0x07
        };

        ArtNetPacket packet = ArtNetPacket.parseBytes(data);

        assertThat(packet, instanceOf(ArtPoll.class));

        ArtPoll artPoll = (ArtPoll)packet;
        assertThat(artPoll.getTalkToMe(), equalTo(5));
        assertThat(artPoll.getPriority(), equalTo(7));
    }
}