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
package nl.sonicity.raspi.dmx.handlers;

import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.GpioProviderBase;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;

import java.util.Map;

public class SimulatedGpioProvider extends GpioProviderBase implements GpioProvider {
    private String configName;

    public SimulatedGpioProvider() {
        Map<String, String> env = System.getenv();
        String config = env.get("SimulatedPlatform");
        if (config == null) {
            config = "RaspberryPi GPIO Provider";
        }

        configName = config;
    }

    public String getName() {
        return configName;
    }

    public void setState(Pin pin, PinState state) {
        this.getPinCache(pin).setState(state);
        this.dispatchPinDigitalStateChangeEvent(pin, state);
    }

    public void setAnalogValue(Pin pin, double value) {
        this.getPinCache(pin).setAnalogValue(value);
        this.dispatchPinAnalogValueChangeEvent(pin, value);
    }
}
