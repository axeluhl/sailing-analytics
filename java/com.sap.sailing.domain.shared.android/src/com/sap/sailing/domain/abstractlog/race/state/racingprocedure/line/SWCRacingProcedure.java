package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line;

import com.sap.sailing.domain.base.configuration.procedures.SWCConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

public interface SWCRacingProcedure extends LineStartRacingProcedure {

    public final static Flags DefaultStartMode = Flags.UNIFORM;
    
    SWCConfiguration getConfiguration();
}
