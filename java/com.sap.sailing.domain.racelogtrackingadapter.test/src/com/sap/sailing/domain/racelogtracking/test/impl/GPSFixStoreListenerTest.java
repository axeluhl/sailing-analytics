package com.sap.sailing.domain.racelogtracking.test.impl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import com.sap.sailing.domain.common.tracking.GPSFixMoving;
import com.sap.sailing.domain.persistence.racelog.tracking.impl.MongoSensorFixStoreImpl;
import com.sap.sailing.domain.racelog.tracking.FixReceivedListener;
import com.sap.sailing.domain.racelogtracking.DeviceIdentifier;
import com.sap.sailing.domain.racelogtracking.test.AbstractGPSFixStoreTest;

public class GPSFixStoreListenerTest extends AbstractGPSFixStoreTest {
    @Rule
    public Timeout TrackedRaceLoadsFixesTestTimeout = new Timeout(3 * 60 * 1000);

    /**
     * {@link MongoSensorFixStoreImpl} had broken synchronization of the listeners collection (add/removeListener
     * methods were synchronized but notifyListeners was not synchronized).
     */
    @Test
    public void lockingOfGPSFixStoreListenersIsWorkingCorrectly() throws InterruptedException {
        // The first listener will take an extended amount of time
        store.addListener(new ListenerWithDefinedHashCode(1) {
            @Override
            public void fixReceived(DeviceIdentifier device, GPSFixMoving fix) {
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }, device);
        // This ensures, that there is another listener so that the iterator isn't directly finished which would prevent
        // the exception to occur
        store.addListener(new ListenerWithDefinedHashCode(2), device);

        Thread thread = new Thread() {
            public void run() {
                try {
                    Thread.sleep(100);
                    // During iteration in the main thread this causes a modification that makes the iterator throw a
                    // ConcurrentModificationException on next()
                    store.addListener(new ListenerWithDefinedHashCode(3), device);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            };
        };
        thread.start();
        try {
            store.storeFix(device, createFix(100, 10, 20, 30, 40));
        } finally {
            thread.join(1000);
        }
    }

    /**
     * Explicitly setting the hashcode makes the iteration order deterministic.
     */
    private static class ListenerWithDefinedHashCode implements FixReceivedListener<GPSFixMoving> {
        private final int hashCode;

        public ListenerWithDefinedHashCode(int hashCode) {
            this.hashCode = hashCode;
        }

        @Override
        public void fixReceived(DeviceIdentifier device, GPSFixMoving fix) {
        }

        @Override
        public int hashCode() {
            return hashCode;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof ListenerWithDefinedHashCode)) {
                return false;
            }
            return hashCode == ((ListenerWithDefinedHashCode) other).hashCode;
        }
    }
}
