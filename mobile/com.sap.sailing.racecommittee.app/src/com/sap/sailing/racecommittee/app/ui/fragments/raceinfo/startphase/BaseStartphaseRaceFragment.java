package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.drawable.Drawable;
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
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public abstract class BaseStartphaseRaceFragment<ProcedureType extends RacingProcedure> extends BaseRaceInfoRaceFragment<ProcedureType>
    implements View.OnClickListener {

    private ArrayList<ImageView> mDots;
    private ArrayList<View> mPanels;
    private RaceStateListener mStateListener;

    private int mActivePage = 0;

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
    }

    @Override
    public void onPause() {
        super.onPause();

        getRaceState().removeChangedListener(mStateListener);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        racingProcedureChanged();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_prev:
                viewPanel(-1);
                break;

            case R.id.nav_next:
                viewPanel(1);
                break;
        }
    }

    private void racingProcedureChanged() {
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
                viewPanel(0);
            }
        }
    }

    private void viewPanel(int direction) {
        mActivePage += direction;
        if (mActivePage < 0) {
            mActivePage = mDots.size() - 1;
        }
        if (mActivePage == mDots.size()) {
            mActivePage = 0;
        }

        if (mDots.get(mActivePage).getVisibility() == View.GONE) {
            viewPanel(direction);
        }

        for (ImageView mDot : mDots) {
            int tint = ThemeHelper.getColor(getActivity(), R.attr.sap_light_gray);
            Drawable drawable = BitmapHelper.getTintedDrawable(getActivity(), R.drawable.ic_dot, tint);
            mDot.setImageDrawable(drawable);
        }

        int tint = ThemeHelper.getColor(getActivity(), R.attr.black);
        Drawable drawable = BitmapHelper.getTintedDrawable(getActivity(), R.drawable.ic_dot, tint);
        mDots.get(mActivePage).setImageDrawable(drawable);

        for (View view : mPanels) {
            view.setVisibility(View.GONE);
        }

        mPanels.get(mActivePage).setVisibility(View.VISIBLE);
    }

    private class RaceStateListener extends BaseRaceStateChangedListener {

        @Override
        public void onRacingProcedureChanged(ReadonlyRaceState state) {
            super.onRacingProcedureChanged(state);

            racingProcedureChanged();
        }
    }
}
