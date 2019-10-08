package com.sap.sailing.domain.coursetemplate.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.coursetemplate.FixedPositioning;

public class FixedPositioningImpl extends AbstractPositioningImpl implements FixedPositioning {
    
    public FixedPositioningImpl(Position position) {
        super(PositioningType.FIXED_POSITION, position, null);
    }
}
