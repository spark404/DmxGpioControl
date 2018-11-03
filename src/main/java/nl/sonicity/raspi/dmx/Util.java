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
package nl.sonicity.raspi.dmx;

public class Util {
    private Util() {
    }

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] calculateArtNetAddress(byte[] macAddress, byte[] oemCode, boolean networkSwitch) {
        byte[] rawAddress = new byte[4];
        if (!networkSwitch) {
            rawAddress[0] = 2;
        } else {
            rawAddress[0] = 10;
        }
        // force to bytes
        byte b = (byte)((byte)(macAddress[3] + oemCode[0]) + oemCode[1]);
        byte c = macAddress[4];
        byte d = macAddress[5];

        rawAddress[1] = b;
        rawAddress[2] = c;
        rawAddress[3] = d;

        return rawAddress;
    }

}
