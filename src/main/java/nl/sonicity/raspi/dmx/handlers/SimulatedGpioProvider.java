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
