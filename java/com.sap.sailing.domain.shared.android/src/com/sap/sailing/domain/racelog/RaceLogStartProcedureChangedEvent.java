package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.common.racelog.RacingProcedureType;


public interface RaceLogStartProcedureChangedEvent extends RaceLogEvent {
    
    RacingProcedureType getStartProcedureType();

}
