package com.sap.sailing.domain.racelogtracking.impl;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartOfTrackingEventImpl;
import com.sap.sailing.domain.base.Regatta;
import com.sap.sailing.domain.tracking.StartTimeChangedListener;
import com.sap.sailing.domain.tracking.TrackedRace;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;

/**
 * A listener for start time changes that---when {@link Regatta#isControlTrackingFromStartAndFinishTimes() enabled},
 * updates the start of tracking time for a race through the race log whenever the race's start time has been changed.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class StartOfTrackingController implements StartTimeChangedListener {
    private final Regatta regatta;
    private final TrackedRace trackedRace;
    private final RaceLog raceLog;
    private final AbstractLogEventAuthor raceLogEventAuthor;
    
    /**
     * Initially checks the {@code trackedRace} for a {@link TrackedRace#getStartOfRace() start time}. If one is found and
     * no {@link TrackedRace#getStartOfTracking() start of tracking} is defined yet, immediately invokes {@link #startTimeChanged(TimePoint)}
     * which will set a start of tracking time in the race log.
     */
    public StartOfTrackingController(TrackedRace trackedRace, RaceLog raceLog, AbstractLogEventAuthor raceLogEventAuthor) {
        super();
        this.regatta = trackedRace.getTrackedRegatta().getRegatta();
        this.raceLog = raceLog;
        this.trackedRace = trackedRace;
        this.raceLogEventAuthor = raceLogEventAuthor;
        final TimePoint startOfRace = trackedRace.getStartOfRace();
        if (startOfRace != null) {
            startTimeChanged(startOfRace);
        }
    }

    @Override
    public void startTimeChanged(TimePoint newTimePoint) {
        if (regatta.isControlTrackingFromStartAndFinishTimes()) {
            final TimePoint newStartOfTracking = newTimePoint.minus(TrackedRace.START_TRACKING_THIS_MUCH_BEFORE_RACE_START);
            if (!Util.equalsWithNull(trackedRace.getStartOfTracking(), newStartOfTracking)) {
                raceLog.add(new RaceLogStartOfTrackingEventImpl(newStartOfTracking,
                        raceLogEventAuthor, raceLog.getCurrentPassId()));
            }
        }
    }
}
