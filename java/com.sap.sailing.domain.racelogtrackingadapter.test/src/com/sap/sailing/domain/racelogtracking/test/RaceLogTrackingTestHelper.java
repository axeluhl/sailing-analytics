package com.sap.sailing.domain.racelogtracking.test;

import java.util.UUID;

import com.sap.sailing.domain.abstractlog.AbstractLogEventAuthor;
import com.sap.sailing.domain.abstractlog.impl.LogEventAuthorImpl;
import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogEndOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogRaceStatusEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartOfTrackingEventImpl;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogStartTimeEventImpl;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.tracking.DynamicTrackedRace;
import com.sap.sse.common.TimePoint;

public abstract class RaceLogTrackingTestHelper {
    protected RaceLog raceLog;
    protected AbstractLogEventAuthor author = new LogEventAuthorImpl("author", 0);
    
    protected void setStartAndEndOfTrackingInRaceLog(TimePoint startOfTrackingInRacelog, TimePoint endOfTrackingInRacelog) {
        raceLog.add(new RaceLogStartOfTrackingEventImpl(startOfTrackingInRacelog, author, 0));
        raceLog.add(new RaceLogEndOfTrackingEventImpl(endOfTrackingInRacelog, author, 0));
    }
    
    protected void setStartAndEndOfRaceInRaceLog(TimePoint startTimeInRaceLog, TimePoint endTimeInRaceLog) {
        raceLog.add(new RaceLogStartTimeEventImpl(startTimeInRaceLog, author, 0, startTimeInRaceLog));
        raceLog.add(new RaceLogRaceStatusEventImpl(endTimeInRaceLog, endTimeInRaceLog, author, UUID.randomUUID(), 0, RaceLogRaceStatus.FINISHED));
    }
    
    protected void setManualTrackingTimesOnTrackedRace(DynamicTrackedRace trackedRace, TimePoint startTime, TimePoint endTime) {
        trackedRace.setStartOfTrackingReceived(startTime);
        trackedRace.setEndOfTrackingReceived(endTime);
    }
}
