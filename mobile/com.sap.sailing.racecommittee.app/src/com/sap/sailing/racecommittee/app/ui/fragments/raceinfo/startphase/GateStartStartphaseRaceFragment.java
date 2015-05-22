package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import android.os.Bundle;
import android.widget.TextView;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.ReadonlyGateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.racecommittee.app.R;

public class GateStartStartphaseRaceFragment extends BaseStartphaseRaceFragment<GateStartRacingProcedure> {

    private TextView pathfinderTextView;
    private TextView gateLaunchTimeTextView;

    public GateStartStartphaseRaceFragment() {
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
