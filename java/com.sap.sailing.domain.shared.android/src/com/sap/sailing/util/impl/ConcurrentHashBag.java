package com.sap.sailing.util.impl;

import java.util.AbstractCollection;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An "almost" thread-safe bag implementation. The constraint: when for equal objects an add or remove is performed,
 * there should be no concurrency regarding that key. The implementation uses a {@link ConcurrentHashMap} internally.
 * 
 * @author Axel Uhl (D043530)
 */
public class ConcurrentHashBag<T> extends AbstractCollection<T> {
    private ConcurrentHashMap<T, Integer> map = new ConcurrentHashMap<T, Integer>();
    private final AtomicInteger size = new AtomicInteger();

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
            map.put(t, oldCount - 1);
        }
        if (oldCount != null) {
            if (size.get() == 0) {
                throw new RuntimeException("Internal error: size==0 although we just think we found an element that we rmeoved: "+o);
            } else {
                size.decrementAndGet();
            }
        }
        return oldCount != null;
    }

    @Override
    public boolean add(T e) {
        Integer oldCount = map.put(e, 1);
        if (oldCount != null && oldCount != 0) {
            map.put(e, oldCount + 1);
        }
        size.incrementAndGet();
        return true;
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return new Iterator();
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    private class Iterator implements java.util.Iterator<T> {
        private final java.util.Iterator<Entry<T, Integer>> iter = map.entrySet().iterator();
        private int howManyMore = 0;
        private boolean canRemove;
        private T lastElementOfWhichWeHaveMore;
        
        @Override
        public boolean hasNext() {
            return howManyMore > 0 || iter.hasNext();
        }

        @Override
        public T next() {
            final T result;
            if (howManyMore > 0) {
                result = lastElementOfWhichWeHaveMore;
                howManyMore--;
            } else {
                Entry<T, Integer> next = iter.next();
                howManyMore = next.getValue()-1;
                lastElementOfWhichWeHaveMore = next.getKey();
                result = next.getKey();
            }
            canRemove = true;
            return result;
        }

        @Override
        public void remove() {
            if (!canRemove) {
                throw new IllegalStateException("Can't remove through iterator without calling next");
            }
            ConcurrentHashBag.this.remove(lastElementOfWhichWeHaveMore);
            canRemove = false;
        }
    }
}
