package com.sap.sailing.domain.polarsheets;

import java.util.Iterator;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.RaceDefinition;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;

public class PerRaceWorker implements Runnable {

    private final TrackedRace race;

    private final PolarSheetGenerationWorker polarSheetGenerationWorker;

    public PerRaceWorker(PolarSheetGenerationWorker polarSheetGenerationWorker, TrackedRace race) {
        this.race = race;
        this.polarSheetGenerationWorker = polarSheetGenerationWorker;
    }

    @Override
    public void run() {
        TimePoint startTime = race.getStartOfRace();
        TimePoint endTime = race.getEndOfRace();
        RaceDefinition raceDefinition = race.getRace();
        Iterable<Competitor> competitors = raceDefinition.getCompetitors();

        for (Competitor competitor : competitors) {
            getPolarDataForCompetitor(startTime, endTime, competitor);
        }

        polarSheetGenerationWorker.workerDone(this);

    }

    private void getPolarDataForCompetitor(final TimePoint startTime, final TimePoint endTime,
            final Competitor competitor) {
        Runnable perCompetitorWorker = new Runnable() {

            @Override
            public void run() {
                GPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(competitor);
                Iterator<GPSFixMoving> fixesIterator = track.getFixesIterator(startTime, true);
                // TODO maneuver exclusion
                // List<Maneuver> maneuvers = race.getManeuvers(competitor, startTime, endTime, false);

                while (fixesIterator.hasNext()) {
                    GPSFixMoving fix = fixesIterator.next();
                    if (fix.getTimePoint().after(endTime)) {
                        break;
                    }

                    SpeedWithBearing speedWithBearing = fix.getSpeed();
                    double speed = speedWithBearing.getMetersPerSecond();
                    Bearing bearing = speedWithBearing.getBearing();
                    Position position = fix.getPosition();
                    Wind wind = race.getWind(position, fix.getTimePoint());
                    Bearing windBearing = wind.getBearing();
                    double windSpeed = wind.getMetersPerSecond();

                    // TODO Figure out if this normalizing is okay concerning different windspeeds and bearings
                    double normalizedSpeed = speed / windSpeed;
                    double angleToWind = bearing.getDifferenceTo(windBearing).getDegrees();

                    polarSheetGenerationWorker.addPolarData(Math.round(angleToWind), normalizedSpeed);
                }
            }
        };

        Thread perCompetitorThread = new Thread(perCompetitorWorker);
        perCompetitorThread.start();

    }
}
