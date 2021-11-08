package com.sap.sailing.domain.common.orc;

import com.sap.sse.common.Speed;

public interface FixedSpeedImpliedWind extends ImpliedWindSource {
    @Override
    default <T> T accept(ImpliedWindSourceVisitor<T> visitor) {
        return visitor.visit(this);
    }
    
    Speed getFixedImpliedWindSpeed();
}
