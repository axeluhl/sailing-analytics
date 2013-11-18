/**
 * 
 */
package com.sap.sailing.racecommittee.app.ui.fragments.dialogs;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.sap.sailing.domain.common.impl.MillisecondsTimePoint;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.racelog.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.racecommittee.app.R;

public class RaceChooseStartModeDialog extends RaceDialogFragment {

    public interface StartModeSelectionListener {
        public void onStartModeSelected();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.race_choose_start_mode_view, container);
        getDialog().setTitle(getText(R.string.choose_startmode));

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ImageButton papaButton = (ImageButton) getView().findViewById(R.id.papaFlagButton);
        ImageButton zuluButton = (ImageButton) getView().findViewById(R.id.zuluFlagButton);
        ImageButton blackButton = (ImageButton) getView().findViewById(R.id.blackFlagButton);
        ImageButton indiaButton = (ImageButton) getView().findViewById(R.id.indiaFlagButton);

        papaButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                process(Flags.PAPA);
            }

        });

        zuluButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                process(Flags.ZULU);
            }

        });

        blackButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                process(Flags.BLACK);
            }

        });

        indiaButton.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                process(Flags.INDIA);
            }

        });

    }

    private void process(Flags flag) {
        RRS26RacingProcedure procedure = getRace().getState().getTypedRacingProcedure();
        procedure.setStartModeFlag(MillisecondsTimePoint.now(), flag);
        this.dismiss();
    }
}
