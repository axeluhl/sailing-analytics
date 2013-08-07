package com.sap.sailing.datamining.impl;

import java.util.Calendar;

import com.sap.sailing.datamining.GPSFixContext;
import com.sap.sailing.datamining.GPSFixWithContext;
import com.sap.sailing.datamining.shared.Dimension;
import com.sap.sailing.datamining.shared.WindStrength;
import com.sap.sailing.domain.base.BoatClass;
import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.Nationality;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.base.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.base.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.NoWindException;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class GPSFixWithContextImpl extends GPSFixMovingImpl implements GPSFixWithContext {
    private static final long serialVersionUID = -5551381302809417831L;

    private GPSFixContext context;
    private LegType legType;
    private Wind wind;

    private boolean legTypeHasBeenInitialized;
    private boolean windHasBeenInitialized;

    public GPSFixWithContextImpl(GPSFixMoving gpsFix, GPSFixContext context) {
        super(copyPosition(gpsFix), copyTimePoint(gpsFix), copySpeed(gpsFix));
        this.context = context;
        
        legTypeHasBeenInitialized = false;
        windHasBeenInitialized= false;
    }

    private static SpeedWithBearing copySpeed(GPSFixMoving gpsFix) {
        return new KnotSpeedWithBearingImpl(gpsFix.getSpeed().getKnots(), gpsFix.getSpeed().getBearing());
    }

    private static TimePoint copyTimePoint(GPSFixMoving gpsFix) {
        return new MillisecondsTimePoint(gpsFix.getTimePoint().asMillis());
    }

    private static Position copyPosition(GPSFixMoving gpsFix) {
        return new DegreePosition(gpsFix.getPosition().getLatDeg(), gpsFix.getPosition().getLngDeg());
    }

    @Override
    public Competitor getCompetitor() {
        return context.getCompetitor();
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
        return context.getTrackedRace().getRace();
    }

    @Override
    public Regatta getRegatta() {
        return context.getTrackedRace().getTrackedRegatta().getRegatta();
    }

    @Override
    public int getLegNumber() {
        return context.getLegNumber();
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

    @Override
    public Integer getYear() {
        TimePoint time = context.getTrackedRace().getStartOfRace() != null ? context.getTrackedRace().getStartOfRace() : context.getTrackedRace().getStartOfTracking();
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

    private Wind getWind() {
        if (!windHasBeenInitialized) {
            wind = this.context.getTrackedRace().getWind(getPosition(), getTimePoint());
            windHasBeenInitialized = true;
        }
        
        return wind;
    }

    private void initializeLegType() {
        try {
            legType = context.getTrackedLeg() == null ? null : context.getTrackedLeg().getLegType(getTimePoint());
        } catch (NoWindException e) {
            legType = null;
        }
        legTypeHasBeenInitialized = true;
    }

}
