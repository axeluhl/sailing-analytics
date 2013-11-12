package com.sap.sailing.domain.racelog.state;

import java.io.Serializable;
import java.util.List;

import com.sap.sailing.domain.base.CourseBase;
import com.sap.sailing.domain.common.MaxPointsReason;
import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.RaceLogEventAuthor;
import com.sap.sailing.domain.tracking.Wind;

public interface RaceState2 {
    
    RaceLog getRaceLog();
    RaceLogEventAuthor getAuthor();
    
    void setRacingProcedure(RacingProcedureType racingProcedureType);
    RacingProcedure2 getRacingProcedure();
    
    RaceLogRaceStatus getStatus();
    
    RacingProcedurePrerequisite setStartTime(TimePoint timePoint);
    TimePoint getStartTime();
    void setFinishingTime(TimePoint timePoint);
    TimePoint getFinishingTime();
    void setFinishedTime(TimePoint timePoint);
    TimePoint getFinishedTime();
    void setProtestTime(TimePoint timePoint);
    TimePoint getProtestTime();
    void setAborted(TimePoint timePoint, boolean isPostponed);
    void setGeneralRecall(TimePoint timePoint);
    
    void setFinishPositioningListChanged(List<Triple<Serializable, String, MaxPointsReason>> positionedCompetitors);
    List<Triple<Serializable, String, MaxPointsReason>> getFinishPositioningList();
    
    void setFinishPositioningConfirmed();
    boolean isFinishPositioningConfirmed();
    
    void setCourseDesign(CourseBase courseDesign);
    CourseBase getCourseDesign();
    
    void setWindFix(Wind wind);
    
    void addChangedListener(RaceState2ChangedListener listener);
    void removeChangedListener(RaceState2ChangedListener listener);

}
