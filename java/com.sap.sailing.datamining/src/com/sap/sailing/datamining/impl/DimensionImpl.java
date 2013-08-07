package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.Dimension;
import com.sap.sailing.datamining.shared.GroupKey;

public class DimensionImpl extends AbstractGroupKey implements Dimension {
    
    private Type type;

    public DimensionImpl(Type type) {
        this.type = type;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public String asString() {
        return getType().toString();
    }

    @Override
    public boolean hasSubKey() {
        return false;
    }

    @Override
    public GroupKey getSubKey() {
        return null;
    }

    @Override
    public boolean equals(Object other) {
        return type.equals(other);
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

}
