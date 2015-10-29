package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.ReadonlyRaceState;
import com.sap.sailing.domain.abstractlog.race.state.impl.BaseRaceStateChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.gate.GateStartRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.rrs26.RRS26RacingProcedure;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.panels.MorePanelFragment;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseRaceInfoRaceFragment;

public abstract class BaseStartphaseRaceFragment<ProcedureType extends RacingProcedure> extends BaseRaceInfoRaceFragment<ProcedureType>
    implements View.OnClickListener {

    private RaceStateListener mStateListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_main, container, false);

        mDots = new ArrayList<>();
        mPanels = new ArrayList<>();

        mStateListener = new RaceStateListener();

        View panel;
        panel = ViewHelper.get(layout, R.id.race_panel_time);
        if (panel != null) {
            mPanels.add(panel);
        }
        panel = ViewHelper.get(layout, R.id.race_panel_setup);
        if (panel != null) {
            mPanels.add(panel);
        }
        panel = ViewHelper.get(layout, R.id.race_panel_extra);
        if (panel != null) {
            mPanels.add(panel);
        }

        ImageView dot;
        dot = ViewHelper.get(layout, R.id.panel_1);
        if (dot != null) {
            mDots.add(dot);
        }
        dot = ViewHelper.get(layout, R.id.panel_2);
        if (dot != null) {
            mDots.add(dot);
        }
        dot = ViewHelper.get(layout, R.id.panel_3);
        if (dot != null) {
            mDots.add(dot);
        }

        ImageView btnPrev = ViewHelper.get(layout, R.id.nav_prev);
        if (btnPrev != null) {
            btnPrev.setOnClickListener(this);
        }

        ImageView btnNext = ViewHelper.get(layout, R.id.nav_next);
        if (btnNext != null) {
            btnNext.setOnClickListener(this);
        }

        return layout;
    }

    @Override
    protected void setupUi() {
    }

    @Override
    public void onResume() {
        super.onResume();

        getRaceState().addChangedListener(mStateListener);
        initMoreButtons();
    }

    @Override
    public void onPause() {
        super.onPause();

        getRaceState().removeChangedListener(mStateListener);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_prev:
                viewPanel(MOVE_DOWN);
                break;

            case R.id.nav_next:
                viewPanel(MOVE_UP);
                break;
        }
    }

    private void initMoreButtons() {
        if (mDots != null && mDots.size() > 0) {
            if (getRaceState() != null && getRaceState().getRacingProcedure() != null) {
                RacingProcedure procedure = getRaceState().getRacingProcedure();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                if (procedure instanceof RRS26RacingProcedure || procedure instanceof GateStartRacingProcedure) {
                    mDots.get(mDots.size() - 1).setVisibility(View.VISIBLE);
                    transaction.replace(R.id.race_panel_extra, MorePanelFragment.newInstance(getArguments()));
                } else {
                    mDots.get(mDots.size() - 1).setVisibility(View.GONE);
                    Fragment fragment = getFragmentManager().findFragmentById(R.id.race_panel_extra);
                    if (fragment != null) {
                        transaction.remove(fragment);
                    }
                }
                transaction.commit();
                viewPanel(MOVE_NONE);
            }
        }
    }

    private class RaceStateListener extends BaseRaceStateChangedListener {

        @Override
        public void onRacingProcedureChanged(ReadonlyRaceState state) {
            super.onRacingProcedureChanged(state);

            initMoreButtons();
        }
    }
}
