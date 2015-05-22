package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.basic.BasicRacingProcedure;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.R;

public class RaceInfoRaceFragment extends BaseRaceInfoRaceFragment<BasicRacingProcedure> {

    public static RaceInfoRaceFragment newInstance() {
        RaceInfoRaceFragment fragment = new RaceInfoRaceFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.race_main, container, false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sendIntent(AppConstants.INTENT_ACTION_SHOW_MAIN_CONTENT);
    }

    @Override
    protected void setupUi() {

    }
}
