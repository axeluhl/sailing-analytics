package com.sap.sailing.racecommittee.app.domain.coursedesign;

import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.tracking.Positioned;

public interface Located extends Positioned {
    Bearing getBearingFrom(Position other);
    Distance getDistanceFromPosition(Position other);
}
