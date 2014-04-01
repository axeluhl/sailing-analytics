package com.sap.sailing.domain.igtimiadapter.impl;

import com.sap.sailing.domain.igtimiadapter.HasId;

public class HasIdImpl implements HasId {
    private final long id;

    protected HasIdImpl(long id) {
        super();
        this.id = id;
    }
    
    @Override
    public long getId() {
        return id;
    }
}
