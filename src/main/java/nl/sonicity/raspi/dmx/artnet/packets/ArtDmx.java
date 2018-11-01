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

import java.util.Arrays;

public class ArtDmx extends ArtNetPacket {

    private static final int DMX_ADDRESS = 14;
    private static final int DMX_LENGTH_OFFSET = 16;
    private static final int DMX_DATA_OFFSET = 18;
    private int network;
    private int subnet;
    private int universe;

    public ArtDmx() {
        super(ArtNetOpCodes.ARTNET_OP_DMX);
    }

    @Override
    public ArtNetPacket parse(byte[] data) throws ArtNetException {
        isValid(data);

        setData(data);

        // 15 Bit Address
        // bit 15 = 0, bit 14-8 = net, bit 7-4 subnet, bit 3-0 universe
        network = data[DMX_ADDRESS + 1];
        subnet = data[DMX_ADDRESS] >> 4;
        universe = data[DMX_ADDRESS] & 0x0F;
        return this;
    }

    private void isValid(byte[] packet) throws ArtNetException {
        if (packet.length < 20) {
            throw new ArtNetException("Packet too short");
        }

        for (int i = 0; i < ARTNET_ID.length; i++) {
            if (packet[i] != ARTNET_ID[i]) {
                throw new ArtNetException("Missing protocol header");
            }
        }

        if (packet[8] != 0x00 || packet[9] != 0x50) {
            throw new ArtNetException("Wrong opcode");
        }
    }

    public byte[] getDmxData() {
        byte[] data = getData();
        int dmxLength = readIntLsb(data, DMX_LENGTH_OFFSET);
        return Arrays.copyOfRange(data, DMX_DATA_OFFSET, DMX_DATA_OFFSET + dmxLength);
    }

    public int getDmxLength() {
        byte[] data = getData();
        return readIntLsb(data, DMX_LENGTH_OFFSET);
    }

    public int getNetwork() {
        return network;
    }

    public int getSubnet() {
        return subnet;
    }

    public int getUniverse() {
        return universe;
    }
}
