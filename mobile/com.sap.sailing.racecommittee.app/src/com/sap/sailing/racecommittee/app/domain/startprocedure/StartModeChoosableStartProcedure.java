package com.sap.sailing.racecommittee.app.domain.startprocedure;

import com.sap.sailing.domain.common.racelog.Flags;

public interface StartModeChoosableStartProcedure  extends StartProcedure{
    void setStartModeFlag(Flags startModeFlag);
    Flags getCurrentStartModeFlag();
}
