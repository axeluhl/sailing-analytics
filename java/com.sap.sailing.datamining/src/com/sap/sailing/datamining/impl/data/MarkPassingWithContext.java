package com.sap.sailing.datamining.impl.data;

import com.sap.sailing.datamining.data.HasMarkPassingContext;
import com.sap.sailing.datamining.data.HasTrackedRaceContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Waypoint;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;

public class MarkPassingWithContext implements HasMarkPassingContext {
    private final HasTrackedRaceContext trackedRaceContext;
    private final MarkPassing markPassing;

    public MarkPassingWithContext(HasTrackedRaceContext trackedRaceContext, MarkPassing markPassing) {
        super();
        this.trackedRaceContext = trackedRaceContext;
        this.markPassing = markPassing;
    }

    @Override
    public HasTrackedRaceContext getTrackedRaceContext() {
        return trackedRaceContext;
    }

    @Override
    public MarkPassing getMarkPassing() {
        return markPassing;
    }

    @Override
    public Competitor getCompetitor() {
        return markPassing.getCompetitor();
    }

    @Override
    public Speed getSpeed() {
        return getTrackedRace().getTrack(getCompetitor()).getEstimatedSpeed(getMarkPassing().getTimePoint());
    }

    @Override
    public Speed getSpeedTenSecondsBefore() {
        return getTrackedRace().getTrack(getCompetitor()).getEstimatedSpeed(getMarkPassing().getTimePoint().minus(10000 /* ms */));
    }

    private TrackedRace getTrackedRace() {
        return trackedRaceContext.getTrackedRace();
    }
    
    @Override
    public String getPreviousLegTypeSignifier() throws NoWindException {
        final String result;
        TrackedLeg previousLeg = getPreviousLeg();
        if (previousLeg == null) {
            result = "Start";
        } else {
            result = previousLeg.getLegType(getMarkPassing().getTimePoint()).toString();
        }
        return result;
    }

    private TrackedLeg getPreviousLeg() {
        final TrackedLeg result;
        Waypoint waypoint = markPassing.getWaypoint();
        if (waypoint == getTrackedRace().getRace().getCourse().getFirstWaypoint()) {
            result = null;
        } else {
            result = getTrackedRace().getTrackedLegFinishingAt(waypoint);
        }
        return result;
    }
}
