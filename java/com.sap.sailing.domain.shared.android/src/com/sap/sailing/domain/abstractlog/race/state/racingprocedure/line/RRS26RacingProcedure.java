package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line;

import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.Flags;

public interface RRS26RacingProcedure extends ConfigurableStartModeFlagRacingProcedure {
    public final static Flags DefaultStartMode = Flags.PAPA;
    
    RRS26Configuration getConfiguration();
}
