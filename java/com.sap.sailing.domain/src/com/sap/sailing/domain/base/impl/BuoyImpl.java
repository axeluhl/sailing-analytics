package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class BuoyImpl extends NamedImpl implements Buoy {
    private static final long serialVersionUID = 1900673146064411979L;

    public BuoyImpl(String name) {
        super(name);
    }
    
    @Override
    public Serializable getId() {
        return getName();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Buoy> getBuoys() {
        Iterable<? extends Buoy> result = Collections.singleton(this);
        return (Iterable<Buoy>) result;
    }

    @Override
    public Buoy resolve(DomainFactory domainFactory) {
        Buoy result = domainFactory.getOrCreateBuoy(getName());
        return result;
    }
}
