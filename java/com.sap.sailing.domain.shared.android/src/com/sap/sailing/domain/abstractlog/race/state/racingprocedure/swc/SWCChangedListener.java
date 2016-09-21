package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.swc;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;

public interface SWCChangedListener extends RacingProcedureChangedListener {
    
    void onStartmodeChanged(ReadonlySWCRacingProcedure racingProcedure);

}
