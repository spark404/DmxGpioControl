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
import nl.sonicity.raspi.dmx.artnet.ArtNetOpCodes;

import java.util.Arrays;

@Slf4j
public abstract class ArtNetPacket {
    public static final byte[] ARTNET_ID = { 'A', 'r', 't', '-', 'N', 'e', 't', 0x0};
    static final int ARTNET_VERSION = 14;
    static final int OEM = 0x4242;

    private final ArtNetOpCodes opCode;
    private byte[] data;
    private int length;

    ArtNetPacket(ArtNetOpCodes opCode) {
        this.opCode = opCode;
    }

    public abstract ArtNetPacket parse(byte [] data) throws ArtNetException;

    public ArtNetOpCodes getOpCode() {
        return opCode;
    }

    void setHeader() {
        System.arraycopy(ARTNET_ID, 0, data, 0, 8);
        data[8] = (byte)(opCode.getOpCode() & 0xff);
        data[9] = (byte)((opCode.getOpCode() >> 8) & 0xff);
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = Arrays.copyOf(data, data.length);
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int readIntMsb(byte[] data, int startPos) {
        return (data[startPos] & 0xff) + (data[startPos + 1] << 8);
    }

    public int readIntLsb(byte[] data, int startPos) {
        return (data[startPos] << 8) + (data[startPos + 1] & 0xff);
    }

    public void writeIntMsb(byte[] data, int startPos, int value) {
        data[startPos] = (byte)(value & 0xff);
        data[startPos + 1] = (byte)((value >> 8) & 0xff);
    }

    public void writeIntLsb(byte[] data, int startPos, int value) {
        data[startPos] = (byte)((value >> 8) & 0xff);
        data[startPos + 1] = (byte)(value & 0xff);
    }
}
