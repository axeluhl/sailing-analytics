package com.sap.sailing.domain.swisstimingadapter.impl;

import java.util.ArrayList;

import com.sap.sailing.domain.swisstimingadapter.Mark;

public class MarkImpl implements Mark {
    private final String description;
    private final int index;
    private final Iterable<String> devices;
    
    
    public MarkImpl(String description, int index, Iterable<String> devices) {
        super();
        this.description = description;
        this.index = index;
        ArrayList<String> l = new ArrayList<String>();
        for (String device : devices) {
            l.add(device);
        }
        this.devices = l;
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

}
