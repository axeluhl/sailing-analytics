package com.sap.sailing.domain.racelog.state.racingprocedure.rrs26;

import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.ReadonlyRacingProcedure;

public interface ReadonlyRRS26RacingProcedure extends ReadonlyRacingProcedure {
    
    public final static Flags DefaultStartMode = Flags.PAPA;
    
    void addChangedListener(RRS26ChangedListener listener);
    
    RRS26Configuration getConfiguration();
    
    Flags getStartModeFlag();

}
