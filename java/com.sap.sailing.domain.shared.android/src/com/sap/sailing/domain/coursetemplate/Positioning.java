package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.Position;

public interface Positioning {
    
    public enum PositioningType {
        FIXED_POSITION, DEVICE
    }
    
    PositioningType getType();
    
    Position getPosition();
}
