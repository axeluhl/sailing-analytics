package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.TimePoint;

public class FinishingTimeFinder extends RaceLogAnalyzer<TimePoint> {

    public FinishingTimeFinder(RaceLog raceLog) {
        super(raceLog);
    }

    public RaceLogRaceStatusEvent findFinishingEvent() {
        log.lockForRead();
        try {
            for (RaceLogEvent event : getPassEventsDescending()) {
                if (event instanceof RaceLogRaceStatusEvent) {
                    RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) event;
                    if (statusEvent.getNextStatus().equals(RaceLogRaceStatus.FINISHING)) {
                        return statusEvent;
                    }
                }
            }
            return null;
        } finally {
            log.unlockAfterRead();
        }
    }

    @Override
    protected TimePoint performAnalysis() {
        for (RaceLogEvent event : log.getUnrevokedEvents()) {
            if (event instanceof RaceLogRaceStatusEvent) {
                RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) event;
                if (statusEvent.getNextStatus().equals(RaceLogRaceStatus.FINISHING)) {
                    return statusEvent.getLogicalTimePoint();
                }
            }
        }

        return null;
    }
}
