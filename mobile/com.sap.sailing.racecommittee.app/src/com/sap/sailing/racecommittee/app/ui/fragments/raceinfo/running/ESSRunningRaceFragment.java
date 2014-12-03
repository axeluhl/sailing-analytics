package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ess.ESSRacingProcedure;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class ESSRunningRaceFragment extends BaseRunningRaceFragment<ESSRacingProcedure> {
    
    @Override
    protected void setFinishingTime() {
        getRaceState().setFinishingTime(MillisecondsTimePoint.now());
    }
    
}
