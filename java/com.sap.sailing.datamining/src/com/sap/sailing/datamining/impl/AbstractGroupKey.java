package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.shared.GroupKey;

public abstract class AbstractGroupKey implements GroupKey {
    
    @Override
    public String toString() {
        return asString();
    }
    
    //Enforce hash code and equals in all subclasses
    @Override
    public abstract boolean equals(Object other);
    @Override
    public abstract int hashCode();

}
