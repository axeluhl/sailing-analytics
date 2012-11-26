package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.DomainFactory;
import com.sap.sailing.domain.common.impl.NamedImpl;

public class MarkImpl extends NamedImpl implements Mark {
    private static final long serialVersionUID = 1900673146064411979L;

    private final String color;
    private final String shape;
    private final String pattern;

    public MarkImpl(String name) {
        super(name);
        color = null;
        shape = null;
        pattern = null;
    }

    public MarkImpl(String name, String color) {
        super(name);
        this.color = color;
        shape = null;
        pattern = null;
    }

    public MarkImpl(String name, String color, String shape) {
        super(name);
        this.color = color;
        this.shape = shape;
        pattern = null;
    }

    public MarkImpl(String name, String color, String shape, String pattern) {
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
    public Iterable<Mark> getMarks() {
        Iterable<? extends Mark> result = Collections.singleton(this);
        return (Iterable<Mark>) result;
    }

    @Override
    public Mark resolve(DomainFactory domainFactory) {
        Mark result = domainFactory.getOrCreateMark(getName(), color, shape, pattern);
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
