package com.sap.sailing.datamining.impl;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class GPSFixWithContextImpl extends GPSFixMovingImpl implements GPSFixWithContext {
    private static final long serialVersionUID = -5551381302809417831L;
    
    private TrackedRace trackedRace;
    private int legNumber;
    private LegType legType;
    private Competitor competitor;
    private Wind wind;

    public GPSFixWithContextImpl(GPSFixMoving gpsFix, TrackedRace trackedRace, Leg leg, int legNumber, Competitor competitor) {
        super(gpsFix.getPosition(), gpsFix.getTimePoint(), gpsFix.getSpeed());
        this.trackedRace = trackedRace;
        this.legNumber = legNumber;
        this.competitor = competitor;
        this.wind = getTrackedRace().getWind(getPosition(), getTimePoint());
        setLegType(leg);
    }

    private void setLegType(Leg leg) {
        TrackedLeg trackedLeg = getTrackedRace().getTrackedLeg(leg);
        try {
            legType = trackedLeg == null ? null : trackedLeg.getLegType(getTimePoint());
        } catch (NoWindException e) {
            legType = null;
        }
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public LegType getLegType() {
        return legType;
    }

    @Override
    public TrackedRace getTrackedRace() {
        return trackedRace;
    }

    @Override
    public Regatta getRegatta() {
        return getTrackedRace().getTrackedRegatta().getRegatta();
    }

    @Override
    public int getLegNumber() {
        return legNumber;
    }

    @Override
    public Wind getWind() {
        return wind;
    }

}
