package com.sap.sailing.domain.common;

import java.io.Serializable;



/**
 * Value-object identifying possible sources for wind data. Used to key and select between different {@link WindTrack}s.
 * Objects of this class have their {@link Object#equals(Object)} and {@link Object#hashCode()} methods defined
 * accordingly.
 * 
 * @author Axel Uhl (d043530)
 * 
 */
public interface WindSource extends Serializable {
    WindSourceType getType();
    
    /**
     * @return {@link #getType()}.{@link WindSourceType#canBeStored() canBeStored()}
     */
    boolean canBeStored();

    /**
     * Usually the name of this wind source's {@link #getType() type}, optionally suffixed by an identifier, e.g., in case
     * there are multiple sources of the same type.
     */
    String name();

    /**
     * An optional ID. May be <code>null</code>. Can be used to distinguish different wind sources of the same type.
     */
    Object getId();
}
