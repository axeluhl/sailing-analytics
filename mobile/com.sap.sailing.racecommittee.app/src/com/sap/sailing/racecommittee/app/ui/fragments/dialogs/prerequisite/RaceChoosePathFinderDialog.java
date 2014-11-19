package com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.sap.sailing.domain.racelog.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.gate.impl.PathfinderPrerequisite;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.domain.ManagedRace;
import com.sap.sailing.racecommittee.app.ui.fragments.dialogs.RaceDialogFragment;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceChoosePathFinderDialog extends PrerequisiteRaceDialog<PathfinderPrerequisite, String> {
    
    public static RaceChoosePathFinderDialog createOn(ManagedRace race) {
        RaceChoosePathFinderDialog dialog = new RaceChoosePathFinderDialog();
        dialog.setArguments(RaceDialogFragment.createArguments(race));
        return dialog;
    }

    private EditText sailingNationalityEditText;
    private EditText sailingNumberEditText;
    private Button chooseButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.race_choose_path_finder_view, container);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getDialog().setTitle(getString(R.string.set_pathfinder_title));

        sailingNationalityEditText = (EditText) getView().findViewById(R.id.pathFinderNationality);
        sailingNumberEditText = (EditText) getView().findViewById(R.id.pathFinderNumber);

        final EditText focusEditText = sailingNumberEditText;
        sailingNationalityEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() == 3)
                    focusEditText.requestFocus();
            }
        });

        chooseButton = (Button) getDialog().findViewById(R.id.choosePathFinderButton);

        chooseButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (sailingNationalityEditText.getText().length() == 0 || sailingNumberEditText.getText().length() == 0) {
                    Toast.makeText(getActivity(), "Please enter a valid pathfinder", Toast.LENGTH_LONG).show();
                } else {
                    onChosen(composeSailingId());
                    dismiss();
                }
            }
        });
    }

    protected String composeSailingId() {
        String sailingId = sailingNationalityEditText.getText().toString() + " "
                + sailingNumberEditText.getText().toString();
        return sailingId;
    }

    @Override
    protected void onNormalChosen(String pathfinderId) {
        GateStartRacingProcedure procedure = getRaceState().getTypedRacingProcedure();
        procedure.setPathfinder(MillisecondsTimePoint.now(), pathfinderId);
    }
    
    @Override
    protected void onPrerequisiteChosen(PathfinderPrerequisite prerequisite, String pathfinderId) {
        prerequisite.fulfill(pathfinderId);
    }

}
