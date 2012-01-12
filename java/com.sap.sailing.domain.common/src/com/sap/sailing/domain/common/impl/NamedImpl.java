package com.sap.sailing.domain.common.impl;

import com.sap.sailing.domain.common.Named;

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
