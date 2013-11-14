package com.sap.sailing.domain.racelog.state.racingprocedure;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.state.RaceState2;
import com.sap.sailing.domain.racelog.state.RaceState2ChangedListener;
import com.sap.sailing.domain.racelog.state.RaceStateEventProcessor;
import com.sap.sailing.domain.racelog.state.RaceStateEventScheduler;

public interface RacingProcedure2 extends RaceStateEventProcessor, RaceState2ChangedListener {
    
    RaceLog getRaceLog();
    RacingProcedureType getType();
    
    void addChangedListener(RacingProcedureChangedListener listener);
    void removeChangedListener(RacingProcedureChangedListener listener);
    
    void setStateEventScheduler(RaceStateEventScheduler scheduler);
    void triggerStateEventScheduling(RaceState2 state);
    
    RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint startTime, TimePoint now);
    boolean isStartphaseActive(TimePoint startTime, TimePoint now);
    
    boolean hasIndividualRecall();
    boolean isIndividualRecallDisplayed();
    TimePoint getIndividualRecallRemovalTime();
    void displayIndividualRecall(TimePoint timePoint);
    void removeIndividualRecall(TimePoint timePoint);
    
    FlagPoleState getActiveFlags(TimePoint startTime, TimePoint now);
    
    void detachFromRaceLog();

}
