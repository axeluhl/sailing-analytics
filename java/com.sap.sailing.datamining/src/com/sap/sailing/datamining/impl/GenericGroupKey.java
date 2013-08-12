package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.shared.GroupKey;

public abstract class GenericGroupKey<T> extends AbstractGroupKey {
    
    private T value;

    public GenericGroupKey(T value) {
        this.value = value;
    }

    protected T getValue() {
        return value;
    }

    @Override
    public boolean hasSubKey() {
        return false;
    }
    
    @Override
    public GroupKey getMainKey() {
        return this;
    }

    @Override
    public GroupKey getSubKey() {
        return null;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        GenericGroupKey<?> other = (GenericGroupKey<?>) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
