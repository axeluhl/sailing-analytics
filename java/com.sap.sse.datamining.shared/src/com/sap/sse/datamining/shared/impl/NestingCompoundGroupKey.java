package com.sap.sse.datamining.shared.impl;

import com.sap.sse.datamining.shared.GroupKey;


public class NestingCompoundGroupKey extends AbstractGroupKey {

    private static final long serialVersionUID = -7902450253393172550L;
    
    private GroupKey mainKey;
    private GroupKey subKey;

    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    NestingCompoundGroupKey() { }
    
    public NestingCompoundGroupKey(GroupKey mainKey) {
        this(mainKey, null);
    }

    public NestingCompoundGroupKey(GroupKey mainKey, GroupKey subKey) {
        this.mainKey = mainKey;
        this.subKey = subKey;
    }

    @Override
    public String asString() {
        return mainKey.asString() + " (" + subKey.asString() + ")";
    }

    @Override
    public boolean hasSubKey() {
        return subKey != null;
    }
    
    @Override
    public GroupKey getMainKey() {
        return mainKey;
    }

    @Override
    public GroupKey getSubKey() {
        return subKey;
    }
    
    public void setSubKey(GroupKey subKey) {
        this.subKey = subKey;
    }
    
    @Override
    public int compareTo(GroupKey key) {
        int result = super.compareTo(key);
        if (key instanceof NestingCompoundGroupKey) {
            NestingCompoundGroupKey nestingCompoundGroupKey = (NestingCompoundGroupKey) key;
            result = mainKey.compareTo(nestingCompoundGroupKey.mainKey);
            if (result == 0) {
                result = subKey.compareTo(nestingCompoundGroupKey.subKey);
            }
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mainKey == null) ? 0 : mainKey.hashCode());
        result = prime * result + ((subKey == null) ? 0 : subKey.hashCode());
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
        NestingCompoundGroupKey other = (NestingCompoundGroupKey) obj;
        if (mainKey == null) {
            if (other.mainKey != null)
                return false;
        } else if (!mainKey.equals(other.mainKey))
            return false;
        if (subKey == null) {
            if (other.subKey != null)
                return false;
        } else if (!subKey.equals(other.subKey))
            return false;
        return true;
    }

    

}
