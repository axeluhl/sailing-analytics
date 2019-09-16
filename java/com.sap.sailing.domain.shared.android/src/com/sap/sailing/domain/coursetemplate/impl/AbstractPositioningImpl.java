package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.Positioning;

public abstract class AbstractPositioningImpl implements Positioning {
    
    private final PositioningType type;
    private final Position position;

    public AbstractPositioningImpl(PositioningType type, Position position) {
        this.type = type;
        this.position = position;
    }

    @Override
    public PositioningType getType() {
        return type;
    }

    @Override
    public Position getPosition() {
        return position;
    }
}
