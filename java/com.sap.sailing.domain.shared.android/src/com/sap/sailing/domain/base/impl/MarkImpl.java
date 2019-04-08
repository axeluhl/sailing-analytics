package com.sap.sailing.domain.base.impl;

import java.io.Serializable;
import java.util.Collections;

import com.sap.sailing.domain.base.Mark;
import com.sap.sailing.domain.base.SharedDomainFactory;
import com.sap.sailing.domain.common.MarkType;
import com.sap.sse.common.Color;
import com.sap.sse.common.impl.NamedImpl;

public class MarkImpl extends NamedImpl implements Mark {
    private static final long serialVersionUID = 1900673146064411979L;

    private final Color color;
    private final String shape;
    private final String pattern;
    private final MarkType type;
    private final Serializable id;

    public MarkImpl(String name) {
        this(name, name);
    }

    public MarkImpl(Serializable id, String name) {
        this(id, name, MarkType.BUOY, /* color */null, /* shape */null, /* pattern */null);
    }

    public MarkImpl(Serializable id, String name, MarkType type, Color color, String shape, String pattern) {
        super(name);
        this.id = id;
        this.type = type;
        this.color = color;
        this.shape = shape;
        this.pattern = pattern;
    }

    @Override
    public Serializable getId() {
        return id;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<Mark> getMarks() {
        Iterable<? extends Mark> result = Collections.singleton(this);
        return (Iterable<Mark>) result;
    }

    @Override
    public Mark resolve(SharedDomainFactory domainFactory) {
        Mark result = domainFactory.getOrCreateMark(getId(), getName(), type, color, shape, pattern);
        return result;
    }

    @Override
    public Color getColor() {
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

    @Override
    public MarkType getType() {
        return type;
    }

    public String toString() {
        return getId() + " " + (getColor() == null ? "" : (getColor() + " ")) + super.toString();
    }
}
