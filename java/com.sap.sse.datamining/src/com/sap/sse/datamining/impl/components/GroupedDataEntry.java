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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataEntry == null) ? 0 : dataEntry.hashCode());
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GroupedDataEntry<?> other = (GroupedDataEntry<?>) obj;
        if (dataEntry == null) {
            if (other.dataEntry != null)
                return false;
        } else if (!dataEntry.equals(other.dataEntry))
            return false;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        return true;
    }
    
    

}
