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
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.race_flag_screen, container, false);

        mLayout = (LinearLayout) layout.findViewById(R.id.flags);

        return layout;
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
        List<FlagPole> upcoming = poleState.computeUpcomingChanges();
        mLayout.removeAllViews();
        int size = 0;
        for (FlagPole flagPole : currentState) {
            size++;
            mLayout.addView(renderFlag(poleState, flagPole, FlagPoleState.getMostInterestingFlagPole(upcoming), currentState.size() == size));
        }
    }

    private RelativeLayout renderFlag(FlagPoleState poleState, final FlagPole flag, FlagPole nextFlag, boolean lastEntry) {
        RelativeLayout layout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.race_flag, mLayout, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        layout.setLayoutParams(layoutParams);

        TimePoint changeAt = poleState.getNextStateValidFrom();
        boolean next = false;
        if (nextFlag != null && flag.getUpperFlag().equals(nextFlag.getUpperFlag())) {
            next = true;
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
            flagView.setBackgroundColor(getFleetColorId());
            flagView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Toast.makeText(view.getContext(), flag.getUpperFlag().name() + "|" + flag.getLowerFlag().name(), Toast.LENGTH_SHORT).show();
                }
            });
        }

        textView.setText("");
        if (changeAt != null && next) {
            textView.setVisibility(View.VISIBLE);
            textView.setText(getDuration(changeAt.asDate(), Calendar.getInstance().getTime()).replace("-", ""));
            if (flag.isDisplayed()) {
                downView.setVisibility(View.VISIBLE);
                upView.setVisibility(View.GONE);
            } else {
                downView.setVisibility(View.GONE);
                upView.setVisibility(View.VISIBLE);
            }
        } else {
            if (flag.isDisplayed()) {
                downView.setVisibility(View.INVISIBLE);
                upView.setVisibility(View.GONE);
            } else {
                downView.setVisibility(View.GONE);
                upView.setVisibility(View.INVISIBLE);
            }
        }

        return layout;
    }

    private int getFleetColorId() {
        Util.Triple<Integer, Integer, Integer> rgb = getRace().getFleet().getColor() == null ? new Util.Triple<>(0, 0, 0) : getRace().getFleet().getColor().getAsRGB();
        return Color.rgb(rgb.getA(), rgb.getB(), rgb.getC());
    }
}
