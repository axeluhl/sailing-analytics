package com.sap.sailing.domain.racelog.analyzing.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEvent;
import com.sap.sailing.domain.racelog.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.racelog.RaceLogStartTimeEvent;
import com.sap.sailing.domain.racelog.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure2;

public class RaceStatusAnalyzer extends RaceLogAnalyzer<RaceLogRaceStatus> {

    private final EventDispatcher eventDispatcher;
    
    public RaceStatusAnalyzer(RaceLog raceLog, RacingProcedure2 racingProcedure) {
        super(raceLog);
        this.eventDispatcher = new EventDispatcher(racingProcedure);
    }
    
    public RaceStatusAnalyzer(RaceLog raceLog) {
        this(raceLog, null);
    }

    @Override
    protected RaceLogRaceStatus performAnalysis() {
        for (RaceLogEvent event : getPassEventsDescending()) {
            if (event instanceof RaceLogRaceStatusEvent) {
                RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) event;
                statusEvent.accept(eventDispatcher);
                return eventDispatcher.nextStatus;
            }
        }
        
        return RaceLogRaceStatus.UNSCHEDULED;
    }
    
    private class EventDispatcher extends BaseRaceLogEventVisitor {
        
        private final RacingProcedure2 racingProcedure;
        public RaceLogRaceStatus nextStatus;
        
        public EventDispatcher(RacingProcedure2 racingProcedure) {
            this.racingProcedure = racingProcedure;
            this.nextStatus = RaceLogRaceStatus.UNKNOWN;
        }

        @Override
        public void visit(RaceLogStartTimeEvent event) {
            if (racingProcedure == null) {
                visit((RaceLogRaceStatusEvent) event);
            } else {
                TimePoint now = MillisecondsTimePoint.now();
                if (racingProcedure.isStartphaseActive(event.getStartTime(), now)) {
                    nextStatus = RaceLogRaceStatus.STARTPHASE;
                } else if (now.before(event.getStartTime())) {
                    nextStatus = RaceLogRaceStatus.SCHEDULED;
                } else {
                    nextStatus = RaceLogRaceStatus.RUNNING;
                }
            }
        };
        
        @Override
        public void visit(RaceLogRaceStatusEvent event) {
            nextStatus = event.getNextStatus();
        };
        
    };

}
