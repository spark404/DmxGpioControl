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

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioProvider;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import lombok.extern.slf4j.Slf4j;
import nl.sonicity.raspi.dmx.artnet.ArtNetNode;
import nl.sonicity.raspi.dmx.artnet.ArtNetNodeConfig;
import nl.sonicity.raspi.dmx.handlers.DmxToGPIOHandler;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.function.Function;

@Slf4j
public class App
{
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private static volatile boolean shutdown = false;

    public static void main( String[] args ) throws Exception {
        BasicConfigurator.configure();

        Properties properties = readProperties();
        String networkInterface =
                properties.getProperty("artnet.interface", "eth0");
        String gpioProviderClass =
                properties.getProperty("gpio.provider", "com.pi4j.io.gpio.RaspiGpioProvider");

        GpioFactory.setDefaultProvider(getGpioProvider(gpioProviderClass));
        GpioController gpioController = GpioFactory.getInstance();

        ArtNetNodeConfig artNetNodeConfig = ArtNetNodeConfig.builder()
                .network(0)
                .subnet(0)
                .universe(0)
                .networkInterface(networkInterface)
                .build();

        ArtNetNode artNetNode = new ArtNetNode(artNetNodeConfig);
        artNetNode.start();

        DmxToGPIOHandler dmxToGPIOHandler = new DmxToGPIOHandler(gpioController, 0, 1);
        artNetNode.getHandlers().add(dmxToGPIOHandler);

        Debouncer<GpioPin> debouncer = new Debouncer<>(gpioPin -> {
            log.info("Debounced input state for pin {} is {}", gpioPin.getName(), "blub");
            return null;
        }, 200);

        GpioPinDigitalInput buttonOne = gpioController.provisionDigitalInputPin(RaspiPin.GPIO_02);
        GpioPinDigitalInput buttonTwo = gpioController.provisionDigitalInputPin(RaspiPin.GPIO_03);
        gpioController.addListener((GpioPinListenerDigital)event -> debouncer.call(event.getPin()), buttonOne, buttonTwo);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook called, terminating app");
            artNetNode.stop();
            shutdown = true;
        }));

        while (!shutdown) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // Sleep was interrupted, let's shutdown
                shutdown = true;
                Thread.currentThread().interrupt();
            }
        }

        debouncer.terminate();

        LOG.info("Shutting down");
    }

    private static GpioProvider getGpioProvider(String gpioProviderClass) throws Exception {
        Class<?> providerClass = App.class.getClassLoader().loadClass(gpioProviderClass);
        return (GpioProvider) providerClass.getConstructor().newInstance();
    }

    private static Properties readProperties() throws IOException {
        Properties properties = new Properties();

        // Load properties from classpath
        try (InputStream in = App.class.getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        }

        if (System.getenv("properties") != null) {
            // We have configured properties file
            try (InputStream in = new FileInputStream(System.getenv("properties"))) {
                properties.load(in);
            }
        }

        return properties;
    }

}
