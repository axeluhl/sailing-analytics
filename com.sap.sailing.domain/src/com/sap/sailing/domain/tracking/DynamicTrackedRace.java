package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;

public interface DynamicTrackedRace extends TrackedRace {
    void recordFix(Competitor competitor, GPSFixMoving fix);
}
