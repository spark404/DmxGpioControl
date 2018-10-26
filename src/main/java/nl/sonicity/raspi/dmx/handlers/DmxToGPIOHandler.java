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
    private final GpioController gpio = GpioFactory.getInstance();
    private final GpioPinDigitalOutput relay1;
    private final GpioPinDigitalOutput relay2;
    private final GpioPinDigitalOutput relay3;
    private final GpioPinDigitalOutput relay4;

    public DmxToGPIOHandler(int universe, int address) {
        super("Dmx2GPIO", universe, address, 8);
        relay1 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_07, PinState.LOW);
        relay2 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_17, PinState.LOW);
        relay3 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_27, PinState.LOW);
        relay4 = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, PinState.LOW);

    }

    @Override
    public void onDmx(byte[] data) {
        int val1 = data[0] & 0xFF;
        if (val1 <= 102 && relay1.isState(PinState.HIGH)) {
            log.debug("Toggle relay 1 LOW");
            relay1.low();
        } else if (val1 >= 153 && relay1.isState(PinState.LOW)) {
            log.debug("Toggle relay 1 HIGH");
            relay1.high();
        }
    }
}
