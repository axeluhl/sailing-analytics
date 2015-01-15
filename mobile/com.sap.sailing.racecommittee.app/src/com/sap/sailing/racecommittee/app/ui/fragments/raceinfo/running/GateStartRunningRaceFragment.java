package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.ReadonlyGateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;

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
        public void onIndividualRecallDisplayed(ReadonlyRacingProcedure racingProcedure) {
            
        }
        
        @Override
        public void onIndividualRecallRemoved(ReadonlyRacingProcedure racingProcedure) {
            
        }
        
        @Override
        public void onGateLaunchTimeChanged(ReadonlyGateStartRacingProcedure gateStartRacingProcedure) {
            // not interested - handled by active flags
        }

        @Override
        public void onPathfinderChanged(ReadonlyGateStartRacingProcedure procedure) {
            // we do not display changes of the pathfinder this late
        }
        
    }

}
