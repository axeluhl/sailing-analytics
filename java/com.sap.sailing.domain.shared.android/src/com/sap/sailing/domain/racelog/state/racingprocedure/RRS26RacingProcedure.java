package com.sap.sailing.domain.racelog.state.racingprocedure;

import com.sap.sailing.domain.common.TimePoint;
import com.sap.sailing.domain.common.racelog.Flags;

public interface RRS26RacingProcedure extends RacingProcedure {
    
    public final static Flags DefaultStartMode = Flags.PAPA;
    
    void addChangedListener(RRS26ChangedListener listener);
    
    void setStartModeFlag(TimePoint timePoint, Flags startMode);
    Flags getStartModeFlag();

}
