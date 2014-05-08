package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sap.sailing.util.impl.ConcurrentWeakHashMap;

/**
 * Tests the class {@link ConcurrentWeakHashMap}.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ConcurrentWeakHashMapTest {
    @Test
    public void testPutAndGet() {
        ConcurrentWeakHashMap<String, String> m = new ConcurrentWeakHashMap<>();
        m.put("a", "b");
        assertEquals("b", m.get("a"));
    }

    @Test
    public void testPutAndIterate() {
        ConcurrentWeakHashMap<String, String> m = new ConcurrentWeakHashMap<>();
        m.put("a", "b");
        assertEquals("b", m.values().iterator().next());
        assertEquals("a", m.keySet().iterator().next());
        assertEquals("a", m.entrySet().iterator().next().getKey());
    }

    @Test
    public void testPutAndGetWithSubsequentGC() {
        Object key = createKeyObject();
        ConcurrentWeakHashMap<Object, String> m = new ConcurrentWeakHashMap<>();
        m.put(key, "b");
        assertEquals("b", m.get(key));
        key = null;
        forceEnqueuableWeakReferencesToBeEnqueued();
        assertTrue(m.isEmpty());
    }

    private void forceEnqueuableWeakReferencesToBeEnqueued() {
        System.gc();
        try {
            long[] hugeArrayThatWontGetAllocated = new long[Integer.MAX_VALUE];
            assertTrue(hugeArrayThatWontGetAllocated.length > 0);
        } catch (OutOfMemoryError e) {
            // Ignore OME
        }
        try {
            Thread.sleep(50); // give GC some time to finish enqueuing and finalizing all weakly-referenced objects
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Use a separate method for this, assuming that the compiler won't put the object on the using method's stack
     * @return
     */
    private Object createKeyObject() {
        return new Object();
    }
}
