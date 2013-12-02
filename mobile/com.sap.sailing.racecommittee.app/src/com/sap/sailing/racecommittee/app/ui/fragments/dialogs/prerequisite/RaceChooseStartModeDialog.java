/**
 * 
 */
package com.sap.sailing.racecommittee.app.ui.fragments.dialogs.prerequisite;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.impl.StartmodePrerequisite;
import com.sap.sailing.racecommittee.app.R;

public class RaceChooseStartModeDialog extends PrerequisiteRaceDialog<StartmodePrerequisite, Flags> {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_choose_start_mode_view, container);
        getDialog().setTitle(getText(R.string.choose_startmode));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        LinearLayout container = (LinearLayout) getView().findViewById(R.id.race_choose_startmode_container);
        for (final Flags flag : preferences.getRRS26StartmodeFlags()) {
            Button button = new Button(getActivity());
            button.setText(flag.toString());
            button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    onChosen(flag);
                    dismiss();
                }
            });
            container.addView(button);
        }

    }
    
    @Override
    protected void onNormalChosen(Flags flag) {
        getRaceState().getTypedRacingProcedure(RRS26RacingProcedure.class).setStartModeFlag(
                MillisecondsTimePoint.now(), flag);
    }
    
    @Override
    protected void onPrerequisiteChosen(StartmodePrerequisite prerequisite, Flags flag) {
        prerequisite.fulfill(flag);
    }
}
