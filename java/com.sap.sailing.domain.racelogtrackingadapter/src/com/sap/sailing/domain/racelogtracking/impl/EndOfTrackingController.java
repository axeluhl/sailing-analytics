package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEndOfTrackingEventImpl;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sailing.domain.tracking.impl.AbstractRaceChangeListener;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * A listener for finished time changes that---when {@link Regatta#isControlTrackingFromStartAndFinishTimes() enabled},
 * updates the end of tracking time for a race through the race log whenever the race's finished time has been changed,
 * e.g., by a blue flag down event on the race log.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class EndOfTrackingController extends AbstractRaceChangeListener {
    private final Regatta regatta;
    private final TrackedRace trackedRace;
    private final RaceLog raceLog;
    private final AbstractLogEventAuthor raceLogEventAuthor;

    
    /**
     * Initially checks the {@code trackedRace} for a {@link TrackedRace#getFinishedTime() finished time}. If one is found and
     * no {@link TrackedRace#getEndOfTracking() end of tracking} is defined yet, immediately invokes {@link #finishedTimeChanged(TimePoint, TimePoint)}
     * which will set an end of tracking time in the race log.
     */
    public EndOfTrackingController(TrackedRace trackedRace, RaceLog raceLog, AbstractLogEventAuthor raceLogEventAuthor) {
        super();
        this.regatta = trackedRace.getTrackedRegatta().getRegatta();
        this.trackedRace = trackedRace;
        this.raceLog = raceLog;
        this.raceLogEventAuthor = raceLogEventAuthor;
        final TimePoint finishedTime = trackedRace.getFinishedTime();
        if (finishedTime != null) {
            finishedTimeChanged(/* old finished time */ null, finishedTime);
        }
    }

    @Override
    public void finishedTimeChanged(TimePoint oldFinishedTime, TimePoint newFinishedTime) {
        if (regatta.isControlTrackingFromStartAndFinishTimes()) {
            final TimePoint newEndOfTracking = newFinishedTime.plus(TrackedRace.STOP_TRACKING_THIS_MUCH_AFTER_RACE_FINISH);
            if (!Util.equalsWithNull(trackedRace.getEndOfTracking(), newEndOfTracking)) {
                raceLog.add(new RaceLogEndOfTrackingEventImpl(newEndOfTracking,
                        raceLogEventAuthor, raceLog.getCurrentPassId()));
            }
        }
    }
}
