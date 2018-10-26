package nl.sonicity.raspi.dmx.artnet;

import lombok.extern.slf4j.Slf4j;
import nl.sonicity.raspi.dmx.Util;

@Slf4j
public class DummyHandler extends DmxHandler {
    public DummyHandler(String name, int universe, int address, int width) {
        super(name, universe, address, width);
    }

    @Override
    public void onDmx(byte[] data) {
        log.debug("Reveived : {}", Util.bytesToHex(data));
    }
}
