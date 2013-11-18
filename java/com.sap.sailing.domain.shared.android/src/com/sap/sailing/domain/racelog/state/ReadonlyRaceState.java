package com.sap.sailing.domain.racelog.state;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;

public interface ReadonlyRaceState extends RaceStateEventProcessor{

    RaceLog getRaceLog();
    
    ReadonlyRacingProcedure getRacingProcedure();
    <T extends ReadonlyRacingProcedure> T getTypedReadonlyRacingProcedure();
    <T extends ReadonlyRacingProcedure> T getTypedReadonlyRacingProcedure(Class<T> clazz);
    
    void setStateEventScheduler(RaceStateEventScheduler scheduler);
    
    void addChangedListener(RaceStateChangedListener listener);
    void removeChangedListener(RaceStateChangedListener listener);
    
    RaceLogRaceStatus getStatus();
    
    TimePoint getStartTime();
    TimePoint getFinishingTime();
    TimePoint getFinishedTime();
    TimePoint getProtestTime();
    CompetitorResults getFinishPositioningList();
    boolean isFinishPositioningConfirmed();
    CourseBase getCourseDesign();
}
