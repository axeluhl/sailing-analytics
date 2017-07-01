package com.sap.sse.util.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.SortedSet;

/**
 * A view on a {@link NavigableSet} that reverses its order.
 * 
 * @author Axel Uhl (d043530)
 *
 * @param <E>
 */
public class DescendingNavigableSet<E> implements NavigableSet<E> {
    private final NavigableSet<E> set;
    
    public DescendingNavigableSet(NavigableSet<E> set) {
        if (set == null) {
            throw new IllegalArgumentException();
        }
        this.set = set;
    }
    
    @Override
    public Comparator<? super E> comparator() {
        return Collections.reverseOrder(set.comparator());
    }

    @Override
    public E first() {
        return set.last();
    }

    @Override
    public E last() {
        return set.first();
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
        Object[] result = new Object[size()];
        int i=0;
        for (E e : this) {
            result[i++] = e;
        }
        return result;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Object[] result = a;
        if (result.length < size()) {
            result = new Object[size()];
        }
        int i=0;
        for (E e : this) {
            result[i++] = e;
        }
        @SuppressWarnings("unchecked")
        T[] tResult = (T[]) result;
        return tResult;
    }

    @Override
    public boolean add(E e) {
        return set.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return set.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return set.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return set.removeAll(c);
    }

    @Override
    public void clear() {
        set.clear();
    }

    @Override
    public E lower(E e) {
        return set.higher(e);
    }

    @Override
    public E floor(E e) {
        return set.ceiling(e);
    }

    @Override
    public E ceiling(E e) {
        return set.floor(e);
    }

    @Override
    public E higher(E e) {
        return set.lower(e);
    }

    @Override
    public E pollFirst() {
        return set.pollLast();
    }

    @Override
    public E pollLast() {
        return set.pollFirst();
    }

    @Override
    public Iterator<E> iterator() {
        return set.descendingIterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return set;
    }

    @Override
    public Iterator<E> descendingIterator() {
        return set.iterator();
    }

    @Override
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new DescendingNavigableSet<E>(set.subSet(toElement, toInclusive, fromElement, fromInclusive));
    }

    @Override
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return new DescendingNavigableSet<E>(set.tailSet(toElement, inclusive));
    }

    @Override
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return new DescendingNavigableSet<E>(set.headSet(fromElement, inclusive));
    }

    @Override
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    @Override
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    @Override
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

}
