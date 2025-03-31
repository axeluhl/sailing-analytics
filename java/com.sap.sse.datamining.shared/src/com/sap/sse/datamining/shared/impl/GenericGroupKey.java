package com.sap.sse.datamining.shared.impl;

import java.util.Collections;
import java.util.List;

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
    public boolean hasSubKeys() {
        return false;
    }
    
    @Override
    public int size() {
        return 1;
    }

    @Override
    public List<? extends GroupKey> getKeys() {
        return Collections.singletonList(this);
    }
    
    @Override
    public String asString() {
        return value == null ? "null" : value.toString();
    }
    
    @Override
    public int compareTo(GroupKey key) {
        int result = super.compareTo(key);
        if (key instanceof GenericGroupKey) {
            Object keyValue = ((GenericGroupKey<?>) key).value;
            if (value instanceof Enum && keyValue instanceof Enum) {
                result = ((Enum<?>) value).ordinal() - ((Enum<?>) keyValue).ordinal(); 
            }
        }
        return result;
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
