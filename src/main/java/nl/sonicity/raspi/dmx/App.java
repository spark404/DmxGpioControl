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

import nl.sonicity.raspi.dmx.artnet.ArtNetException;
import nl.sonicity.raspi.dmx.artnet.ArtNetNode;
import nl.sonicity.raspi.dmx.artnet.ArtNetNodeConfig;
import nl.sonicity.raspi.dmx.handlers.DmxToGPIOHandler;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Hello world!
 *
 */
public class App 
{
    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    private static volatile boolean shutdown = false;

    public static void main( String[] args ) throws ArtNetException {
        BasicConfigurator.configure();

        ArtNetNodeConfig artNetNodeConfig = ArtNetNodeConfig.builder()
                .network(0)
                .subnet(0)
                .universe(0)
                .networkInterface("eth0")
                .build();

        ArtNetNode artNetNode = new ArtNetNode(artNetNodeConfig);
        artNetNode.start();

        artNetNode.getHandlers().add(new DmxToGPIOHandler(0, 1));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Shutdown hook called, terminating app");
            shutdown = true;
            artNetNode.stop();
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

        LOG.info("Shutting down");
    }


}
