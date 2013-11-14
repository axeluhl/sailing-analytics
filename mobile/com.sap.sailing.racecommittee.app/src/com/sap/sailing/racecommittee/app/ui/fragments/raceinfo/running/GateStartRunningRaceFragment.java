package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;

public class GateStartRunningRaceFragment extends BaseRunningRaceFragment<GateStartRacingProcedure> {

    private GateStartChangedListener changeListener;
    
    public GateStartRunningRaceFragment() {
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
    
    private class ChangeListener extends BaseRacingProcedureChangedListener implements GateStartChangedListener {

        @Override
        public void onIndividualRecallDisplayed(RacingProcedure racingProcedure) {
            
        }
        
        @Override
        public void onIndividualRecallRemoved(RacingProcedure racingProcedure) {
            
        }
        
        @Override
        public void onGateLineOpeningTimeChanged(GateStartRacingProcedure gateStartRacingProcedure) {
            // not interested - handled by active flags
        }

        @Override
        public void onPathfinderChanged(GateStartRacingProcedure procedure) {
            // we do not display changes of the pathfinder this late
        }
        
    }

}
