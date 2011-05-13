package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.BuoyGate;

public class BuoyGateImpl implements BuoyGate {
    private final Buoy left;
    private final Buoy right;
    private final String name;
    
    public BuoyGateImpl(Buoy left, Buoy right, String name) {
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
}
