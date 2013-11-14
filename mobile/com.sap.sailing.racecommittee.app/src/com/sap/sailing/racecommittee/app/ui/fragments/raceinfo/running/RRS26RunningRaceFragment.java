package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26ChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.racecommittee.app.logging.ExLog;

public class RRS26RunningRaceFragment extends BaseRunningRaceFragment<RRS26RacingProcedure> {

    
    private RRS26ChangedListener changeListener;
    
    public RRS26RunningRaceFragment() {
        this.changeListener = new ChangeListener();
    }

    @Override
    public void onStart() {
        super.onStart();
        getRacingProcedure().addChangedListener(changeListener);
    }
    
    @Override
    public void onStop() {
        getRacingProcedure().removeChangedListener(changeListener);
        super.onStop();
    }
    
    private class ChangeListener extends BaseRacingProcedureChangedListener implements RRS26ChangedListener {

        @Override
        public void onIndividualRecallDisplayed(RacingProcedure racingProcedure) {
            ExLog.i("NY", "XRAY up");
        }
        
        @Override
        public void onIndividualRecallRemoved(RacingProcedure racingProcedure) {
            ExLog.i("NY", "XRAY down");
        }

        @Override
        public void onStartmodeChanged(RRS26RacingProcedure racingProcedure) {
            // not my interest
        }
        
    }

}
