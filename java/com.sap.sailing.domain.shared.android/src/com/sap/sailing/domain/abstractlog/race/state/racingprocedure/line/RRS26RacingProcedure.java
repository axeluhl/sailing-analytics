package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line;

import java.util.Arrays;
import java.util.List;

import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.Flags;

public interface RRS26RacingProcedure extends ConfigurableStartModeFlagRacingProcedure {
    public final static Flags DEFAULT_START_MODE = Flags.PAPA;
    public final static List<Flags> DEFAULT_START_MODE_FLAGS = Arrays.asList(Flags.PAPA, Flags.ZULU, Flags.BLACK, Flags.INDIA, Flags.UNIFORM, Flags.INDIA_ZULU);

    RRS26Configuration getConfiguration();
}
