package com.sap.sse.datamining.shared.impl;

import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.datamining.shared.GroupKey;


public abstract class AbstractGroupKey implements GroupKey {
    private static final long serialVersionUID = 183947887066745315L;
    private static final NaturalComparator naturalComparator = new NaturalComparator(/* case sensitive */ false);
    
    @Override
    public String toString() {
        return asString();
    }
    
    @Override
    public int compareTo(GroupKey key) {
        return naturalComparator.compare(asString(), key.asString());
    }

}
