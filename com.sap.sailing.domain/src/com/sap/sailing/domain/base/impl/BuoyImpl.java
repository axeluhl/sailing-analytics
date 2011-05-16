package com.sap.sailing.domain.base.impl;

import java.util.Collections;

import com.sap.sailing.domain.base.Buoy;

public class BuoyImpl extends NamedImpl implements Buoy {

    public BuoyImpl(String name) {
        super(name);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Buoy> getBuoys() {
        Iterable<? extends Buoy> result = Collections.singleton(this);
        return (Iterable<Buoy>) result;
    }

}
