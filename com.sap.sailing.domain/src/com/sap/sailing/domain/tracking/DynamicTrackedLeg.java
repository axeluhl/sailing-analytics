package com.sap.sailing.domain.tracking;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.TimePoint;


public interface DynamicTrackedLeg extends TrackedLeg {
    void completed(Competitor competitor, TimePoint at);
}
