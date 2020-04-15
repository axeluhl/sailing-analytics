package com.sap.sailing.racecommittee.app.ui.adapters;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.sap.sailing.domain.abstractlog.race.CompetitorResult;
import com.sap.sailing.racecommittee.app.domain.impl.CompetitorResultEditableImpl;

/**
 * In addition to being an array list of {@link CompetitorResultEditableImpl} objects, instances of this class can
 * compute the {@link #getFirstRankZeroPosition()} which comes in handy in several places where such a list is managed.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class CompetitorResultsList<C extends CompetitorResult> implements List<C> {
    private final List<C> list;

    public CompetitorResultsList(List<C> list) {
        super();
        this.list = list;
    }

    @Override
    public void add(int location, C object) {
        list.add(location, object);
    }

    @Override
    public boolean add(C object) {
        return list.add(object);
    }

    @Override
    public boolean addAll(int location, Collection<? extends C> collection) {
        return list.addAll(location, collection);
    }

    @Override
    public boolean addAll(Collection<? extends C> collection) {
        return list.addAll(collection);
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean contains(Object object) {
        return list.contains(object);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return list.containsAll(collection);
    }

    @Override
    public boolean equals(Object object) {
        return list.equals(object);
    }

    @Override
    public C get(int location) {
        return list.get(location);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public int indexOf(Object object) {
        return list.indexOf(object);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public Iterator<C> iterator() {
        return list.iterator();
    }

    @Override
    public int lastIndexOf(Object object) {
        return list.lastIndexOf(object);
    }

    @Override
    public ListIterator<C> listIterator() {
        return list.listIterator();
    }

    @Override
    public ListIterator<C> listIterator(int location) {
        return list.listIterator(location);
    }

    @Override
    public C remove(int location) {
        return list.remove(location);
    }

    @Override
    public boolean remove(Object object) {
        return list.remove(object);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return list.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return list.retainAll(collection);
    }

    @Override
    public C set(int location, C object) {
        return list.set(location, object);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public List<C> subList(int start, int end) {
        return list.subList(start, end);
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return list.toArray(array);
    }

    /**
     * Determines the first position of an item in {@link #mCompetitor} for which its
     * {@link CompetitorResult#getOneBasedRank() rank} is zero and for which this condition holds for all its
     * successors. This means in particular that any in-between rank-0 item that is followed by other non-zero-ranked
     * items will <em>not</em> be returned as the first such item.
     * <p>
     * 
     * If no such item is found, the size of {@link #mCompetitor} is returned, thus pointing "behind" the end of the
     * list.
     */
    public int getFirstRankZeroPosition() {
        int result = size();
        for (int i = size() - 1; i >= 0; i--) {
            if (get(i).getOneBasedRank() == 0) {
                result = i;
            } else {
                break;
            }
        }
        return result;
    }
}
