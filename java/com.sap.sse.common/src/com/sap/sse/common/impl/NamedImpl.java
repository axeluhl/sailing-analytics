package com.sap.sse.common.impl;

import com.sap.sse.common.Named;


public class NamedImpl implements Named {
    private static final long serialVersionUID = -4815125282671451300L;
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
