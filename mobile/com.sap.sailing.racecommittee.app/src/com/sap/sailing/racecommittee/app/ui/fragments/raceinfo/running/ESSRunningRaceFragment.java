package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.racelog.state.racingprocedure.ess.ESSRacingProcedure;

public class ESSRunningRaceFragment extends BaseRunningRaceFragment<ESSRacingProcedure> {
    
    @Override
    protected void setFinishingTime() {
        getRaceState().setFinishingTime(MillisecondsTimePoint.now());
    }
    
}
