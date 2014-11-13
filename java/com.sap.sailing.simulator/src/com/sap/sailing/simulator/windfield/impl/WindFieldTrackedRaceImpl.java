package com.sap.sailing.simulator.windfield.impl;

import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.domain.common.impl.MeterDistance;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;
import com.sap.sailing.simulator.TimedPosition;
import com.sap.sailing.simulator.windfield.WindFieldGenerator;

/**
 * A windfield that can be used for race simulation and is based on the wind measurements & interpolation made available
 * as part of a {@link TrackedRace}. This is base data for visualizing race simulation on the {@link RaceSimulationOverlay}
 * of the {@link RaceMap}
 * 
 * @author Christopher Ronnewinkel (D036654)
 * 
 */
public class WindFieldTrackedRaceImpl extends WindFieldGeneratorImpl implements WindFieldGenerator {

    private static final long serialVersionUID = -7005970781594631010L;
	private static final double EPSILON_DISTANCE_METER = 20;
	private static final long EPSILON_TIME_MILLIS = 5000;
    private TrackedRace race;
    
    public WindFieldTrackedRaceImpl(TrackedRace race) {
        super(null, null);
        this.race = race;
    }

    @Override
    public Wind getWind(TimedPosition timedPosition) {
    	Position pos = timedPosition.getPosition();
    	double epsLat = (new MeterDistance(EPSILON_DISTANCE_METER)).getCentralAngleDeg();
    	double qLat = Math.floor(pos.getLatDeg() / epsLat) * epsLat;
    	double epsLng = epsLat / Math.cos(qLat * Math.PI / 180.0);
    	double qLng = Math.floor(pos.getLngDeg() / epsLng) * epsLng;
    	TimePoint time = timedPosition.getTimePoint();
    	long epsTime = EPSILON_TIME_MILLIS;
    	TimePoint qTime = new MillisecondsTimePoint((long) (Math.floor(time.asMillis() / epsTime) * epsTime));
    	Position qPosition = new DegreePosition(qLat, qLng);
    	Wind wind = this.race.getWind(qPosition, qTime);
        return wind;
    }
    
}
