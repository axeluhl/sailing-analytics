package com.sap.sailing.domain.swisstimingadapter.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sap.sailing.domain.swisstimingadapter.Mark;
import com.sap.sse.common.Util;

public class MarkImpl implements Mark {
    private final String description;
    private final int index;
    private final Iterable<Serializable> deviceIds;
    private final MarkType markType;
    
    public MarkImpl(String description, int index, Iterable<Serializable> devices, MarkType markType) {
        super();
        this.description = description;
        this.index = index;
        this.markType = markType;
        List<Serializable> l = new ArrayList<>();
        for (Serializable device : devices) {
            l.add(device);
        }
        this.deviceIds = l;
    }

    @Override
    public MarkType getMarkType() {
        return markType;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public Iterable<Serializable> getDeviceIds() {
        return deviceIds;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getIndex());
        result.append(": ");
        result.append(getDescription());
        result.append(" [");
        result.append(Util.join(", ", getDeviceIds()));
        result.append("]");
        return result.toString();
    }
    
    
}
