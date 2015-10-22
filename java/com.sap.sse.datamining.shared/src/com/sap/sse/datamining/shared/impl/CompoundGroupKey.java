package com.sap.sse.datamining.shared.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sse.datamining.shared.GroupKey;


public class CompoundGroupKey extends AbstractGroupKey {

    private static final long serialVersionUID = -7902450253393172550L;
    
    private GroupKey mainKey;
    private ArrayList<GroupKey> subKeys;

    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    CompoundGroupKey() { }

    public CompoundGroupKey(GroupKey mainKey, List<GroupKey> subKeys) {
        if (mainKey == null) {
            throw new IllegalArgumentException("The mainKey mustn't be null");
        }
        
        this.mainKey = mainKey;
        this.subKeys = new ArrayList<>(subKeys);
    }

    @Override
    public String asString() {
        StringBuilder builder = new StringBuilder(mainKey.asString());
        for (GroupKey groupKey : subKeys) {
            builder.append("(" + groupKey.asString());
        }
        for (int i = 0; i < subKeys.size(); i++) {
            builder.append(")");
        }
        return builder.toString();
    }

    @Override
    public boolean hasSubKeys() {
        return !subKeys.isEmpty();
    }
    
    @Override
    public GroupKey getMainKey() {
        return mainKey;
    }

    @Override
    public List<GroupKey> getSubKeys() {
        return Collections.unmodifiableList(subKeys);
    }
    
    @Override
    public int compareTo(GroupKey key) {
        int result;
        if (key instanceof CompoundGroupKey) {
            CompoundGroupKey compoundGroupKey = (CompoundGroupKey) key;
            result = mainKey.compareTo(compoundGroupKey.mainKey);
            if (result == 0) {
                result = Integer.compare(subKeys.size(), compoundGroupKey.subKeys.size());
                if (result == 0) {
                    for (int i = 0; i < subKeys.size(); i++) {
                        result = subKeys.get(i).compareTo(compoundGroupKey.subKeys.get(i));
                        if (result != 0) {
                            break;
                        }
                    }
                }
            }
        } else {
            result = super.compareTo(key);
        }
        return result;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((mainKey == null) ? 0 : mainKey.hashCode());
        result = prime * result + ((subKeys == null) ? 0 : subKeys.hashCode());
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
        CompoundGroupKey other = (CompoundGroupKey) obj;
        if (mainKey == null) {
            if (other.mainKey != null)
                return false;
        } else if (!mainKey.equals(other.mainKey))
            return false;
        if (subKeys == null) {
            if (other.subKeys != null)
                return false;
        } else if (!subKeys.equals(other.subKeys))
            return false;
        return true;
    }

}
