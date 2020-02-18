package com.sap.sse.common.impl;

import java.io.Serializable;
import java.util.UUID;

import com.sap.sse.common.NamedWithID;

public class NamedWithIDImpl extends NamedImpl implements NamedWithID {
    private static final long serialVersionUID = 8259043761754615072L;
    private final Serializable id;
    
    /**
     * Constructs a new random {@link UUID} as this object's {@link #getId() ID}.
     */
    public NamedWithIDImpl(String name) {
        this(name, UUID.randomUUID());
    }
    
    public NamedWithIDImpl(String name, Serializable id) {
        super(name);
        this.id = id;
    }
    
    @Override
    public Serializable getId() {
        return id;
    }
}
