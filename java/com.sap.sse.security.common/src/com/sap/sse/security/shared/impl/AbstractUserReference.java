package com.sap.sse.security.shared.impl;

import java.io.Serializable;

import com.sap.sse.security.shared.UserReference;

public abstract class AbstractUserReference implements UserReference {
    private static final long serialVersionUID = -3639860207453072248L;

    private String name;
    
    public AbstractUserReference(String name) {
        this.name = name;
    }
    
    @Override
    public String getName() {
        return name;
    }

    @Override
    public Serializable getId() {
        return getName();
    }

    @Override
    public String toString() {
        return name;
    }
}
