package com.sap.sailing.domain.tracking.impl;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.tracking.DynamicTrackedLeg;
import com.sap.sailing.domain.tracking.MarkPassing;

public class DynamicTrackedLegImpl extends TrackedLegImpl implements DynamicTrackedLeg {
    
    public DynamicTrackedLegImpl(TrackedRaceImpl trackedRace, Leg leg, Iterable<Competitor> competitors) {
        super(trackedRace, leg, competitors);
    }

    @Override
    public void completed(MarkPassing passing) {
        // TODO implement DynamicTrackedLegImpl.completed
    }

}
