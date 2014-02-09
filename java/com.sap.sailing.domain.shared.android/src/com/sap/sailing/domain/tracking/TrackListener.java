package com.sap.sailing.domain.tracking;

import java.io.Serializable;

public interface TrackListener<FixType> extends Serializable {
    void fixReceived(FixType fix);
    
    /**
     * Listeners can use this to skip their serialization.
     */
    boolean isTransient();
}
