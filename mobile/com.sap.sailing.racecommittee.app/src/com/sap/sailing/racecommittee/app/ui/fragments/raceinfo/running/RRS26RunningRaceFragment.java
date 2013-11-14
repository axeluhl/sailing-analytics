package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26ChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure2;
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
    
    @Override
    protected void setupUi() {
        // TODO: display all the flags!
    }
    
    private class ChangeListener extends BaseRacingProcedureChangedListener implements RRS26ChangedListener {

        @Override
        public void onIndividualRecallDisplayed(RacingProcedure2 racingProcedure) {
            ExLog.i("NY", "XRAY up");
        }
        
        @Override
        public void onIndividualRecallRemoved(RacingProcedure2 racingProcedure) {
            ExLog.i("NY", "XRAY down");
        }

        @Override
        public void onStartmodeChanged(RRS26RacingProcedure racingProcedure) {
            // not my interest
        }
        
    }

}
