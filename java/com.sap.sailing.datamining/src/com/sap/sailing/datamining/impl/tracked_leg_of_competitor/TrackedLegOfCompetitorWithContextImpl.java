package com.sap.sailing.datamining.impl.tracked_leg_of_competitor;

import com.sap.sailing.datamining.WindStrengthCluster;
import com.sap.sailing.datamining.data.HasTrackedLegOfCompetitorContext;
import com.sap.sailing.datamining.data.TrackedLegOfCompetitorWithContext;
import com.sap.sailing.domain.common.Distance;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.TrackedLegOfCompetitor;

public class TrackedLegOfCompetitorWithContextImpl implements TrackedLegOfCompetitorWithContext {

    private TrackedLegOfCompetitor trackedLegOfCompetitor;
    private HasTrackedLegOfCompetitorContext context;

    public TrackedLegOfCompetitorWithContextImpl(TrackedLegOfCompetitor trackedLegOfCompetitor, HasTrackedLegOfCompetitorContext context) {
        this.trackedLegOfCompetitor = trackedLegOfCompetitor;
        this.context = context;
    }

    @Override
    public String getRegattaName() {
        return context.getRegatta().getName();
    }

    @Override
    public String getRaceName() {
        return context.getRace().getName();
    }

    @Override
    public int getLegNumber() {
        return context.getLegNumber();
    }

    @Override
    public String getCourseAreaName() {
        return context.getCourseArea().getName();
    }

    @Override
    public String getFleetName() {
        return context.getFleet().getName();
    }

    @Override
    public String getBoatClassName() {
        return context.getCompetitor().getBoat().getBoatClass().getName();
    }

    @Override
    public Integer getYear() {
        return context.getYear();
    }

    @Override
    public LegType getLegType() {
        return context.getLegType();
    }

    @Override
    public String getCompetitorName() {
        return context.getCompetitor().getName();
    }

    @Override
    public String getCompetitorSailID() {
        return context.getCompetitor().getBoat().getSailID();
    }

    @Override
    public String getCompetitorNationality() {
        return context.getCompetitor().getTeam().getNationality().getThreeLetterIOCAcronym();
    }

    @Override
    public WindStrengthCluster getWindStrength() {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public Distance getDistanceTraveled() {
        TimePoint finishTime = trackedLegOfCompetitor.getFinishTime();
        return finishTime != null ? trackedLegOfCompetitor.getDistanceTraveled(finishTime) : null;
    }

}
