package com.sap.sse.common.impl;

import java.util.UUID;

import com.sap.sse.common.NamedWithID;

public class NamedWithUUIDImpl extends NamedImpl implements NamedWithID {
    private static final long serialVersionUID = -3423929856042951606L;
    private final UUID id;
    
    public NamedWithUUIDImpl(String name) {
        this(name, UUID.randomUUID());
    }
    
    public NamedWithUUIDImpl(String name, UUID id) {
        super(name);
        this.id = id;
    }

    @Override
    public UUID getId() {
        return id;
    }
}
