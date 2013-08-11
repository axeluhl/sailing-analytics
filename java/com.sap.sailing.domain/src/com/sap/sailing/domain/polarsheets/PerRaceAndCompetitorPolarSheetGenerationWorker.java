package com.sap.sailing.domain.polarsheets;

import java.util.Iterator;
import java.util.NavigableSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sap.sailing.domain.base.Competitor;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.tracking.GPSFixMoving;
import com.sap.sailing.domain.tracking.GPSFixTrack;
import com.sap.sailing.domain.tracking.MarkPassing;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.WindWithConfidence;

/**
 * Iterates through the fixes of one competitor in one tracked race and fills the {@link PolarSheetGenerationWorker}
 * with datapoints that are found for the speed of the boat and its angle to the wind
 * 
 * @author D054528 Frederik Petersen
 * 
 */
public class PerRaceAndCompetitorPolarSheetGenerationWorker implements Runnable{
    
    private static final Logger logger = Logger.getLogger(PerRaceAndCompetitorPolarSheetGenerationWorker.class.getName());

    private final TrackedRace race;

    private final PolarSheetGenerationWorker polarSheetGenerationWorker;
    
    private final OddFixClassifier oddFixClassifier;

    private TimePoint startTime;

    private TimePoint endTime;

    private final Competitor competitor;
    
    private boolean noConfidence = false;

    private boolean done = false;
    
    private int finishedEarlyAtWaypoint = -1;

    public PerRaceAndCompetitorPolarSheetGenerationWorker(TrackedRace race,
            PolarSheetGenerationWorker polarSheetGenerationWorker, TimePoint startTime, TimePoint endTime,
            Competitor competitor) {
        super();
        this.race = race;
        this.polarSheetGenerationWorker = polarSheetGenerationWorker;
        this.startTime = startTime;
        this.endTime = endTime;
        this.competitor = competitor;
        this.oddFixClassifier = new AngleSpeedOddClassifier();
        optimizeStartTime();
        optimizeEndTime();
        checkIfRaceAborted();
    }

    private void checkIfRaceAborted() {
        NavigableSet<MarkPassing> markPassings = race.getMarkPassings(competitor);
        if (markPassings == null || markPassings.size() < 1) {
            return;
        }
        if (markPassings.size() < race.getRace().getCourse().getLegs().size() + 1) {
            finishedEarlyAtWaypoint = markPassings.size() - 1;
        }
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
            noConfidence = true;
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
        if (noConfidence || (finishedEarlyAtWaypoint != -1 && finishedEarlyAtWaypoint < 3)) {
            done = true;
        } else {
            GPSFixTrack<Competitor, GPSFixMoving> track = race.getTrack(competitor);
            track.lockForRead();
            Iterator<GPSFixMoving> fixesIterator = track.getFixesIterator(startTime, true);
            TimePoint lastConsideredTimePoint = null;
            if (finishedEarlyAtWaypoint != -1) {
                NavigableSet<MarkPassing> markPassings = race.getMarkPassings(competitor);
                MarkPassing lastConsideredPassing = markPassings.last();
                lastConsideredPassing = markPassings.lower(lastConsideredPassing);
                lastConsideredTimePoint = lastConsideredPassing.getTimePoint();
            }

            while (fixesIterator.hasNext()) {
                GPSFixMoving fix = fixesIterator.next();
                if (fix.getTimePoint().after(endTime)
                        || (lastConsideredTimePoint != null && fix.getTimePoint().after(lastConsideredTimePoint))) {
                    break;
                }

                if (track.hasDirectionChange(fix.getTimePoint(), race.getRace().getBoatClass()
                        .getManeuverDegreeAngleThreshold())) {
                    continue;
                }

                WindWithConfidence<Pair<Position, TimePoint>> windWithConfidence = race.getWindWithConfidence(
                        fix.getPosition(), fix.getTimePoint());
                if (windWithConfidence.useSpeed() && windWithConfidence.getConfidence() > 0.2) {
                    PolarFix polarFix = new PolarFix(fix, race);

                    if (oddFixClassifier.classifiesAsOdd(polarFix)) {
                        logger.log(Level.INFO, String.format(
                                "Odd point was found for: %1$s, in Race %2$s, at %3$tk:%3$tM:%3$tS",
                                competitor.getName(), race.getRace().getName(), fix.getTimePoint().asDate()));
                        //continue;
                    }

                    polarSheetGenerationWorker.addPolarData(Math.round(polarFix.getAngleToWind()),
                            polarFix.getBoatSpeed(), polarFix.getWindSpeed());
                }
            }

            track.unlockAfterRead();
            done = true;
        }
    }
    
    public boolean isDone() {
        return done;
    }
}
