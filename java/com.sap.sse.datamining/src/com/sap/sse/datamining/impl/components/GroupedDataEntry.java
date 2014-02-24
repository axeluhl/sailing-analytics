package com.sap.sse.datamining.impl.components;

import com.sap.sse.datamining.shared.GroupKey;

public class GroupedDataEntry<DataType> {
    
    private final GroupKey key;
    private final DataType dataEntry;

    public GroupedDataEntry(GroupKey key, DataType dataEntry) {
        this.key = key;
        this.dataEntry = dataEntry;
    }

    public GroupKey getKey() {
        return key;
    }

    public DataType getDataEntry() {
        return dataEntry;
    }

    @Override
    public String toString() {
        return "[" + key + ", " + dataEntry + "]";
    }

}
