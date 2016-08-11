package com.sap.sailing.domain.tracking;

import java.io.Serializable;

public interface TrackListener extends Serializable {
    
    /**
     * Listeners can use this to skip their serialization.
     */
    boolean isTransient();

}
