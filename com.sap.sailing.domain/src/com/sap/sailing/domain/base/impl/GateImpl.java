package com.sap.sailing.domain.base.impl;

import java.util.ArrayList;
import java.util.Collection;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.Gate;

public class GateImpl implements Gate {
    private final Buoy left;
    private final Buoy right;
    private final String name;
    
    public GateImpl(Buoy left, Buoy right, String name) {
        super();
        this.left = left;
        this.right = right;
        this.name = name;
    }

    @Override
    public Buoy getLeft() {
        return left;
    }

    @Override
    public Buoy getRight() {
        return right;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Iterable<Buoy> getBuoys() {
        Collection<Buoy> result = new ArrayList<Buoy>(2);
        result.add(getLeft());
        result.add(getRight());
        return result;
    }
}
