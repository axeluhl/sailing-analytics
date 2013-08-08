package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.shared.GroupKey;

public class StringGroupKey extends AbstractGroupKey {
    
    private String value;

    public StringGroupKey(String value) {
        this.value = value;
    }

    @Override
    public String asString() {
        return value;
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
        StringGroupKey other = (StringGroupKey) obj;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
