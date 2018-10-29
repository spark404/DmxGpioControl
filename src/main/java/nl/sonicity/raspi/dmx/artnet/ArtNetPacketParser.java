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
import nl.sonicity.raspi.dmx.artnet.packets.ArtNetPacket;
import nl.sonicity.raspi.dmx.artnet.packets.ArtPoll;
import nl.sonicity.raspi.dmx.artnet.packets.ArtPollReply;

import java.util.Arrays;
import java.util.Optional;

public class ArtNetPacketParser {
    private ArtNetPacketParser() {}

    public static ArtNetPacket generatePacketByOpCode(ArtNetOpCodes opCode) throws ArtNetException {
        ArtNetPacket artNetPacket;
        try {
            artNetPacket = getTypeForOpCode(opCode)
                    .orElseThrow(() -> new ArtNetException("Unknown packet type received"))
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ArtNetException(e);
        }

        return artNetPacket;
    }

    public static ArtNetPacket parse(byte[] data) throws ArtNetException{
        final byte[] packetData = data.clone();

        byte[] id = Arrays.copyOfRange(packetData, 0, 8);
        if (!Arrays.equals(ArtNetPacket.ARTNET_ID, id)) {
            throw new ArtNetException("Invalid magic string, expected " + new String(ArtNetPacket.ARTNET_ID));
        }

        int opCodeValue = (packetData[9] << 8) + packetData[8];
        ArtNetOpCodes opCode = ArtNetOpCodes.fromInt(opCodeValue);

        ArtNetPacket artNetPacket;
        try {
            artNetPacket = getTypeForOpCode(opCode)
                    .orElseThrow(() -> new ArtNetException("Unknown packet type received"))
                    .newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ArtNetException(e);
        }

        artNetPacket.setLength(data.length);
        return artNetPacket.parse(data);
    }

    private static Optional<Class<? extends ArtNetPacket>> getTypeForOpCode(ArtNetOpCodes opCode) {
        Class clazz;
        switch (opCode) {
            case ARTNET_OP_POLL:
                clazz = ArtPoll.class;
                break;
            case ARNET_OP_POLLREPLY:
                clazz = ArtPollReply.class;
                break;
            case ARTNET_OP_DMX:
                clazz = ArtDmx.class;
                break;
            default:
                clazz = null;
        }
        return Optional.ofNullable(clazz);
    }
}
