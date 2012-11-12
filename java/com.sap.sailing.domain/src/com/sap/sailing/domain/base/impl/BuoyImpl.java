package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Buoy;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class BuoyImpl extends NamedImpl implements Buoy {
    private static final long serialVersionUID = 1900673146064411979L;

    private final String color;
    private final String shape;
    private final String pattern;

    public BuoyImpl(String name) {
        super(name);
        color = null;
        shape = null;
        pattern = null;
    }

    public BuoyImpl(String name, String color) {
        super(name);
        this.color = color;
        shape = null;
        pattern = null;
    }

    public BuoyImpl(String name, String color, String shape) {
        super(name);
        this.color = color;
        this.shape = shape;
        pattern = null;
    }

    public BuoyImpl(String name, String color, String shape, String pattern) {
        super(name);
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
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
        Buoy result = domainFactory.getOrCreateBuoy(getName(), color, shape, pattern);
        return result;
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public String getShape() {
        return shape;
    }

    @Override
    public String getPattern() {
        return pattern;
    }
}
