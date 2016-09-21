package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.swc;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.base.configuration.procedures.SWCConfiguration;
import com.sap.sailing.domain.common.racelog.Flags;

public interface ReadonlySWCRacingProcedure extends ReadonlyRacingProcedure {
    
    public final static Flags DefaultStartMode = Flags.UNIFORM;
    
    void addChangedListener(SWCChangedListener listener);
    
    SWCConfiguration getConfiguration();
    
    Flags getStartModeFlag();

}
