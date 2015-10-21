package com.sap.sse.datamining.shared;

import java.io.Serializable;
import java.util.List;


public interface GroupKey extends Serializable, Comparable<GroupKey> {
    
    /**
     * @return a string representation of this group key.
     */
    public String asString();
    
    /**
     * Checks if this group key has a sub key. For example if the data was grouped primary by competitor and secondary by leg type.
     * @return true, if this group key has a sub key
     */
    public boolean hasSubKeys();

    /**
     * @return the main key, if there is one. Otherwise it returns <code>this</code>. 
     */
    public GroupKey getMainKey();
    
    /**
     * @return the sub keys or an empty list, if there are none.
     */
    public List<GroupKey> getSubKeys();
    
    //Enforce hash code and equals in all subclasses
    @Override
    public abstract boolean equals(Object other);
    @Override
    public abstract int hashCode();

}
