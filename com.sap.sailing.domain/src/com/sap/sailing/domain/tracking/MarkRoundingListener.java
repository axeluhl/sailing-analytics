package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;

public interface MarkRoundingListener {
    void legCompleted(Leg leg, TrackedRace race, Competitor competitor);
}
