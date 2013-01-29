package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.ArrayList;

import com.sap.sailing.domain.swisstimingadapter.Mark;

public class MarkImpl implements Mark {
    private final String description;
    private final int index;
    private final Iterable<String> devices;
    private final MarkType markType;
    
    public MarkImpl(String description, int index, Iterable<String> devices, MarkType markType) {
        super();
        this.description = description;
        this.index = index;
        this.markType = markType;
        ArrayList<String> l = new ArrayList<String>();
        for (String device : devices) {
            l.add(device);
        }
        this.devices = l;
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
    public Iterable<String> getDevices() {
        return devices;
    }
    
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getIndex());
        result.append(": ");
        result.append(getDescription());
        result.append(" [");
        for (String device : getDevices()) {
            result.append(device);
            result.append(", ");
        }
        result.delete(result.length()-2, result.length());
        result.append("]");
        return result.toString();
    }
    
    
}
