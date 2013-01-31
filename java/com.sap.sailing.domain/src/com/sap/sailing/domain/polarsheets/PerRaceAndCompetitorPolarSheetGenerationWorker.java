package com.sap.sailing.domain.polarsheets;

import java.util.Iterator;
import java.util.concurrent.Callable;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;

public class PerRaceAndCompetitorPolarSheetGenerationWorker implements Callable<Void>{

    private final TrackedRace race;

    private final PolarSheetGenerationWorker polarSheetGenerationWorker;
    
    private final TimePoint startTime;
    
    private final TimePoint endTime;
    
    private final Competitor competitor;
    

    

    public PerRaceAndCompetitorPolarSheetGenerationWorker(TrackedRace race, PolarSheetGenerationWorker polarSheetGenerationWorker,
            TimePoint startTime, TimePoint endTime, Competitor competitor) {
        super();
        this.race = race;
        this.polarSheetGenerationWorker = polarSheetGenerationWorker;
        this.startTime = startTime;
        this.endTime = endTime;
        this.competitor = competitor;
    }

    @Override
    public Void call() throws Exception {
        GPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(competitor);
        track.lockForRead();
        Iterator<GPSFixMoving> fixesIterator = track.getFixesIterator(startTime, true);

        while (fixesIterator.hasNext()) {
            GPSFixMoving fix = fixesIterator.next();
            if (fix.getTimePoint().after(endTime)) {
                break;
            }
            
            if (track.hasDirectionChange(fix.getTimePoint(), race.getRace().getBoatClass().getManeuverDegreeAngleThreshold())) {
                continue;
            }

            SpeedWithBearing speedWithBearing = fix.getSpeed();
            double speed = speedWithBearing.getKnots();
            Bearing bearing = speedWithBearing.getBearing();
            Position position = fix.getPosition();
            Wind wind = race.getWind(position, fix.getTimePoint());
            Bearing windBearing = wind.getFrom();
            double windSpeed = wind.getKnots();

            // TODO Figure out if this normalizing is okay concerning different windspeeds and bearings
            double normalizedSpeed = speed/* / windSpeed*/;
            double angleToWind = bearing.getDifferenceTo(windBearing).getDegrees();

            polarSheetGenerationWorker.addPolarData(Math.round(angleToWind), normalizedSpeed);
        }
        
        track.unlockAfterRead();
        return null;
    }
}
