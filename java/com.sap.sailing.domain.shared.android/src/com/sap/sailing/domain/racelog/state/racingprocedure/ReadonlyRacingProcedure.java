package com.sap.sailing.domain.racelog.state.racingprocedure;

import com.sap.sailing.domain.base.configuration.RacingProcedureConfiguration;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.state.RaceStateChangedListener;
import com.sap.sailing.domain.racelog.state.RaceStateEventProcessor;
import com.sap.sailing.domain.racelog.state.RaceStateEventScheduler;
import com.sap.sailing.domain.racelog.state.ReadonlyRaceState;

public interface ReadonlyRacingProcedure extends RaceStateEventProcessor, RaceStateChangedListener {
    RaceLog getRaceLog();
    RacingProcedureType getType();
    RacingProcedureConfiguration getConfiguration();
    
    void addChangedListener(RacingProcedureChangedListener listener);
    void removeChangedListener(RacingProcedureChangedListener listener);
    
    void setStateEventScheduler(RaceStateEventScheduler scheduler);
    void triggerStateEventScheduling(ReadonlyRaceState state);
    
    RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint startTime, TimePoint now);
    boolean isStartphaseActive(TimePoint startTime, TimePoint now);
    
    boolean hasIndividualRecall();
    boolean isIndividualRecallDisplayed();
    TimePoint getIndividualRecallRemovalTime();
    
    FlagPoleState getActiveFlags(TimePoint startTime, TimePoint now);
    
    void detach();
}
