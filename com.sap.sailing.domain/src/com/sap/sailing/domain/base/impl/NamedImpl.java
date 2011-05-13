package com.sap.sailing.domain.base.impl;

import com.sap.sailing.domain.base.Named;

public class NamedImpl implements Named {
    private final String name;
    
    public NamedImpl(String name) {
        super();
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String toString() {
        return getName();
    }

}
