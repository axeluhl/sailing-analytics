package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.fragments.RaceFragment;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.Calendar;
import java.util.List;
import java.util.Objects;

public class RaceFlagViewerFragment extends RaceFragment {

    private LinearLayout mLayout;

    public RaceFlagViewerFragment() {

    }

    public static RaceFlagViewerFragment newInstance() {
        RaceFlagViewerFragment fragment = new RaceFlagViewerFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mLayout = (LinearLayout) inflater.inflate(R.layout.race_flag_screen, container, false);

        return mLayout;
    }

    @Override
    public void notifyTick() {
        super.notifyTick();

        updateFlags();
    }

    private void updateFlags() {
        if (getRace() == null || getRaceState() == null || getRaceState().getStartTime() == null) {
            return;
        }

        FlagPoleState poleState = getRaceState().getRacingProcedure().getActiveFlags(getRaceState().getStartTime(),
                MillisecondsTimePoint.now());
        List<FlagPole> currentState = poleState.getCurrentState();
        mLayout.removeAllViews();
        int size = 0;
        for (FlagPole flagPole : currentState) {
            size++;
            mLayout.addView(renderFlag(poleState, flagPole, currentState.size() == size));
        }
    }

    private RelativeLayout renderFlag(FlagPoleState poleState, final FlagPole flag, boolean lastEntry) {
        RelativeLayout layout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.race_flag, mLayout, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        layout.setLayoutParams(layoutParams);

        TimePoint changeAt = poleState.getNextStateValidFrom();
        List<FlagPole> upcoming = poleState.computeUpcomingChanges();
        boolean next = false;
        for (FlagPole pole : upcoming) {
            if (pole.getUpperFlag().name().equals(flag.getUpperFlag().name())) {
                next = true;
            }
        }

        ImageView flagView = (ImageView) layout.findViewById(R.id.flag);
        TextView textView = (TextView) layout.findViewById(R.id.flag_text);
        View downView = layout.findViewById(R.id.arrow_down);
        View upView = layout.findViewById(R.id.arrow_up);
        View line = layout.findViewById(R.id.line);
        if (lastEntry) {
            line.setVisibility(View.GONE);
        }

        flagView.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), flag.getUpperFlag().name(), 96));
        if (flag.getUpperFlag() == Flags.CLASS && getRace().getFleet().getColor() != null) {
            flagView.setPadding(6, 6, 6, 6);
            flagView.setBackgroundColor(getFleetColorId());
        }

        downView.setVisibility(View.GONE);
        upView.setVisibility(View.GONE);
        textView.setText(null);
        if (changeAt != null && next) {
            textView.setText(getDuration(changeAt.asDate(), Calendar.getInstance().getTime()).replace("-", ""));
            if (!flag.isDisplayed()) {
                upView.setVisibility(View.VISIBLE);
            } else {
                downView.setVisibility(View.VISIBLE);
            }
        }

        layout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), flag.getUpperFlag().name() + "|" + flag.getLowerFlag().name(), Toast.LENGTH_SHORT).show();
            }
        });

        return layout;
    }

    private int getFleetColorId() {
        Util.Triple<Integer, Integer, Integer> rgb = getRace().getFleet().getColor() == null ? new Util.Triple<>(0, 0, 0) : getRace().getFleet().getColor().getAsRGB();
        return Color.rgb(rgb.getA(), rgb.getB(), rgb.getC());
    }
}
