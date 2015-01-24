package com.sap.sailing.domain.abstractlog.race.analyzing.impl;

import com.sap.sailing.domain.abstractlog.race.RaceLog;
import com.sap.sailing.domain.abstractlog.race.RaceLogEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogRaceStatusEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogStartTimeEvent;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.impl.RaceLogRaceStatusEventComparator;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sse.common.ArrayListNavigableSet;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceStatusAnalyzer extends RaceLogAnalyzer<RaceLogRaceStatus> {
    
    public interface Clock {
        TimePoint now();
    }
    
    public final static class StandardClock implements Clock {
        @Override
        public TimePoint now() {
            return MillisecondsTimePoint.now();
        }
        
    }
    
    private final EventDispatcher eventDispatcher;
    
    public RaceStatusAnalyzer(RaceLog raceLog, ReadonlyRacingProcedure racingProcedure) {
        super(raceLog);
        this.eventDispatcher = new EventDispatcher(new StandardClock(), racingProcedure);
    }
    
    public RaceStatusAnalyzer(RaceLog raceLog, Clock clock, ReadonlyRacingProcedure racingProcedure) {
        super(raceLog);
        this.eventDispatcher = new EventDispatcher(clock, racingProcedure);
    }

    @Override
    protected RaceLogRaceStatus performAnalysis() {
        ArrayListNavigableSet<RaceLogRaceStatusEvent> statusEvents = new ArrayListNavigableSet<RaceLogRaceStatusEvent>(RaceLogRaceStatusEventComparator.INSTANCE);
        for(RaceLogEvent event: getPassEvents()) {
            if(event instanceof RaceLogRaceStatusEvent) {
                statusEvents.add((RaceLogRaceStatusEvent) event);
            }
        }
        for (RaceLogRaceStatusEvent event : statusEvents.descendingSet()) {
            RaceLogRaceStatusEvent statusEvent = (RaceLogRaceStatusEvent) event;
            statusEvent.accept(eventDispatcher);
            return eventDispatcher.nextStatus;
        }
        
        return RaceLogRaceStatus.UNSCHEDULED;
    }
    
    private class EventDispatcher extends BaseRaceLogEventVisitor {

        private final Clock clock;
        private final ReadonlyRacingProcedure racingProcedure;
        public RaceLogRaceStatus nextStatus;
        
        public EventDispatcher(Clock clock, ReadonlyRacingProcedure racingProcedure) {
            this.clock = clock;
            this.racingProcedure = racingProcedure;
            this.nextStatus = RaceLogRaceStatus.UNKNOWN;
        }

        @Override
        public void visit(RaceLogStartTimeEvent event) {
            TimePoint now = clock.now();
            if (racingProcedure.isStartphaseActive(event.getStartTime(), now)) {
                nextStatus = RaceLogRaceStatus.STARTPHASE;
            } else if (now.before(event.getStartTime())) {
                nextStatus = RaceLogRaceStatus.SCHEDULED;
            } else {
                nextStatus = RaceLogRaceStatus.RUNNING;
            }
        };
        
        @Override
        public void visit(RaceLogRaceStatusEvent event) {
            nextStatus = event.getNextStatus();
        };
        
    };

}
