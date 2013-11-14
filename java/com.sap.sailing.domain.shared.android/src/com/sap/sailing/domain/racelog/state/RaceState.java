package com.sap.sailing.domain.racelog.state;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.tracking.Wind;

public interface RaceState extends RaceStateEventProcessor {
    
    RaceLog getRaceLog();
    RaceLogEventAuthor getAuthor();
    
    void setRacingProcedure(RacingProcedureType racingProcedureType);
    RacingProcedure getRacingProcedure();
    <T extends RacingProcedure> T getTypedRacingProcedure();
    void setStateEventScheduler(RaceStateEventScheduler scheduler);
    
    RaceLogRaceStatus getStatus();
    
    RacingProcedurePrerequisite setStartTime(TimePoint timePoint);
    TimePoint getStartTime();
    void setFinishingTime(TimePoint timePoint);
    TimePoint getFinishingTime();
    void setFinishedTime(TimePoint timePoint);
    TimePoint getFinishedTime();
    void setProtestTime(TimePoint timePoint);
    TimePoint getProtestTime();
    void setAdvancePass(TimePoint timePoint);
    void setAborted(TimePoint timePoint, boolean isPostponed, Flags abortFlag);
    void setGeneralRecall(TimePoint timePoint);
    
    void setFinishPositioningListChanged(TimePoint timePoint, CompetitorResults positionedCompetitors);
    CompetitorResults getFinishPositioningList();
    
    void setFinishPositioningConfirmed(TimePoint timePoint);
    boolean isFinishPositioningConfirmed();
    
    void setCourseDesign(CourseBase courseDesign);
    CourseBase getCourseDesign();
    
    void setWindFix(Wind wind);
    
    void addChangedListener(RaceStateChangedListener listener);
    void removeChangedListener(RaceStateChangedListener listener);

}
