package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartChangedListener;
import com.sap.sailing.domain.racelog.state.racingprocedure.GateStartRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceChooseGateLineOpeningTimeDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;

public class GateStartStartphaseRaceFragment extends BaseStartphaseRaceFragment<GateStartRacingProcedure> {
    
    private final ChangeListener changeListener;
    
    public GateStartStartphaseRaceFragment() {
        this.changeListener = new ChangeListener();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button gateTimeButton = (Button) getView().findViewById(R.id.race_startphase_gate_actions_pathfinder_button);
        gateTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                RaceDialogFragment fragment = new RaceChooseGateLineOpeningTimeDialog();
                Bundle args = getRecentArguments();
                fragment.setArguments(args);
                fragment.show(fragmentManager, null);
            }
        });
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
    protected int getActionsLayoutId() {
        return R.layout.race_startphase_gate_actions;
    }

    @Override
    protected void setupUi() {
        // TODO: gate time and pathfinder ui update
        super.setupUi();
    }
    
    private class ChangeListener extends BaseRacingProcedureChangedListener implements GateStartChangedListener {

        @Override
        public void onGateLineOpeningTimeChanged(GateStartRacingProcedure gateStartRacingProcedure) {
            setupUi();
        }

        @Override
        public void onPathfinderChanged(GateStartRacingProcedure procedure) {
            setupUi();
        }
        
    }

}
