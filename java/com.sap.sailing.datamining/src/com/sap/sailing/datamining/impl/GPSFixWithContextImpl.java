package com.sap.sailing.datamining.impl;

import java.util.Calendar;

import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.shared.Dimension;
import com.sap.sailing.datamining.shared.WindStrength;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Leg;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.TrackedLeg;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class GPSFixWithContextImpl extends GPSFixMovingImpl implements GPSFixWithContext {
    private static final long serialVersionUID = -5551381302809417831L;
    
    private TrackedRace trackedRace;
    private Leg leg;
    private int legNumber;
    private LegType legType;
    private Competitor competitor;
    private Wind wind;

    private boolean legTypeHasBeenInitialized;
    private boolean windHasBeenInitialized;

    public GPSFixWithContextImpl(GPSFixMoving gpsFix, TrackedRace trackedRace, Leg leg, int legNumber, Competitor competitor) {
        super(gpsFix.getPosition(), gpsFix.getTimePoint(), gpsFix.getSpeed());
        this.trackedRace = trackedRace;
        this.legNumber = legNumber;
        this.competitor = competitor;
        this.leg = leg;
        
        legTypeHasBeenInitialized = false;
        windHasBeenInitialized= false;
    }

    @Override
    public Competitor getCompetitor() {
        return competitor;
    }

    @Override
    public LegType getLegType() {
        if (!legTypeHasBeenInitialized) {
            initializeLegType();
        }
        
        return legType;
    }

    @Override
    public RaceDefinition getRace() {
        return trackedRace.getRace();
    }

    @Override
    public Regatta getRegatta() {
        return trackedRace.getTrackedRegatta().getRegatta();
    }

    @Override
    public int getLegNumber() {
        return legNumber;
    }

    @Override
    public BoatClass getBoatClass() {
        return getCompetitor().getBoat().getBoatClass();
    }

    @Override
    public Nationality getNationality() {
        return getCompetitor().getTeam().getNationality();
    }

    @Override
    public WindStrength getWindStrength() {
        if (getWind() == null) {
            return null;
        }
        
        return WindStrength.valueOf(getWind().getBeaufort());
    }

    private Wind getWind() {
        if (!windHasBeenInitialized) {
            wind = this.trackedRace.getWind(getPosition(), getTimePoint());
            windHasBeenInitialized = true;
        }
        
        return wind;
    }

    @Override
    public Integer getYear() {
        TimePoint time = trackedRace.getStartOfRace() != null ? trackedRace.getStartOfRace() : trackedRace.getStartOfTracking();
        if (time == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(time.asDate());
        return calendar.get(Calendar.YEAR);
    }

    @Override
    public String getStringRepresentation(Dimension dimension) {
        switch (dimension) {
        case BoatClassName:
            return getBoatClass().getName();
        case CompetitorName:
            return getCompetitor().getName();
        case LegNumber:
            return getLegNumber() + "";
        case LegType:
            LegType legType = getLegType();
            return legType != null ? legType.toString() : null;
        case Nationality:
            return getNationality().getThreeLetterIOCAcronym();
        case RaceName:
            return getRace().getName();
        case RegattaName:
            return getRegatta().getName();
        case SailID:
            return getCompetitor().getBoat().getSailID();
        case WindStrength:
            WindStrength windStrength = getWindStrength();
            return windStrength != null ? windStrength.toString() : null;
        case Year:
            Integer year = getYear();
            return year != null ? year.toString() : null;
        }
        return null;
    }

    private void initializeLegType() {
        TrackedLeg trackedLeg = trackedRace.getTrackedLeg(leg);
        try {
            legType = trackedLeg == null ? null : trackedLeg.getLegType(getTimePoint());
        } catch (NoWindException e) {
            legType = null;
        }
        legTypeHasBeenInitialized = true;
    }

}
