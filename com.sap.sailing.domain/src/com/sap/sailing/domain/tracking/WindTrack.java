package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Position;
import com.sap.sailing.domain.base.TimePoint;

public interface WindTrack extends Track<Wind> {
    void add(Wind wind);

    Wind getEstimatedWind(Position p, TimePoint at);
}
