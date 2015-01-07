package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.ReadonlyGateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.RaceChooseGateLaunchTimesDialog;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite.RaceChoosePathFinderDialog;

public class GateStartStartphaseRaceFragment extends BaseStartphaseRaceFragment<GateStartRacingProcedure> {
    
    private final ChangeListener changeListener;
    
    private TextView pathfinderTextView;
    private TextView gateLaunchTimeTextView;
    
    public GateStartStartphaseRaceFragment() {
        this.changeListener = new ChangeListener();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        pathfinderTextView = (TextView) getView().findViewById(R.id.race_startphase_gate_actions_pathfinder_text);
        gateLaunchTimeTextView = (TextView) getView().findViewById(R.id.race_startphase_gate_actions_opening_text);
        
        Button pathfinderButton = (Button) getView().findViewById(R.id.race_startphase_gate_actions_pathfinder_button);
        pathfinderButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                RaceDialogFragment fragment = new RaceChoosePathFinderDialog();
                Bundle args = getRecentArguments();
                fragment.setArguments(args);
                fragment.show(fragmentManager, null);
            }
        });
        Boolean hasPathfinder = getRacingProcedure().getConfiguration().hasPathfinder();
        if (!hasPathfinder) {
            pathfinderButton.setVisibility(View.GONE);
            pathfinderTextView.setVisibility(View.GONE);
        }
        
        Button gateLaunchTimeButton = (Button) getView().findViewById(R.id.race_startphase_gate_actions_opening_button);
        gateLaunchTimeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager fragmentManager = getFragmentManager();
                RaceDialogFragment fragment = new RaceChooseGateLaunchTimesDialog();
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
        GateStartRacingProcedure procedure = getRacingProcedure();
        
        String pathfinderId = procedure.getPathfinder();
        if (pathfinderId != null) {
            pathfinderTextView.setText(String.format("%s", pathfinderId));
        } else {
            pathfinderTextView.setText(R.string.no_pathfinder_selected);
        }
       
        Long gateLaunchTime = procedure.getGateLaunchStopTime();
        if (gateLaunchTime != null) {
            gateLaunchTimeTextView.setText(getString(R.string.gate_launch_stops_after, gateLaunchTime / 1000 / 60));
        } else {
            gateLaunchTimeTextView.setText(R.string.no_line_opening_time_selected);
        }
        super.setupUi();
    }
    
    private class ChangeListener extends BaseRacingProcedureChangedListener implements GateStartChangedListener {

        @Override
        public void onGateLaunchTimeChanged(ReadonlyGateStartRacingProcedure gateStartRacingProcedure) {
            setupUi();
        }

        @Override
        public void onPathfinderChanged(ReadonlyGateStartRacingProcedure procedure) {
            setupUi();
        }
        
    }

}
