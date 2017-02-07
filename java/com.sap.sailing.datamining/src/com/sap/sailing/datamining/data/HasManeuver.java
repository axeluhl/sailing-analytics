package com.sap.sailing.datamining.data;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.Positioned;
import com.sap.sailing.domain.tracking.Maneuver;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Timed;

public interface HasManeuver extends Positioned, Timed {
    Maneuver getManeuver();

    @Override
    default TimePoint getTimePoint() {
        return getManeuver().getTimePoint();
    }

    @Override
    default Position getPosition() {
        return getManeuver().getPosition();
    }
}
