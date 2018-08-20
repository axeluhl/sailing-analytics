package com.sap.sse.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterable that wraps another iterable and removes the elements of yet another iterable from the iteration it
 * implements. This can be used to "lazily" remove elements from an iteration order without having to construct
 * the full collection in-place.
 * 
 * @author Axel Uhl (D043530)
 *
 * @param <T>
 */
public class ObscuringIterable<T> implements Iterable<T> {
    private final Iterable<T> allTs;
    private final Iterable<T> suppressed;
    
    /**
     * @param suppressed
     *            the elements to suppress from {@code allTs} in the iteration order implemented by this object. May
     *            also be {@code null} or empty.
     */
    public ObscuringIterable(Iterable<T> allTs, Iterable<T> suppressed) {
        super();
        this.allTs = allTs;
        this.suppressed = suppressed;
    }

    @Override
    public Iterator<T> iterator() {
        final Iterator<T> result;
        if (suppressed == null || Util.isEmpty(suppressed)) {
            result = allTs.iterator();
        } else {
            result = new Iterator<T>() {
                private final Iterator<T> allIter = allTs.iterator();
                private T next = advance();
                
                private T advance() {
                    next = null;
                    while (allIter.hasNext() && next == null) {
                        next = allIter.next();
                        if (Util.contains(suppressed, next)) {
                            next = null;
                        }
                    }
                    return next;
                }
                
                @Override
                public boolean hasNext() {
                    return next != null;
                }
    
                @Override
                public T next() {
                    if (next == null) {
                        throw new NoSuchElementException();
                    }
                    final T result = next;
                    advance();
                    return result;
                }
    
                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        return result;
    }
}
