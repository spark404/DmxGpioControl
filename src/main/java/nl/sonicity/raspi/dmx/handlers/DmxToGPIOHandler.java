package nl.sonicity.raspi.dmx.handlers;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import lombok.extern.slf4j.Slf4j;
import nl.sonicity.raspi.dmx.artnet.DmxHandler;

@Slf4j
public class DmxToGPIOHandler extends DmxHandler {
    private static final PinState STARTUP_STATE = PinState.HIGH;
    private static final PinState ENABLED_STATE = PinState.LOW;
    private static final PinState DISABLED_STATE = PinState.HIGH;

    private final GpioPinDigitalOutput relay1;
    private final GpioPinDigitalOutput relay2;
    private final GpioPinDigitalOutput relay3;
    private final GpioPinDigitalOutput relay4;
    private final GpioController gpio;

    public DmxToGPIOHandler(int universe, int address) {
        super("Dmx2GPIO", universe, address, 8);
        gpio = GpioFactory.getInstance();
        relay1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, STARTUP_STATE);
        relay1.setShutdownOptions(true, STARTUP_STATE);
        log.debug("DMX address {} controls {}, current state {}", address, relay1.getName(), relay1.getState().getName());

        relay2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_11, STARTUP_STATE);
        relay2.setShutdownOptions(true, STARTUP_STATE);
        log.debug("DMX address {} controls {}, current state {}", address + 1, relay2.getName(), relay2.getState().getName());

        relay3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_13, STARTUP_STATE);
        relay3.setShutdownOptions(true, STARTUP_STATE);
        log.debug("DMX address {} controls {}, current state {}", address + 2, relay3.getName(), relay3.getState().getName());

        relay4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_15, STARTUP_STATE);
        relay4.setShutdownOptions(true, STARTUP_STATE);
        log.debug("DMX address {} controls {}, current state {}", address + 3, relay4.getName(), relay4.getState().getName());
    }

    @Override
    public void onDmx(byte[] data) {
        toggleOnDmx(relay1, data[0] & 0xFF);
        toggleOnDmx(relay2, data[1] & 0xFF);
        toggleOnDmx(relay3, data[2] & 0xFF);
        toggleOnDmx(relay4, data[3] & 0xFF);
    }

    @Override
    public void shutdown() {
        gpio.shutdown();
    }

    private void toggleOnDmx(GpioPinDigitalOutput output, int dmxVal) {
        if (dmxVal <= 102 && output.isState(ENABLED_STATE)) {
            log.debug("Toggle {}", output.getName());
            output.toggle();
        } else if (dmxVal >= 153 && output.isState(DISABLED_STATE)) {
            log.debug("Toggle {}", output.getName());
            output.toggle();
        }

    }
}
