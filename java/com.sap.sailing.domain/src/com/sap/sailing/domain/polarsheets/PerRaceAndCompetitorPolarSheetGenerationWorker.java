package com.sap.sailing.domain.polarsheets;

import java.util.Iterator;
import java.util.NavigableSet;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.base.SpeedWithBearing;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.Wind;

/**
 * Iterates through the fixes of one competitor in one tracked race and fills the {@link PolarSheetGenerationWorker}
 * with datapoints that are found for the speed of the boat and its angle to the wind
 * 
 * @author D054528 Frederik Petersen
 * 
 */
public class PerRaceAndCompetitorPolarSheetGenerationWorker implements Runnable{

    private final TrackedRace race;

    private final PolarSheetGenerationWorker polarSheetGenerationWorker;

    private TimePoint startTime;

    private TimePoint endTime;

    private final Competitor competitor;

    private boolean done = false;

    public PerRaceAndCompetitorPolarSheetGenerationWorker(TrackedRace race,
            PolarSheetGenerationWorker polarSheetGenerationWorker, TimePoint startTime, TimePoint endTime,
            Competitor competitor) {
        super();
        this.race = race;
        this.polarSheetGenerationWorker = polarSheetGenerationWorker;
        this.startTime = startTime;
        this.endTime = endTime;
        this.competitor = competitor;
        optimizeStartTime();
        optimizeEndTime();
    }

    private void optimizeEndTime() {
        NavigableSet<MarkPassing> markPassings = race.getMarkPassings(competitor);
        if (markPassings == null || markPassings.size() < 1) {
            return;
        }
        MarkPassing passedFinish = markPassings.last();
        TimePoint passedFinishTimePoint = passedFinish.getTimePoint();
        if (passedFinishTimePoint.before(endTime)) {
            endTime = passedFinishTimePoint;
        }
    }

    private void optimizeStartTime() {
        NavigableSet<MarkPassing> markPassings = race.getMarkPassings(competitor);
        if (markPassings == null || markPassings.size() < 1) {
            return;
        }
        MarkPassing passedStart = markPassings.first();
        TimePoint passedStartTimePoint = passedStart.getTimePoint();
        if (passedStartTimePoint.after(startTime)) {
            startTime = passedStartTimePoint;
        }
    }

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

            if (track.hasDirectionChange(fix.getTimePoint(), race.getRace().getBoatClass()
                    .getManeuverDegreeAngleThreshold())) {
                continue;
            }

            SpeedWithBearing speedWithBearing = fix.getSpeed();
            Bearing bearing = speedWithBearing.getBearing();
            Position position = fix.getPosition();
            Wind wind = race.getWind(position, fix.getTimePoint());
            Bearing windBearing = wind.getFrom();
            double angleToWind = bearing.getDifferenceTo(windBearing).getDegrees();

            polarSheetGenerationWorker.addPolarData(Math.round(angleToWind), speedWithBearing, wind);
        }

        track.unlockAfterRead();
        done = true;
    }
    
    public boolean isDone() {
        return done;
    }
}
