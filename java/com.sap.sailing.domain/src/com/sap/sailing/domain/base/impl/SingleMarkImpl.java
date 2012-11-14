package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.SingleMark;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class SingleMarkImpl extends NamedImpl implements SingleMark {
    private static final long serialVersionUID = 1900673146064411979L;

    private final String color;
    private final String shape;
    private final String pattern;

    public SingleMarkImpl(String name) {
        super(name);
        color = null;
        shape = null;
        pattern = null;
    }

    public SingleMarkImpl(String name, String color) {
        super(name);
        this.color = color;
        shape = null;
        pattern = null;
    }

    public SingleMarkImpl(String name, String color, String shape) {
        super(name);
        this.color = color;
        this.shape = shape;
        pattern = null;
    }

    public SingleMarkImpl(String name, String color, String shape, String pattern) {
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
    public Iterable<SingleMark> getMarks() {
        Iterable<? extends SingleMark> result = Collections.singleton(this);
        return (Iterable<SingleMark>) result;
    }

    @Override
    public SingleMark resolve(DomainFactory domainFactory) {
        SingleMark result = domainFactory.getOrCreateSingleMark(getName(), color, shape, pattern);
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
