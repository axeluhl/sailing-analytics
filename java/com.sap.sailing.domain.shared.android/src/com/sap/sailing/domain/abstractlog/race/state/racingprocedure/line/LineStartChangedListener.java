package com.sap.sailing.domain.abstractlog.race.state.racingprocedure.line;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;

public interface LineStartChangedListener extends RacingProcedureChangedListener {
    
    void onStartModeChanged(ConfigurableStartModeFlagRacingProcedure racingProcedure);

}
