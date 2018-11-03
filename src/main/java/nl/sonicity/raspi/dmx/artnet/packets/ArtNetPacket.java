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

import lombok.extern.slf4j.Slf4j;
import nl.sonicity.raspi.dmx.artnet.ArtNetException;
import nl.sonicity.raspi.dmx.artnet.ArtNetOpCode;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

@Slf4j
public abstract class ArtNetPacket {
    static final byte[] ARTNET_ID = { 'A', 'r', 't', '-', 'N', 'e', 't', 0x0};
    static final byte[] ARTNET_VERSION = { 0x00, 0x0E };  // 14
    static final int OEM = 0x4242;

    protected byte[] packet;

    public abstract static class Builder<U, T extends Builder<U, T>> {
        protected byte[] data;

        public Builder(int opCode, int packetLength) {
            data = new byte[packetLength];
            System.arraycopy(ARTNET_ID, 0, data, 0, 8);

            data[8] = (byte)(opCode & 0xff);
            data[9] = (byte)((opCode >> 8) & 0xff);

            data[10] = ARTNET_VERSION[0];
            data[11] = ARTNET_VERSION[1];
        }

        public abstract U build();
    }

    public byte[] toBytes() {
        return Arrays.copyOf(packet, packet.length);
    }

    public static ArtNetPacket parseBytes(byte[] data) {
        if (data.length < 12) {
            throw new ArtNetException("Malformed packet");
        }

        byte[] id = Arrays.copyOfRange(data, 0, 8);
        if (!Arrays.equals(ArtNetPacket.ARTNET_ID, id)) {
            throw new ArtNetException("Malformed packet");
        }

        ArtNetOpCode opCode = extractOpCode(data);
        Class clazz = opCode.getPacketClass();

        try {
            return (ArtNetPacket) clazz.getMethod("fromBytes", byte[].class).invoke(clazz, data);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new ArtNetException(String.format("Failed to construct class for packet type %s", opCode.toString()), e);
        }
    }

    public static ArtNetOpCode extractOpCode(byte[] data) {
        int opCodeValue = readUint16Msb(data, 8);
        return ArtNetOpCode.fromInt(opCodeValue);
    }

    public static int readUint16Msb(byte[] data, int startPos) {
        return (data[startPos] & 0xff) + (data[startPos + 1] << 8);
    }

    public static int readUint16Lsb(byte[] data, int startPos) {
        return (data[startPos] << 8) + (data[startPos + 1] & 0xff);
    }

    public static int readUint8(byte[] data, int offset) {
        return data[offset] & 0xff;
    }

    public static void writeUint16Msb(byte[] data, int startPos, int value) {
        data[startPos] = (byte)(value & 0xff);
        data[startPos + 1] = (byte)((value >> 8) & 0xff);
    }

    public static void writeUint16Lsb(byte[] data, int startPos, int value) {
        data[startPos] = (byte)((value >> 8) & 0xff);
        data[startPos + 1] = (byte)(value & 0xff);
    }

    public static void writeUint8(byte[] data, int offset, int value) {
        data[offset] = (byte)(value);
    }


}
