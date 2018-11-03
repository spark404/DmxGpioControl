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
package nl.sonicity.raspi.dmx.artnet;

import nl.sonicity.raspi.dmx.artnet.packets.ArtDmx;
import nl.sonicity.raspi.dmx.artnet.packets.ArtPoll;
import nl.sonicity.raspi.dmx.artnet.packets.ArtPollReply;

import java.util.HashMap;
import java.util.Map;

public enum ArtNetOpCode {
    ARTNET_OP_POLL(0x2000, ArtPoll.class),
    ARTNET_OP_POLLREPLY(0x2100, ArtPollReply.class),
    ARTNET_OP_DMX(0x5000, ArtDmx.class),
    ARNET_OP_NZS(0x5100, ArtDmx.class);

    private static final Map<Integer, ArtNetOpCode> intToType = new HashMap<>();

    static {
        for (ArtNetOpCode artNetOpCode : ArtNetOpCode.values()) {
            intToType.put(artNetOpCode.getOpCode(), artNetOpCode);
        }
    }

    private final int opCode;
    private final Class packetClass;

    ArtNetOpCode(int opCode, Class packetClass) {
        this.opCode = opCode;
        this.packetClass = packetClass;
    }

    public int getOpCode() {
        return opCode;
    }

    public Class getPacketClass() {
        return packetClass;
    }

    public static ArtNetOpCode fromInt(Integer integer) {
        return intToType.get(integer);
    }
}
