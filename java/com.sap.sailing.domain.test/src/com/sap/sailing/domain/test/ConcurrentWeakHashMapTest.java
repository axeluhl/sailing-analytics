package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

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
}
