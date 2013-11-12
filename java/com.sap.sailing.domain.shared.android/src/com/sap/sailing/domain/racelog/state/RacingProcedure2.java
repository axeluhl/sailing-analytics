package com.sap.sailing.domain.racelog.state;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;

public interface RacingProcedure2 {
    
    RaceLog getRaceLog();
    RacingProcedureType getType();
    
    RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint startTime);

}
