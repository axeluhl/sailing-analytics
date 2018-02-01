package com.sap.sse.util.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

/**
 * An unmodifiable navigable set.
 * 
 * @author Axel Uhl (D043530)
 */
public class UnmodifiableNavigableSet<E> extends AbstractUnmodifiableNavigableSet<E> implements NavigableSet<E> {
    private static final long serialVersionUID = -2829360843200741699L;
    private final NavigableSet<E> set;

    public UnmodifiableNavigableSet(NavigableSet<E> delegate) {
        set = delegate;
    }
    
    @Override
    public Comparator<? super E> comparator() {
        return set.comparator();
    }

    @Override
    public E first() {
        return set.first();
    }

    @Override
    public E last() {
        return set.last();
    }

    @Override
    public int size() {
        return set.size();
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return set.toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public E lower(E e) {
        return set.lower(e);
    }

    @Override
    public E floor(E e) {
        return set.floor(e);
    }

    @Override
    public E ceiling(E e) {
        return set.ceiling(e);
    }

    @Override
    public E higher(E e) {
        return set.higher(e);
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> setIterator = set.iterator();
        return new Iterator<E>() {
            @Override
            public boolean hasNext() {
                return setIterator.hasNext();
            }

            @Override
            public E next() {
                return setIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new UnmodifiableNavigableSet<E>(set.descendingSet());
    }

    @Override
    public Iterator<E> descendingIterator() {
        final Iterator<E> setIterator = set.descendingIterator();
        return new Iterator<E>() {
            @Override
            public boolean hasNext() {
                return setIterator.hasNext();
            }

            @Override
            public E next() {
                return setIterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new UnmodifiableNavigableSet<E>(set.subSet(fromElement, fromInclusive, toElement, toInclusive));
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new UnmodifiableNavigableSet<E>(set.headSet(toElement, inclusive));
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new UnmodifiableNavigableSet<E>(set.tailSet(fromElement, inclusive));
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return Collections.unmodifiableSortedSet(set.subSet(fromElement, toElement));
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return Collections.unmodifiableSortedSet(set.headSet(toElement));
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return Collections.unmodifiableSortedSet(set.tailSet(fromElement));
    }
}
