package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.racecommittee.app.R;

public class RaceChooseGateLaunchTimeDialog extends RaceDialogFragment {

    public interface GateLineOpeningTimeSelectionListener {
        public void onLineOpeningTimeSelected();
    }

    private EditText lineOpeningTimeEditText;
    private Button chooseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_choose_line_opening_time_view, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().setTitle(getString(R.string.set_gate_line_opening_time_title));

        lineOpeningTimeEditText = (EditText) getView().findViewById(R.id.lineOpeningTimeText);
        GateStartRacingProcedure procedure = getRaceState().getTypedRacingProcedure();
        lineOpeningTimeEditText.setText(String.valueOf((procedure.getGateLaunchTime() / (60 * 1000))));
        chooseButton = (Button) getDialog().findViewById(R.id.chooseLineOpeningTimeButton);

        chooseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                onChooseClicked(v);
            }
        });
    }

    protected void onChooseClicked(View view) {
        try {
            Long lineOpeningTime = Long.parseLong(lineOpeningTimeEditText.getText().toString());
            GateStartRacingProcedure procedure = getRace().getState().getTypedRacingProcedure();
            procedure.setGateLaunchTime(MillisecondsTimePoint.now(), Long.valueOf(60 * 1000 * lineOpeningTime));
            Log.i("RACE_SET_GATELINE_OPENING_TIME", String.valueOf(lineOpeningTime));
            dismiss();
        } catch (NumberFormatException nfe) {
            Toast.makeText(getActivity(), "Please enter a valid number of minutes", Toast.LENGTH_LONG).show();
        }
    }
}
