package com.sap.sailing.domain.polarsheets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
        if (endTime == null) {
            //TODO Figure out if there is an alternative:
            endTime = race.getTimePointOfNewestEvent();
        }
        RaceDefinition raceDefinition = race.getRace();
        Iterable<Competitor> competitors = raceDefinition.getCompetitors();
        
        final List<Thread> perCompetitorWorkers = new ArrayList<Thread>();

        
        for (Competitor competitor : competitors) {
            Thread competitorWorkerThread = getPerCompetitorWorkerThread(startTime, endTime, competitor);
            competitorWorkerThread.start();
            perCompetitorWorkers.add(competitorWorkerThread);
        }
        
        final Timer timer = new Timer();
        
        TimerTask threadMonitoringTask = new TimerTask() {
            
            @Override
            public void run() {
                boolean perCompetitorWorkersDone = false;
                do {
                    boolean currentWorkerAlive = true;
                    Thread currentWorker = null;
                    for (Thread worker : perCompetitorWorkers) {
                        currentWorker = worker;
                        if (!worker.isAlive()) {
                            currentWorkerAlive = false;
                        }
                    }
                    if (!currentWorkerAlive) {
                        perCompetitorWorkers.remove(currentWorker);
                        if (perCompetitorWorkers.isEmpty()) {
                            perCompetitorWorkersDone = true;
                        }
                    }
                } while (!perCompetitorWorkersDone);

                polarSheetGenerationWorker.workerDone(PerRaceWorker.this);
                timer.cancel();
            }
        };
        
        timer.schedule(threadMonitoringTask, 500, 500);
        
        

    }

    private Thread getPerCompetitorWorkerThread(final TimePoint startTime, final TimePoint endTime,
            final Competitor competitor) {
        Runnable perCompetitorWorker = new Runnable() {

            @Override
            public void run() {
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
                    double speed = speedWithBearing.getMetersPerSecond();
                    Bearing bearing = speedWithBearing.getBearing();
                    Position position = fix.getPosition();
                    Wind wind = race.getWind(position, fix.getTimePoint());
                    Bearing windBearing = wind.getFrom();
                    double windSpeed = wind.getMetersPerSecond();

                    // TODO Figure out if this normalizing is okay concerning different windspeeds and bearings
                    double normalizedSpeed = speed / windSpeed;
                    double angleToWind = bearing.getDifferenceTo(windBearing).getDegrees();

                    polarSheetGenerationWorker.addPolarData(Math.round(angleToWind), normalizedSpeed);
                }
                
                track.unlockAfterRead();
            }
        };

        Thread perCompetitorThread = new Thread(perCompetitorWorker);
        return perCompetitorThread;

    }
}
