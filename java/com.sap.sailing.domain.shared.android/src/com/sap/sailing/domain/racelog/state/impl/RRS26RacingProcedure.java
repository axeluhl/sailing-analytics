package com.sap.sailing.domain.racelog.state.impl;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.RacingProcedureType;
import com.sap.sailing.domain.racelog.RaceLog;
import com.sap.sailing.domain.racelog.state.RacingProcedurePrerequisite;

public class RRS26RacingProcedure extends BaseRacingProcedure {

    public RRS26RacingProcedure(RaceLog raceLog) {
        super(raceLog);
    }

    @Override
    public RacingProcedureType getType() {
        return RacingProcedureType.RRS26;
    }

    @Override
    public RacingProcedurePrerequisite checkPrerequisitesForStart(TimePoint startTime) {
        // TODO Auto-generated method stub
        return null;
    }

}
