package com.sap.sailing.domain.racelog.state.racingprocedure.rrs26;

import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedureChangedListener;

public interface RRS26ChangedListener extends RacingProcedureChangedListener {
    
    void onStartmodeChanged(ReadonlyRRS26RacingProcedure racingProcedure);

}
