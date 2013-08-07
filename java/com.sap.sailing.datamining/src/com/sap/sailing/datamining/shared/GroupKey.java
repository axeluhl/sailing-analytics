package com.sap.sailing.datamining.shared;


public interface GroupKey {
    
    /**
     * @return a string representation of this group key.
     */
    public String asString();
    
    /**
     * Checks if this group key has a sub key. For example if the data was grouped primary by competitor and secondary by leg type.
     * @return true, if this group key has a sub key
     */
    public boolean hasSubKey();
    
    /**
     * @return the sub key or null, if there's none.
     */
    public GroupKey getSubKey();

}
