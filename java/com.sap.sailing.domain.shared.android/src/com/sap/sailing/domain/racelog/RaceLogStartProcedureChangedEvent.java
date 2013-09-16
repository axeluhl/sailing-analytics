package com.sap.sailing.domain.racelog;

import com.sap.sailing.domain.common.racelog.StartProcedureType;

public interface RaceLogStartProcedureChangedEvent extends RaceLogEvent {
    
    StartProcedureType getStartProcedureType();

}
