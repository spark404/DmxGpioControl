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

import com.pi4j.io.gpio.*;
import lombok.extern.slf4j.Slf4j;
import nl.sonicity.raspi.dmx.artnet.DmxHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class DmxToGPIOHandler extends DmxHandler {
    private static final PinState STARTUP_STATE = PinState.HIGH;
    private static final PinState ENABLED_STATE = PinState.LOW;
    private static final PinState DISABLED_STATE = PinState.HIGH;

    private static final Pin[] RELAY_PINS = new Pin[]{
            RaspiPin.GPIO_07, RaspiPin.GPIO_11, RaspiPin.GPIO_13, RaspiPin.GPIO_15 };
    private final List<GpioPinDigitalOutput> relays = new ArrayList<>(RELAY_PINS.length);

    private final GpioController gpio;

    public DmxToGPIOHandler(GpioController gpioController, int universe, int address) {
        super("Dmx2GPIO", universe, address, 8);
        this.gpio = gpioController;

        Arrays.stream(RELAY_PINS).forEach(pin -> {
            GpioPinDigitalOutput relay = gpio.provisionDigitalOutputPin(pin, STARTUP_STATE);
            relay.setShutdownOptions(true, STARTUP_STATE);
            log.debug("DMX address {} controls {}, current state {}", address, relay.getName(), relay.getState().getName());
            relays.add(relay);
        });
    }

    @Override
    public void onDmx(byte[] data) {
        IntStream.range(0, relays.size()).forEach(i ->
            toggleOnDmx(relays.get(i), data[i] & 0xFF)
        );
    }

    @Override
    public void shutdown() {
        gpio.shutdown();
    }

    @Override
    public void timeout() {
        IntStream.range(0, relays.size()).forEach(i ->
            relays.get(i).setState(STARTUP_STATE)
        );
    }

    private void toggleOnDmx(GpioPinDigitalOutput output, int dmxVal) {
        if (dmxVal <= 102 && output.isState(ENABLED_STATE)) {
            log.debug("{} set to {}", output.getName(), DISABLED_STATE.getName());
            output.setState(DISABLED_STATE);
        } else if (dmxVal >= 153 && output.isState(DISABLED_STATE)) {
            log.debug("{} set to {}", output.getName(), ENABLED_STATE.getName());
            output.setState(ENABLED_STATE);
        }

    }
}
