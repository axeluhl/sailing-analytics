package com.sap.sailing.util.test;

import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import com.sap.sailing.util.impl.ConcurrentHashBag;

/**
 * Initiated by bug 2502 (http://bugzilla.sapsailing.com/bugzilla/show_bug.cgi?id=2502), this test tries to reproduce a
 * race condition that can lead to negative sizes and therefore negative array indices.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ConcurrentHashBagTest {
    private ConcurrentHashBag<String> bag;
    
    @Before
    public void setUp() {
        bag = new ConcurrentHashBag<>();
    }
    
    /**
     * Trying to provoke a negative size() value
     */
    @Test
    public void testMassConcurrentInsertAndRemove() throws InterruptedException {
        final boolean[] sawNegativeSize = new boolean[1];
        final boolean[] stopped = new boolean[1];
        final Runnable adderRemover = () -> {
            while (!stopped[0]) {
                bag.add("a");
                bag.add("b");
                bag.remove("a");
                bag.remove("b");
            }
        };
        Thread t1 = new Thread(adderRemover);
        Thread t2 = new Thread(adderRemover);
        Thread t3 = new Thread(adderRemover);
        Thread t4 = new Thread(adderRemover);
        Thread t5 = new Thread(() -> {
            while (!stopped[0]) {
                if (bag.size() < 0) {
                    sawNegativeSize[0] = true;
                }
            }
        });
        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        Thread.sleep(10000);
        stopped[0] = true;
        assertFalse(sawNegativeSize[0]);
    }
}
