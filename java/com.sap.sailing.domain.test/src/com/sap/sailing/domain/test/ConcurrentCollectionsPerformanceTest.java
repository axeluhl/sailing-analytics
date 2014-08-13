package com.sap.sailing.domain.test;

import static org.junit.Assert.assertEquals;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Test;

import com.sap.sse.common.Util;

/**
 * Measures the performance of different concurrent collections types and shows their add and remove behavior for
 * different collection sizes and different numbers of concurrently accessing threads. The collections used must be able
 * to hold the same element multiple times, keeping count, not necessarily order.
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class ConcurrentCollectionsPerformanceTest {
    private static final int COUNT = 100000;
    
    private static class ConcurrentHashBag<T> extends AbstractCollection<T> {
        private ConcurrentHashMap<T, Integer> map = new ConcurrentHashMap<>();
        private int size;
        
        @Override
        public boolean contains(Object o) {
            return map.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            @SuppressWarnings("unchecked")
            T t = (T) o;
            Integer oldCount = map.remove(t);
            if (oldCount != null && oldCount != 1) {
                map.put(t, oldCount-1);
            }
            if (oldCount != null) {
                size--;
            }
            return oldCount != null;
        }

        @Override
        public boolean add(T e) {
            Integer oldCount = map.put(e, 1);
            if (oldCount != null && oldCount != 0) {
                map.put(e, oldCount+1);
            }
            size++;
            return true;
        }

        @Override
        public Iterator<T> iterator() {
            return map.keySet().iterator();
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return map.isEmpty();
        }
    }

    @Test
    public void testConcurrentLinkedQueue() {
        Collection<Object> c = new ConcurrentLinkedQueue<Object>();
        System.out.println("ConcurrentLinkedQueue" + runWith(c));
    }

    @Test
    public void testConcurrentHashMap() {
        Collection<Object> c = new ConcurrentHashBag<Object>();
        System.out.println("ConcurrentHashBag" + runWith(c));
    }

    private com.sap.sse.common.Util.Pair<Long, Long> runWith(Collection<Object> c) {
        List<Object> l = new ArrayList<>(COUNT);
        for (int i=0; i<COUNT; i++) {
            l.add(new Object());
        }
        // add all objects a second time to ensure
        for (Object o : new ArrayList<Object>(l)) {
            l.add(o);
        }
        long startInsert = System.currentTimeMillis();
        for (Object o : l) {
            c.add(o);
        }
        long endInsert = System.currentTimeMillis();
        assertEquals(l.size(), c.size());
        Collections.shuffle(l);
        long startRemove = System.currentTimeMillis();
        for (Object o : l) {
            c.remove(o);
        }
        long endRemove = System.currentTimeMillis();
        return new Util.Pair<Long, Long>(endInsert-startInsert, endRemove-startRemove);
    }
}
