package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.Dimension;

public abstract class AbstractDimension<DataType> implements Dimension<DataType> {

    @Override
    public String toString() {
        return getName();
    }
    
    //Enforce hash code and equals in all subclasses
    @Override
    public abstract boolean equals(Object other);
    @Override
    public abstract int hashCode();

}
