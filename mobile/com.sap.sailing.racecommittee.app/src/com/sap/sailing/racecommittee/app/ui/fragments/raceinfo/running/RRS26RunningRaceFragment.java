package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.running;

import android.widget.Toast;

import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26ChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.RRS26RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;

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
        }
        
        @Override
        public void onIndividualRecallRemoved(RacingProcedure racingProcedure) {
        }

        @Override
        public void onStartmodeChanged(RRS26RacingProcedure racingProcedure) {
            // that's a little bit late, isn't it?
            Toast.makeText(getActivity(), 
                    String.format("Start mode flag changed to %s", racingProcedure.getStartModeFlag()), Toast.LENGTH_SHORT).show();
        }
        
    }

}
