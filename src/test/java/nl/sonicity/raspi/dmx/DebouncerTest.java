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

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import static org.mockito.Mockito.*;

@Slf4j
public class DebouncerTest {

    @Test
    public void testDebouncer() throws Exception {
        Debouncer<Marker> debouncer = new Debouncer<>(input -> {
            input.mark();
            return null;
        }, 50);

        Marker marker = mock(Marker.class);

        // We're assuming the calls below this happens within 200ms of the debounce
        debouncer.call(marker);
        debouncer.call(marker);
        debouncer.call(marker);
        debouncer.call(marker);
        debouncer.call(marker);

        debouncer.terminate();

        verify(marker, times(1)).mark();
    }


    @Test
    public void testDebouncerWithSecondCall() throws Exception {
        Debouncer<Marker> debouncer = new Debouncer<>(input -> {
            input.mark();
            return null;
        }, 50);

        Marker marker = mock(Marker.class);

        // We're assuming the calls below this happens within 200ms of the debounce
        debouncer.call(marker);
        debouncer.call(marker);
        debouncer.call(marker);
        debouncer.call(marker);
        debouncer.call(marker);
        Thread.sleep(60);
        debouncer.call(marker);
        debouncer.call(marker);
        debouncer.call(marker);

        debouncer.terminate();

        verify(marker, times(2)).mark();
    }

    @Test
    public void testDebouncerWithTwoMarkers() throws Exception {
        Debouncer<Marker> debouncer = new Debouncer<>(input -> {
            input.mark();
            return null;
        }, 50);

        Marker markerOne = mock(Marker.class);
        Marker markerTwo = mock(Marker.class);

        // We're assuming the calls below this happens within 200ms of the debounce
        debouncer.call(markerOne);
        debouncer.call(markerTwo);
        debouncer.call(markerTwo);
        debouncer.call(markerOne);
        debouncer.call(markerOne);
        Thread.sleep(60);
        debouncer.call(markerOne);
        debouncer.call(markerTwo);
        debouncer.call(markerOne);

        debouncer.terminate();

        verify(markerOne, times(2)).mark();
        verify(markerTwo, times(2)).mark();
    }

    public interface Marker {
        void mark();
    }
}