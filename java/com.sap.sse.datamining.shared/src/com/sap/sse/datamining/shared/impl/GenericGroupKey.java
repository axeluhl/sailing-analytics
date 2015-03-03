package com.sap.sse.datamining.shared.impl;

import com.sap.sse.datamining.shared.GroupKey;


public class GenericGroupKey<T> extends AbstractGroupKey {
    private static final long serialVersionUID = -3838535617341226965L;
    
    private T value;

    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    GenericGroupKey() { }

    public GenericGroupKey(T value) {
        this.value = value;
    }
    
    public T getValue() {
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
    public String asString() {
        return value == null ? "null" : value.toString();
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
