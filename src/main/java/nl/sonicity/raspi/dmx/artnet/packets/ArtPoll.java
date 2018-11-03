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
import nl.sonicity.raspi.dmx.artnet.ArtNetOpCode;

import java.util.Arrays;

public class ArtPoll extends ArtNetPacket {

    private static final int OFFSET_TALKTOME = 12;
    private static final int OFFSET_PRIORITY = 13;

    private ArtPoll(byte[] packet) {
        this.packet = Arrays.copyOf(packet, packet.length);
    }

    public int getTalkToMe() {
        return readUint8(packet, OFFSET_TALKTOME);
    }

    public int getPriority() {
        return readUint8(packet, OFFSET_PRIORITY);
    }

    public static ArtPoll fromBytes(byte[] data) {
        if (data.length != 14) {
            throw new ArtNetException("Packet length invalid");
        }

        if (data[8] != 0x00 || data[9] != 0x20) {
            throw new ArtNetException("Wrong opcode");
        }

        int protocolVersion = (data[10] << 8) + (data[11] & 0xff);
        if (protocolVersion < 14) {
            throw new ArtNetException("ArtNet protocol version not compatible");
        }

        return new ArtPoll(data);
    }

    public static class Builder extends ArtNetPacket.Builder<ArtPoll, Builder> {
        public Builder() {
            super(ArtNetOpCode.ARTNET_OP_POLL.getOpCode(), 14);
        }

        Builder talkToMe(int talkToMe) {
            if (talkToMe < 0 || talkToMe > 255) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            data[OFFSET_TALKTOME] = (byte)(talkToMe & 0xFF);

            return this;
        }

        Builder priority(int priority) {
            if (priority < 0 || priority > 255) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            data[OFFSET_PRIORITY] = (byte)(priority & 0xFF);

            return this;
        }

        @Override
        public ArtPoll build() {
            return new ArtPoll(data);
        }
    }
}
