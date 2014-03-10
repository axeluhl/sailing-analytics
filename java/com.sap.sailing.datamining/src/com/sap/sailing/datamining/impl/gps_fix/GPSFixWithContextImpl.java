package com.sap.sailing.datamining.impl.gps_fix;

import com.sap.sailing.datamining.Clusters.WindStrength;
import com.sap.sailing.datamining.WindStrengthCluster;
import com.sap.sailing.datamining.data.HasGPSFixContext;
import com.sap.sailing.datamining.data.GPSFixWithContext;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.SpeedWithBearing;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.KnotSpeedWithBearingImpl;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.domain.tracking.impl.GPSFixMovingImpl;

public class GPSFixWithContextImpl extends GPSFixMovingImpl implements GPSFixWithContext {
    private static final long serialVersionUID = -5551381302809417831L;

    private HasGPSFixContext context;
    private Wind wind;

    private boolean windHasBeenInitialized;

    public GPSFixWithContextImpl(GPSFixMoving gpsFix, HasGPSFixContext context) {
        super(copyPosition(gpsFix), copyTimePoint(gpsFix), copySpeed(gpsFix));
        this.context = context;
        
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
    public String getRegattaName() {
        return context.getTrackedRace().getTrackedRegatta().getRegatta().getName();
    }

    @Override
    public String getRaceName() {
        return context.getTrackedRace().getRace().getName();
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
        if (getWind() == null) {
            return null;
        }

        return WindStrength.getClusterFor(getWind().getBeaufort(), WindStrength.StandardClusters);
    }

    public Wind getWind() {
        if (!windHasBeenInitialized) {
            wind = context.getTrackedRace().getWind(getPosition(), getTimePoint());
            windHasBeenInitialized = true;
        }
        
        return wind;
    }

}
