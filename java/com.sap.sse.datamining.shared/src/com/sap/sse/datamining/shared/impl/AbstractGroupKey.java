package com.sap.sse.datamining.shared.impl;

import com.sap.sse.datamining.shared.GroupKey;


public abstract class AbstractGroupKey implements GroupKey {
    private static final long serialVersionUID = 183947887066745315L;
    
    @Override
    public String toString() {
        return asString();
    }
    
    @Override
    public int compareTo(GroupKey key) {
        return asString().compareTo(key.asString());
    }
    
    //Enforce hash code and equals in all subclasses
    @Override
    public abstract boolean equals(Object other);
    @Override
    public abstract int hashCode();

}
