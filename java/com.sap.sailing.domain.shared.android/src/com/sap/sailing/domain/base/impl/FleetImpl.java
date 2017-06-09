package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Fleet;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.NamedImpl;

/**
 * The comparability is implemented by an integer field. For fleets of a series to compare equal, the constructor
 * without this ordering criterion should be chosen (implicitly setting it to 0).
 * 
 * @author Axel Uhl (D043530)
 *
 */
public class FleetImpl extends NamedImpl implements Fleet {
    private static final long serialVersionUID = 7560417723293278246L;
    
    private final int ordering;
    
    private final Color color;

    public FleetImpl(String name) {
        this(name, 0);
    }
    
    /**
     * @param ordering
     *            a lesser value for <code>ordering</code> means "better"; for example, use 1 for the Gold fleet and 2
     *            for the Silver fleet.
     */
    public FleetImpl(String name, int ordering) {
        super(name);
        this.ordering = ordering;
        this.color = null;
    }

    public FleetImpl(String name, int ordering, Color color) {
        super(name);
        this.ordering = ordering;
        this.color = color;
    }

    @Override
    public int compareTo(Fleet o) {
        return this.ordering - ((FleetImpl) o).ordering;
    }

    @Override
    public int getOrdering() {
        return ordering;
    }

    @Override
    public Color getColor() {
        return color;
    }
}
