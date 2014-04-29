package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasTrackedLegContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.base.Event;
import com.sap.sailing.domain.base.Fleet;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;
import com.sap.sailing.domain.tracking.TrackedRace;

public class HasTrackedLegContextImpl extends HasTrackedRaceContextImpl implements HasTrackedLegContext {
    
    private final TrackedLeg trackedLeg;
    private final int legNumber;
    private LegType legType;
    private boolean legTypeHasBeenInitialized;

    public HasTrackedLegContextImpl(HasTrackedRaceContext trackedRaceContext, TrackedLeg trackedLeg, int legNumber) {
        this(trackedRaceContext.getEvent(), trackedRaceContext.getRegatta(), trackedRaceContext.getFleet(),
                trackedRaceContext.getTrackedRace(), trackedLeg, legNumber);
    }
    
    public HasTrackedLegContextImpl(Event event, Regatta regatta, Fleet fleet, TrackedRace trackedRace, TrackedLeg trackedLeg, int legNumber) {
        super(event, regatta, fleet, trackedRace);
        this.trackedLeg = trackedLeg;
        this.legNumber = legNumber;
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
