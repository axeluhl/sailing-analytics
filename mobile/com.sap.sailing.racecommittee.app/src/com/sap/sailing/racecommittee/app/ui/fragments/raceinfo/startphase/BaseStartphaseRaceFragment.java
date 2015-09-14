package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.startphase;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.raceinfo.BaseRaceInfoRaceFragment;
import com.sap.sailing.racecommittee.app.utils.BitmapHelper;
import com.sap.sailing.racecommittee.app.utils.ThemeHelper;

public abstract class BaseStartphaseRaceFragment<ProcedureType extends RacingProcedure> extends BaseRaceInfoRaceFragment<ProcedureType>
    implements View.OnClickListener {

    private ArrayList<ImageView> mDots;
    private ArrayList<View> mPanels;

    private int mActivePage = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.race_main, container, false);

        mDots = new ArrayList<>();
        mPanels = new ArrayList<>();

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
//        dot = ViewHelper.get(layout, R.id.panel_3);
//        if (dot != null) {
//            mDots.add(dot);
//        }

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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mDots != null && mDots.size() > 0) {
            viewPanel(0);
        }
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

    private void viewPanel(int direction) {
        mActivePage += direction;
        if (mActivePage < 0) {
            mActivePage = mDots.size() - 1;
        }
        if (mActivePage == mDots.size()) {
            mActivePage = 0;
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
}
