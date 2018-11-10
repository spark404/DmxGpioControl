package nl.sonicity.raspi.dmx;

import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinState;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Class to manage the tray for the PNP
 *
 */
@Slf4j
public class TrayManager {
    public static final long TRAY_CLOSE_DELAY = 5000L;
    private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);

    private GpioPinDigitalOutput relay;
    private AtomicBoolean busy = new AtomicBoolean(false);

    public TrayManager(GpioPinDigitalOutput relay) {
        this.relay = relay;
    }

    public void openClose() {
        if (busy.compareAndSet(false, true)) {
            log.debug("Not busy, triggering open close");
            internalOpenClose(() -> busy.set(false));
        } else {
            log.debug("Busy, ignoring request");
        }
    }

    public void openClose(long delay) {
        if (busy.compareAndSet(false, true)) {
            log.debug("Not busy, triggering {}ms delayed open close", delay);
            sched.schedule(() -> internalOpenClose(() -> busy.set(false)), delay, TimeUnit.MILLISECONDS);
        } else {
            log.debug("Busy, ignoring request");
        }
    }

    private void internalOpenClose(Runnable reset) {
        try {
            relay.pulse(50L, PinState.LOW, true);
            sched.schedule(() -> {
                try {
                    relay.pulse(50L, PinState.LOW, true);
                } catch (RuntimeException e) {
                    reset.run();
                    log.debug("Reset busy due to error", e);
                    throw e;
                } finally {
                    reset.run();
                    log.debug("Reset busy");
                }
            }, TRAY_CLOSE_DELAY, TimeUnit.MILLISECONDS);
        } catch (RuntimeException e) {
            reset.run();
            log.debug("Reset busy due to error", e);
            throw e;
        }
    }
}
