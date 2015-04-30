package com.sap.sse.datamining.shared.impl;

import com.sap.sse.datamining.shared.DataMiningSession;

public abstract class AbstractDataMiningSession implements DataMiningSession {
    private static final long serialVersionUID = 4420076115971081397L;
    
    @Override
    public String asString() {
        return toString();
    }
    
    //Enforce methods in all subclasses
    @Override
    public abstract String toString();
    @Override
    public abstract boolean equals(Object other);
    @Override
    public abstract int hashCode();

}
