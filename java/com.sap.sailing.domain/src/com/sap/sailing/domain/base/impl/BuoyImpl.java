package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class BuoyImpl extends NamedImpl implements Buoy {
    private static final long serialVersionUID = 1900673146064411979L;

    private final String displayColor;
    
    /**
     * Creates a buoy with <code>null</code> as {@link #getDisplayColor() color}
     */
    public BuoyImpl(String name) {
        super(name);
        displayColor = null;
    }

    public BuoyImpl(String name, String color) {
        super(name);
        this.displayColor = color;
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
        Buoy result = domainFactory.getOrCreateBuoy(getName(), getDisplayColor());
        return result;
    }

    public String getDisplayColor() {
        return displayColor;
    }
}
