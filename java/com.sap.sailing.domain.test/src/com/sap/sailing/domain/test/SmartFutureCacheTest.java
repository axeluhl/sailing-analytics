package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.util.impl.SmartFutureCache;
import com.sap.sailing.util.impl.SmartFutureCache.EmptyUpdateInterval;
import com.sap.sailing.util.impl.SmartFutureCache.UpdateInterval;

public class SmartFutureCacheTest {
    @Test
    public void testSuspendAndResume() {
        final boolean[] updateWasCalled = new boolean[1];
        SmartFutureCache<String, String, SmartFutureCache.EmptyUpdateInterval> sfc = new SmartFutureCache<String, String, SmartFutureCache.EmptyUpdateInterval>(
                new SmartFutureCache.AbstractCacheUpdater<String, String, SmartFutureCache.EmptyUpdateInterval>() {
                    @Override
                    public String computeCacheUpdate(String key, EmptyUpdateInterval updateInterval) throws Exception {
                        updateWasCalled[0] = true;
                        return "Humba";
                    }
                }, "SmartFutureCacheTest.testSuspendAndResume");
        sfc.suspend();
        sfc.triggerUpdate("Trala", /* updateInterval */ null);
        assertNull(sfc.get("Trala", /* waitForLatest */ false));
        assertFalse(updateWasCalled[0]);
        assertEquals("Humba", sfc.get("Trala", /* waitForLatest */ true));
        assertTrue(updateWasCalled[0]);
    }

    @Test
    public void testUnsuspended() throws InterruptedException {
        final boolean[] updateWasCalled = new boolean[1];
        final boolean[] cacheWasCalled = new boolean[1];
        final boolean[] mayProceed = new boolean[1];
        SmartFutureCache<String, String, SmartFutureCache.EmptyUpdateInterval> sfc = new SmartFutureCache<String, String, SmartFutureCache.EmptyUpdateInterval>(
                new SmartFutureCache.AbstractCacheUpdater<String, String, SmartFutureCache.EmptyUpdateInterval>() {
                    @Override
                    public String computeCacheUpdate(String key, EmptyUpdateInterval updateInterval) throws Exception {
                        updateWasCalled[0] = true;
                        return "Humba";
                    }
                }, "SmartFutureCacheTest.testUnsuspended") {
                    @Override
                    protected void cache(String key, String value) {
                        super.cache(key, value);
                        synchronized (mayProceed) {
                            while (!mayProceed[0]) {
                                try {
                                    cacheWasCalled.wait();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                } // wait until test driver has checked the un-updated cache
                            }
                        }
                        synchronized (cacheWasCalled) {
                            cacheWasCalled[0] = true;
                            cacheWasCalled.notifyAll();
                        }
                    }
        };
        sfc.triggerUpdate("Trala", /* updateInterval */ null);
        assertNull(sfc.get("Trala", /* waitForLatest */ false));
        synchronized (mayProceed) {
            mayProceed[0] = true;
            mayProceed.notifyAll(); // let cache update proceed
        }
        synchronized (cacheWasCalled) {
            while (!cacheWasCalled[0]) {
                cacheWasCalled.wait(); // and wait for update to have updated the cache
            }
        }
        assertTrue(updateWasCalled[0]); // update must have been called by now because we already waited for the cache to have been updated
        assertEquals("Humba", sfc.get("Trala", /* waitForLatest */ false));
        assertEquals("Humba", sfc.get("Trala", /* waitForLatest */ true));
    }
    
    private static class FromAToBUpdateInterval implements UpdateInterval<FromAToBUpdateInterval> {
        private final int a;
        private final int b;
        
        public FromAToBUpdateInterval(int a, int b) {
            super();
            this.a = a;
            this.b = b;
        }

        @Override
        public FromAToBUpdateInterval join(FromAToBUpdateInterval otherUpdateInterval) {
            return new FromAToBUpdateInterval(Math.min(getA(), otherUpdateInterval.getA()), Math.max(getB(), otherUpdateInterval.getB()));
        }

        public int getA() {
            return a;
        }

        public int getB() {
            return b;
        }
    }

    @Test
    public void testJoiningOfUpdateIntervalsWhileSuspended() {
        final boolean[] updateWasCalled = new boolean[1];
        SmartFutureCache<String, Integer, FromAToBUpdateInterval> sfc = new SmartFutureCache<String, Integer, FromAToBUpdateInterval>(
                new SmartFutureCache.AbstractCacheUpdater<String, Integer, FromAToBUpdateInterval>() {
                    @Override
                    public Integer computeCacheUpdate(String key, FromAToBUpdateInterval updateInterval) throws Exception {
                        updateWasCalled[0] = true;
                        return updateInterval.getA() + updateInterval.getB();
                    }
                }, "SmartFutureCacheTest.testJoiningOfUpdateIntervalsWhileSuspended");
        sfc.suspend();
        sfc.triggerUpdate("Trala", new FromAToBUpdateInterval(42, 48));
        assertNull(sfc.get("Trala", /* waitForLatest */ false));
        assertFalse(updateWasCalled[0]);
        sfc.triggerUpdate("Trala", new FromAToBUpdateInterval(43, 49)); // should result in update interval 42..49
        assertEquals(new Integer(91), sfc.get("Trala", /* waitForLatest */ true));
        assertTrue(updateWasCalled[0]);
    }

    @Test
    public void testJoiningOfUpdateIntervalsWhenBeingResumed() throws InterruptedException {
        final boolean[] updateWasCalled = new boolean[1];
        final boolean[] cacheWasCalled = new boolean[1];
        SmartFutureCache<String, Integer, FromAToBUpdateInterval> sfc = new SmartFutureCache<String, Integer, FromAToBUpdateInterval>(
                        new SmartFutureCache.AbstractCacheUpdater<String, Integer, FromAToBUpdateInterval>() {
                            @Override
                            public Integer computeCacheUpdate(String key, FromAToBUpdateInterval updateInterval) throws Exception {
                        updateWasCalled[0] = true;
                        return updateInterval.getA() + updateInterval.getB();
                    }
                }, "SmartFutureCacheTest.testJoiningOfUpdateIntervalsWhenBeingResumed") {
                    @Override
                    protected void cache(String key, Integer value) {
                        super.cache(key, value);
                        synchronized (cacheWasCalled) {
                            cacheWasCalled[0] = true;
                            cacheWasCalled.notifyAll();
                        }
                    }
        };
        sfc.suspend();
        sfc.triggerUpdate("Trala", new FromAToBUpdateInterval(42, 48));
        assertNull(sfc.get("Trala", /* waitForLatest */ false));
        assertFalse(updateWasCalled[0]);
        sfc.triggerUpdate("Trala", new FromAToBUpdateInterval(43, 49)); // should result in update interval 42..49
        assertFalse(updateWasCalled[0]);
        sfc.resume();
        synchronized (cacheWasCalled) {
            while (!cacheWasCalled[0]) {
                cacheWasCalled.wait(); // and wait for update to have updated the cache
            }
        }
        assertTrue(updateWasCalled[0]);
        assertEquals(new Integer(91), sfc.get("Trala", /* waitForLatest */ true));
    }
}
