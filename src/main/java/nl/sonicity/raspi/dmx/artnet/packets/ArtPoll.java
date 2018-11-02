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

import nl.sonicity.raspi.dmx.artnet.ArtNetException;
import nl.sonicity.raspi.dmx.artnet.ArtNetOpCodes;

public class ArtPoll extends ArtNetPacket {
    private int talkToMe;
    private int priority;

    public ArtPoll() {
        super(ArtNetOpCodes.ARTNET_OP_POLL);
    }

    @Override
    public ArtNetPacket parse(byte[] data) throws ArtNetException {
        isValid(data);

        talkToMe = data[12];
        priority = data[13];

        setData(data);
        return this;
    }

    private void isValid(byte[] packet) throws ArtNetException {
        validate(packet);

        if (packet.length != 14) {
            throw new ArtNetException("Packet length invalid");
        }

        if (packet[8] != 0x00 || packet[9] != 0x20) {
            throw new ArtNetException("Wrong opcode");
        }
    }

    public int getTalkToMe() {
        return talkToMe;
    }

    public void setTalkToMe(int talkToMe) {
        this.talkToMe = talkToMe;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
