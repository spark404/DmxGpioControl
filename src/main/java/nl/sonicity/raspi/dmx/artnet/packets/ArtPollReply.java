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

import java.net.Inet4Address;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ArtPollReply extends ArtNetPacket {

    private static final int OFFSET_IPADDRESS = 10;
    private static final int OFFSET_UDPPORT = 14;
    private static final int OFFSET_VERSION = 16;
    private static final int OFFSET_NETSWITCH = 18;
    private static final int OFFSET_SUBNETSWITCH = 19;
    private static final int OFFSET_OEM = 20;
    private static final int OFFSET_UBEA_VERSION = 22;
    private static final int OFFSET_STATUS1 = 23;
    private static final int OFFSET_ESTAMAN = 24;
    private static final int OFFSET_SHORTNAME = 26;
    private static final int OFFSET_LONGNAME = 44;
    private static final int OFFSET_NODEREPORT = 108;
    private static final int OFFSET_PORTS = 172;
    private static final int OFFSET_PORTTYPE = 174;
    private static final int OFFSET_GOODINPUT = 178;
    private static final int OFFSET_GOODOUTPUT = 182;
    private static final int OFFSET_SWIN = 186;
    private static final int OFFSET_SWOUT = 190;
    private static final int OFFSET_SWVIDEO = 194;
    private static final int OFFSET_SWMACRO = 185;
    private static final int OFFSET_SWREMOTE = 196;
    private static final int OFFSET_STYLE = 200;
    private static final int OFFSET_MACADDR = 201;
    private static final int OFFSET_STATUS2 = 212;

    private ArtPollReply(byte[] packet) {
        this.packet = Arrays.copyOf(packet, packet.length);
    }

    public static ArtPollReply fromBytes(byte[] data) {
        if (data.length != 239) {
            throw new ArtNetException("Packet length invalid");
        }

        if (data[8] != 0x00 || data[9] != 0x21) {
            throw new ArtNetException("Wrong opcode");
        }

        return new ArtPollReply(data);
    }

    public String getShortName() {
        byte[] shortName = new byte[18];
        System.arraycopy(packet, OFFSET_SHORTNAME, shortName, 0, 18);
        int i;
        //noinspection StatementWithEmptyBody
        for (i = 0; i < 18 && shortName[i] != 0x0; i++) {
        }
        return new String(shortName, 0, i, Charset.forName("ASCII"));
    }

    public static class Builder extends ArtNetPacket.Builder<ArtPollReply, ArtPollReply.Builder> {
        public Builder() {
            super(ArtNetOpCode.ARTNET_OP_POLLREPLY.getOpCode(), 239);

            // Hardcoded fixed values
            writeUint16Msb(data, OFFSET_UDPPORT, 0x1936);
            writeUint16Lsb(data, OFFSET_OEM, ArtNetPacket.OEM);
            writeUint16Msb(data, OFFSET_ESTAMAN, ArtNetPacket.OEM);
            writeUint8(data, OFFSET_SWVIDEO, 0);
            writeUint8(data, OFFSET_SWMACRO, 0);
            writeUint8(data, OFFSET_SWREMOTE, 0);
        }

        public Builder ipAddress(Inet4Address inet4Address) {
            System.arraycopy(inet4Address.getAddress(), 0, data, OFFSET_IPADDRESS, 4);

            return this;
        }

        public Builder firmwareVersion(int firmwareVersion) {
            if (firmwareVersion < 0 || firmwareVersion > 65535) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            writeUint16Lsb(data, OFFSET_VERSION, firmwareVersion);

            return this;
        }

        public Builder netswitch(int netswitch) {
            if (netswitch < 0 || netswitch > 127) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            writeUint8(data, OFFSET_NETSWITCH, netswitch);

            return this;
        }

        public Builder subswitch(int subswitch) {
            if (subswitch < 0 || subswitch > 15) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            writeUint8(data, OFFSET_SUBNETSWITCH, subswitch);

            return this;
        }

        public Builder ubeaVersion(int ubeaVersion) {
            if (ubeaVersion < 0 || ubeaVersion > 255) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            writeUint8(data, OFFSET_UBEA_VERSION, ubeaVersion);

            return this;
        }

        public Builder shortName(String shortName) {
            byte[] value = shortName.getBytes(StandardCharsets.US_ASCII);
            if (value.length > 17) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            System.arraycopy(value, 0, data, OFFSET_SHORTNAME, value.length);

            return this;
        }

        public Builder longName(String longName) {
            byte[] value = longName.getBytes(StandardCharsets.US_ASCII);
            if (value.length > 63) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            System.arraycopy(value, 0, data, OFFSET_LONGNAME, value.length);

            return this;
        }

        public Builder nodeReport(String nodeReport) {
            byte[] value = nodeReport.getBytes(StandardCharsets.US_ASCII);
            if (value.length > 63) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            System.arraycopy(value, 0, data, OFFSET_NODEREPORT, value.length);

            return this;
        }

        public Builder numPorts(int numPorts) {
            if (numPorts < 0 || numPorts > 4) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            writeUint16Lsb(data, OFFSET_PORTS, numPorts);

            return this;
        }

        public Builder style(int style) {
            if (style < 0 || style > 255) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            writeUint8(data, OFFSET_STYLE, style);

            return this;
        }

        public Builder macAddress(byte[] macAddress) {
            if (macAddress.length != 6) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            System.arraycopy(macAddress, 0, data, OFFSET_MACADDR, macAddress.length);

            return this;
        }

        // TODO Split in settings
        public Builder status1(int status1) {
            if (status1 < 0 || status1 > 255) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            writeUint8(data, OFFSET_STATUS1, status1);

            return this;
        }

        // TODO Split in settings
        public Builder status2(int status2) {
            if (status2 < 0 || status2 > 255) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            writeUint8(data, OFFSET_STATUS2, status2);

            return this;
        }

        public Builder port(int port, boolean input, boolean output, int type) {
            if (port < 0 || port > 3) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            if (type < 0 || type > 15) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }

            byte portSettings = 0x0;
            if (output) {
                portSettings = (byte)(portSettings & 0xFF ^ (1 << 7));
            }
            if (input) {
                portSettings = (byte)(portSettings & 0xFF ^ (1 << 6));
            }
            portSettings = (byte)(portSettings ^ type);
            writeUint8(data, OFFSET_PORTTYPE + port, portSettings);

            return this;
        }

        public Builder swIn(int port, int universe) {
            if (port < 0 || port > 3) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            if (universe < 0 || universe > 15) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            writeUint8(data, OFFSET_SWIN + port, universe);

            return this;
        }

        public Builder swOut(int port, int universe) {
            if (port < 0 || port > 3) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            if (universe < 0 || universe > 15) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            writeUint8(data, OFFSET_SWOUT + port, universe);

            return this;
        }

        public Builder goodInput(int port, int status) {
            if (port < 0 || port > 3) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            if (status < 0 || status > 255) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            writeUint8(data, OFFSET_GOODINPUT + port, status);

            return this;
        }

        public Builder goodOutput(int port, int status) {
            if (port < 0 || port > 3) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            if (status < 0 || status > 255) {
                throw new IllegalArgumentException("Parameter out of bounds");
            }
            writeUint8(data, OFFSET_GOODOUTPUT + port, status);

            return this;
        }

        @Override
        public ArtPollReply build() {
            return new ArtPollReply(data);
        }
    }

}
