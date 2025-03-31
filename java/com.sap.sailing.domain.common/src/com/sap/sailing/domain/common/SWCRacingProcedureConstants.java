package com.sap.sailing.domain.common;

import java.util.Arrays;
import java.util.List;

import com.sap.sailing.domain.common.racelog.Flags;

public interface SWCRacingProcedureConstants {
    final static List<Flags> DEFAULT_START_MODE_FLAGS = Arrays.asList(Flags.BLACK, Flags.UNIFORM, Flags.PAPA);
}
