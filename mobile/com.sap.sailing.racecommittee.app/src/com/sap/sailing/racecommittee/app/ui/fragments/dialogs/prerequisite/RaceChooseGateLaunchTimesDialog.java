package com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent;
import com.sap.sailing.domain.abstractlog.race.RaceLogGateLineOpeningTimeEvent.GateLineOpeningTimes;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.impl.GateLaunchTimePrerequisite;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceChooseGateLaunchTimesDialog extends PrerequisiteRaceDialog<GateLaunchTimePrerequisite, RaceLogGateLineOpeningTimeEvent.GateLineOpeningTimes> {

    public interface GateLineOpeningTimeSelectionListener {
        public void onLineOpeningTimeSelected();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_choose_line_opening_time_view, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().setTitle(getString(R.string.set_gate_line_opening_time_title));

        final EditText gateLaunchStopTimeEdit = (EditText) getView().findViewById(R.id.gateLaunchStopTimeText);
        gateLaunchStopTimeEdit.setText(String.valueOf((getProcedure().getGateLaunchStopTime() / (60 * 1000))));
        
        final EditText golfDownTimeEdit = (EditText) getView().findViewById(R.id.golfDownTimeText);
        golfDownTimeEdit.setText(String.valueOf((getProcedure().getGolfDownTime() / (60 * 1000))));
        
        final ViewGroup golfDownTimeContainer = (ViewGroup) getView().findViewById(R.id.golfDownTimeContainer);
        if (getProcedure().getConfiguration().hasAdditionalGolfDownTime()) {
            golfDownTimeContainer.setVisibility(View.VISIBLE);
        } else {
            golfDownTimeContainer.setVisibility(View.GONE);
        }
        
        final Button chooseButton = (Button) getDialog().findViewById(R.id.chooseLineOpeningTimeButton);
        chooseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                    long gateLaunchStopTime = Long.parseLong(gateLaunchStopTimeEdit.getText().toString());
                    long golfDownTime = 0;
                    if (getProcedure().getConfiguration().hasAdditionalGolfDownTime()) {
                        golfDownTime = Long.parseLong(golfDownTimeEdit.getText().toString());
                    }
                    onChosen(new RaceLogGateLineOpeningTimeEvent.GateLineOpeningTimes(
                            60 * 1000 * gateLaunchStopTime, 
                            60 * 1000 * golfDownTime));
                    dismiss();
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Please enter a valid number", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    protected void onNormalChosen(GateLineOpeningTimes value) {
        getProcedure().setGateLineOpeningTimes(MillisecondsTimePoint.now(), value.getGateLaunchStopTime(), value.getGolfDownTime());
    }
    
    @Override
    protected void onPrerequisiteChosen(GateLaunchTimePrerequisite prerequisite, GateLineOpeningTimes value) {
        prerequisite.fulfill(value.getGateLaunchStopTime(), value.getGolfDownTime());
    }
    
    protected GateStartRacingProcedure getProcedure() {
        return getRaceState().getTypedRacingProcedure();
    }
}
