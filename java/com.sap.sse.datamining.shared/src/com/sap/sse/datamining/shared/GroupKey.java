package com.sap.sse.datamining.shared;

import java.io.Serializable;
import java.util.List;


public interface GroupKey extends Serializable, Comparable<GroupKey> {
    
    /**
     * @return a string representation of this group key.
     */
    public String asString();
    
    /**
     * Checks if this group key has at least one sub key. For example if the data was grouped
     * primary by competitor and secondary by leg type.<br>
     * This means, that {@link #getKeys()} returns a {@link List} that contains at least two elements.
     * @return true, if this group key has at least one sub key
     */
    public boolean hasSubKeys();
    
    /**
     * Returns a non-empty {@link List} of group keys that are represented by this key:
     * <ul>
     *   <li>If {@link #hasSubKeys()} of this key <code>false</code>, a singleton List
     *       containing <code>this</code> is returned.
     *   </li>
     *   <li>If {@link #hasSubKeys()} of this key <code>true</code>, an unmodifiable List
     *       containing the represented keys is returned.<br>
     *       The returned keys can again contain sub keys.
     *    </li>
     * </ul>
     * @return the group keys that are represented by this key.
     */
    public List<? extends GroupKey> getKeys();
    
    //Enforce hash code and equals in all subclasses
    @Override
    public abstract boolean equals(Object other);
    @Override
    public abstract int hashCode();
    
    public static class Util {
        
        public static GroupKey getMainKey(GroupKey groupKey) {
            return groupKey.getKeys().get(0);
        }

        /**
         * Returns the list of {@link GroupKey#getKeys() keys} except for the first key
         * in the list. Or <code>null</code>, if there are no sub keys.
         * 
         * @param groupKey
         * @return the sub keys or <code>null</code>, if there aren't any.
         */
        public static List<? extends GroupKey> getSubKeys(GroupKey groupKey) {
            if (!groupKey.hasSubKeys()) {
                return null;
            }

            List<? extends GroupKey> keys = groupKey.getKeys();
            return keys.subList(1, keys.size());
        }
        
    }

}
