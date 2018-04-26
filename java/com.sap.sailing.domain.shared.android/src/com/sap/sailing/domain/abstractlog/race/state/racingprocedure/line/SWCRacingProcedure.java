package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line;

import java.util.Arrays;
import java.util.List;

import com.sap.sailing.domain.base.configuration.procedures.SWCStartConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

public interface SWCRacingProcedure extends ConfigurableStartModeFlagRacingProcedure {

    public final static Flags DEFAULT_START_MODE = Flags.UNIFORM;
    public final static List<Flags> DEFAULT_START_MODE_FLAGS = Arrays.asList(Flags.BLACK, Flags.UNIFORM, Flags.PAPA);
    
    SWCStartConfiguration getConfiguration();
}
