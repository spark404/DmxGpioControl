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

public class ArtDmx extends ArtNetPacket {

    private static final int OFFSET_SEQUENCE = 12;
    private static final int OFFSET_PHYSICAL_PORT = 13;
    private static final int OFFSET_ADDRESS = 14;
    private static final int OFFSET_LENGTH = 16;
    private static final int OFFSET_DMX = 18;

    private ArtDmx(byte[] packet) {
        this.packet = Arrays.copyOf(packet, packet.length);
    }

    public int getUniverse() {
        return packet[OFFSET_ADDRESS] & 0x0F;
    }

    public int getSubnet() {
        return packet[OFFSET_ADDRESS] >> 4;
    }

    public int getNetwork() {
        return packet[OFFSET_ADDRESS + 1];
    }

    public int getDmxLength() {
        return readUint16Lsb(packet, OFFSET_LENGTH);
    }

    public byte[] getDmxData() {
        int dmxLength = readUint16Lsb(packet, OFFSET_LENGTH);
        return Arrays.copyOfRange(packet, OFFSET_DMX, OFFSET_DMX + dmxLength);
    }

    public static ArtDmx fromBytes(byte[] data) {
        if (data.length < 20) {
            throw new ArtNetException("Packet too short");
        }

        if (data[8] != 0x00 || data[9] != 0x50) {
            throw new ArtNetException("Wrong opcode");
        }

        int protocolVersion = (data[10] << 8) + (data[11] & 0xff);
        if (protocolVersion < 14) {
            throw new ArtNetException("ArtNet protocol version not compatible");
        }

        return new ArtDmx(data);
    }

    public static class Builder extends ArtNetPacket.Builder<ArtDmx, Builder> {
        public Builder() {
            super(ArtNetOpCode.ARTNET_OP_DMX.getOpCode(), 530);
        }

        Builder sequence(int sequence) {
            if (sequence < 0 || sequence > 255) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            data[OFFSET_SEQUENCE] = (byte)sequence;

            return this;
        }

        Builder physicalPort(int physicalPort) {
            if (physicalPort < 0 || physicalPort > 255) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            data[OFFSET_PHYSICAL_PORT] = (byte)physicalPort;

            return this;
        }

        Builder universe(int universe) {
            if (universe < 0 || universe > 15) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            data[OFFSET_ADDRESS] = (byte)(data[OFFSET_ADDRESS] & 0xF0 ^ (byte)universe);

            return this;
        }

        Builder subnet(int subnet) {
            if (subnet < 0 || subnet > 15) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            data[OFFSET_ADDRESS] = (byte)(data[OFFSET_ADDRESS] & 0x0F ^ (byte)subnet << 4);

            return this;
        }

        Builder network(int network) {
            if (network < 0 || network > 127) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            data[OFFSET_ADDRESS + 1] = (byte)network;

            return this;
        }

        Builder dmx(byte[] dmx) {
            if (dmx.length != 512) {
                throw new IllegalArgumentException("Array length must be 512");
            }

            System.arraycopy(dmx, 0, data, OFFSET_DMX, 512);
            writeUint16Lsb(data, OFFSET_LENGTH, 512);
            return this;
        }

        @Override
        public ArtDmx build() {
            return new ArtDmx(data);
        }
    }

}
