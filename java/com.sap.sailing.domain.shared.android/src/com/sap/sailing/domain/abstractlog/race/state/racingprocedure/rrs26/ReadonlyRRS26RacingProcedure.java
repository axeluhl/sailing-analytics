package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.base.configuration.procedures.RRS26Configuration;
import com.sap.sailing.domain.common.racelog.Flags;

public interface ReadonlyRRS26RacingProcedure extends ReadonlyRacingProcedure {
    
    public final static Flags DefaultStartMode = Flags.PAPA;
    
    void addChangedListener(RRS26ChangedListener listener);
    
    RRS26Configuration getConfiguration();
    
    Flags getStartModeFlag();

}
