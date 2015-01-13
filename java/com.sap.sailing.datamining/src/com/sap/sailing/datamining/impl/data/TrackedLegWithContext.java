package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class TrackedLegWithContext implements HasTrackedLegContext {

    private final HasTrackedRaceContext trackedRaceContext;
    
    private final TrackedLeg trackedLeg;
    private final int legNumber;
    private LegType legType;
    private boolean legTypeHasBeenInitialized;

    public TrackedLegWithContext(HasTrackedRaceContext trackedRaceContext, TrackedLeg trackedLeg, int legNumber) {
        this.trackedRaceContext = trackedRaceContext;
        this.trackedLeg = trackedLeg;
        this.legNumber = legNumber;
    }

    @Override
    public HasTrackedRaceContext getTrackedRaceContext() {
        return trackedRaceContext;
    }
    
    @Override
    public TrackedLeg getTrackedLeg() {
        return trackedLeg;
    }

    @Override
    public LegType getLegType() {
        if (!legTypeHasBeenInitialized) {
            initializeLegType();
        }
        return legType;
    }

    @Override
    public int getLegNumber() {
        return legNumber;
    }

    private void initializeLegType() {
        try {
            legType = getTrackedLeg() == null ? null : getTrackedLeg().getLegType(getTimePointForLegType());
        } catch (NoWindException e) {
            legType = null;
        }
        legTypeHasBeenInitialized = true;
    }

    private TimePoint getTimePointForLegType() {
        TimePoint at = null;
        for (TrackedLegOfCompetitor trackedLegOfCompetitor : getTrackedLeg().getTrackedLegsOfCompetitors()) {
            TimePoint start = trackedLegOfCompetitor.getStartTime();
            TimePoint finish = trackedLegOfCompetitor.getFinishTime();
            if (start != null && finish != null) {
                at = new MillisecondsTimePoint((start.asMillis() + finish.asMillis()) / 2);
                break;
            }
        }
        return at;
    }

}
