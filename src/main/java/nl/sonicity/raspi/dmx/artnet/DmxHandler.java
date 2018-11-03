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

public abstract class DmxHandler {
    private String name;
    private int universe;
    private int address;
    private int width;


    public DmxHandler(String name, int universe, int address, int width) {
        if (address < 1 || address > 512) {
            throw new IllegalArgumentException("Address should be a valid DMX address between 1 and 512");
        }

        if (width < 1 || (width + address) > 513) {
            throw new IllegalArgumentException("Width should be valid for the combination of address and width");
        }

        this.name = name;
        this.universe = universe;
        this.address = address;
        this.width = width;
    }


    public String getName() {
        return name;
    }

    public int getUniverse() {
        return universe;
    }

    public int getAddress() {
        return address;
    }

    public int getWidth() {
        return width;
    }

    /** Called whenever the ArtNetNode receives new data from the network
     *
     * @param data
     */
    public abstract void onDmx(byte[] data);

    /** Called when the ArtNetNode is about to shutdown
     *
     */
    public void shutdown() { }

    /** Called when no DMX data has been received withing the globally configured timeout
     *
     */
    public void timeout() { }
}
