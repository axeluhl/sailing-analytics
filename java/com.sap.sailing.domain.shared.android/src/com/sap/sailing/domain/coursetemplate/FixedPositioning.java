package com.sap.sailing.domain.coursetemplate;

import com.sap.sailing.domain.common.Position;

public interface FixedPositioning extends Positioning {
    Position getFixedPosition();
    default <T> T accept(PositioningVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
