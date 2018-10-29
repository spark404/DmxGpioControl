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

import java.util.HashMap;
import java.util.Map;

public enum ArtNetOpCodes {
    ARTNET_OP_POLL(0x2000),
    ARNET_OP_POLLREPLY(0x2100),
    ARTNET_OP_DMX(0x5000),
    ARNET_OP_NZS(0x5100);

    private static final Map<Integer, ArtNetOpCodes> intToType = new HashMap<>();

    static {
        for (ArtNetOpCodes artNetOpCodes : ArtNetOpCodes.values()) {
            intToType.put(artNetOpCodes.getOpCode(), artNetOpCodes);
        }
    }

    private final int opCode;

    ArtNetOpCodes(int opCode) {
        this.opCode = opCode;
    }

    public int getOpCode() {
        return opCode;
    }

    public static ArtNetOpCodes fromInt(Integer integer) {
        return intToType.get(integer);
    }
}
