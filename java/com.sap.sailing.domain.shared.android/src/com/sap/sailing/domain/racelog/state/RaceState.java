package com.sap.sailing.domain.racelog.state;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.CompetitorResults;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedurePrerequisite;
import com.sap.sailing.domain.tracking.Wind;

public interface RaceState extends ReadonlyRaceState {
    
    RaceLogEventAuthor getAuthor();    
    
    RacingProcedure getRacingProcedure();
    <T extends RacingProcedure> T getTypedRacingProcedure();
    <T extends RacingProcedure> T getTypedRacingProcedure(Class<T> clazz);
    void setRacingProcedure(TimePoint timePoint, RacingProcedureType racingProcedureType);
    
    RacingProcedurePrerequisite setStartTime(TimePoint now, TimePoint startTime);
    void setFinishingTime(TimePoint timePoint);
    void setFinishedTime(TimePoint timePoint);
    void setProtestTime(TimePoint now, TimePoint timePoint);
    void setAdvancePass(TimePoint timePoint);
    void setAborted(TimePoint timePoint, boolean isPostponed, Flags reasonFlag);
    void setGeneralRecall(TimePoint timePoint);
    
    void setFinishPositioningListChanged(TimePoint timePoint, CompetitorResults positionedCompetitors);
    
    void setFinishPositioningConfirmed(TimePoint timePoint);
    
    void setCourseDesign(TimePoint timePoint, CourseBase courseDesign);
    
    void setWindFix(TimePoint timePoint, Wind wind);
   

}
