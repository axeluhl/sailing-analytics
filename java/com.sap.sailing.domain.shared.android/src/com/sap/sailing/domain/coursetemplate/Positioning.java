package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.Position;

public interface Positioning {
    
    public enum PositioningType {
        Fixed, Device
    }
    
    PositioningType getType();
    
    Position getPosition();
}
