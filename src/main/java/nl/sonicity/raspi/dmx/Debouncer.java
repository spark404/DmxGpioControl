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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Based on samples found at https://stackoverflow.com/questions/4742210/implementing-debounce-in-java
 * @param <T>
 */
public class Debouncer <T> {
    private final ScheduledExecutorService sched = Executors.newScheduledThreadPool(1);
    private final ConcurrentHashMap<T, TimerTask> delayedMap = new ConcurrentHashMap<>();
    private final Function<T, Void> callback;
    private final int interval;

    public Debouncer(Function<T, Void> c, int interval) {
        this.callback = c;
        this.interval = interval;
    }

    public void call(T key) {
        TimerTask task = new TimerTask(key);

        TimerTask prev;
        do {
            prev = delayedMap.putIfAbsent(key, task);
            if (prev == null)
                sched.schedule(task, interval, TimeUnit.MILLISECONDS);
        } while (prev != null && !prev.extend()); // Exit only if new task was added to map, or existing task was extended successfully
    }

    public void terminate() {
        sched.shutdown();
        try {
            sched.awaitTermination(interval * 2L, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            // NOOP
            Thread.currentThread().interrupt();
        }
    }

    // The task that wakes up when the wait time elapses
    private class TimerTask implements Runnable {
        private final T key;
        private long dueTime;
        private final Object lock = new Object();

        public TimerTask(T key) {
            this.key = key;
            extend();
        }

        public boolean extend() {
            synchronized (lock) {
                if (dueTime < 0) // Task has been shutdown
                    return false;
                dueTime = System.currentTimeMillis() + interval;
                return true;
            }
        }

        public void run() {
            synchronized (lock) {
                long remaining = dueTime - System.currentTimeMillis();
                if (remaining > 0) { // Re-schedule task
                    sched.schedule(this, remaining, TimeUnit.MILLISECONDS);
                } else { // Mark as terminated and invoke callback
                    dueTime = -1;
                    try {
                        callback.apply(key);
                    } finally {
                        delayedMap.remove(key);
                    }
                }
            }
        }
    }
}