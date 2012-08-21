package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.InvertibleComparator;

/**
 * An adapter for an invertible comparator.
 */
public class InvertibleComparatorAdapter<T> implements InvertibleComparator<T> {
    boolean ascending;

    /**
     * Create an InvertibleComparator that sorts ascending by default.
     */
    public InvertibleComparatorAdapter() {
        this(true);
    }

    /**
     * Create an InvertibleComparator that sorts based on the provided order. 
     * @param ascending
     *            the sort order: ascending (true) or descending (false)
     */
    public InvertibleComparatorAdapter(boolean ascending) {
        setAscending(ascending);
    }

    /**
     * Specify the sort order: ascending (true) or descending (false).
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Return the sort order: ascending (true) or descending (false).
     */
    public boolean isAscending() {
        return this.ascending;
    }

    /**
     * Invert the sort order Ascending -> descending or descending -> ascending.
     */
    public void invertOrder() {
        this.ascending = !this.ascending;
    }

    public int compare(T o1, T o2) {
        throw new RuntimeException("You muste overwrite the compare() method to use this comparator ");
    }

    @Override
    public String toString() {
        return "InvertibleComparatorAdpapter: ascending=" + this.ascending;
    }
}