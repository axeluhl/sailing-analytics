package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line;

import com.sap.sailing.domain.base.configuration.procedures.SWCStartConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

public interface SWCRacingProcedure extends ConfigurableStartModeFlagRacingProcedure {
    public final static Flags DEFAULT_START_MODE = Flags.UNIFORM;
    
    SWCStartConfiguration getConfiguration();
}
