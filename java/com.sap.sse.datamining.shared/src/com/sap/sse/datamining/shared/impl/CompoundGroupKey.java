package com.sap.sse.datamining.shared.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sap.sse.datamining.shared.GroupKey;


public class CompoundGroupKey extends AbstractGroupKey {

    private static final long serialVersionUID = -7902450253393172550L;
    
    private List<? extends GroupKey> keys; // non-final only due to GWT serialization; custom field serializer would change this
    private boolean hasSubKeys; // non-final only due to GWT serialization; custom field serializer would change this

    /**
     * Constructor for the GWT-Serialization. Don't use this!
     */
    @Deprecated
    CompoundGroupKey() { }

    public CompoundGroupKey(List<? extends GroupKey> keys) {
        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException("The keys mustn't be null or empty");
        }
        this.keys = new ArrayList<>(keys);
        hasSubKeys = this.keys.size() > 1;
    }

    @Override
    public String asString() {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        int bracketCount = 0;
        for (GroupKey groupKey : keys) {
            if (!first) {
                builder.append('(');
                bracketCount++;
            }
            builder.append(groupKey.asString());
            first = false;
        }
        for (int i = 0; i < bracketCount; i++) {
            builder.append(")");
        }
        return builder.toString();
    }

    @Override
    public boolean hasSubKeys() {
        return hasSubKeys;
    }
    
    @Override
    public int size() {
        int size = 0;
        for (GroupKey key : keys) {
            size += key.size();
        }
        return size;
    }
    
    @Override
    public List<? extends GroupKey> getKeys() {
        if (hasSubKeys) {
            return Collections.unmodifiableList(keys);
        } else {
            return Collections.singletonList(keys.get(0));
        }
    }
    
    @Override
    public int compareTo(GroupKey key) {
        int result;
        if (key instanceof CompoundGroupKey) {
            CompoundGroupKey compoundGroupKey = (CompoundGroupKey) key;
            result = Integer.compare(keys.size(), compoundGroupKey.keys.size());
            if (result == 0) {
                for (int i = 0; i < keys.size(); i++) {
                    result = keys.get(i).compareTo(compoundGroupKey.keys.get(i));
                    if (result != 0) {
                        break;
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
        result = prime * result + (hasSubKeys ? 1231 : 1237);
        result = prime * result + ((keys == null) ? 0 : keys.hashCode());
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
        if (hasSubKeys != other.hasSubKeys)
            return false;
        if (keys == null) {
            if (other.keys != null)
                return false;
        } else if (!keys.equals(other.keys))
            return false;
        return true;
    }

}
