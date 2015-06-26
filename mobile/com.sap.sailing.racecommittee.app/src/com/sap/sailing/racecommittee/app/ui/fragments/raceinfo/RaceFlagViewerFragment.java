package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.sap.sailing.android.shared.util.ViewHolder;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.List;

public class RaceFlagViewerFragment extends BaseFragment {

    private static final int UPPER_FLAG = 0;
    private static final int LOWER_FLAG = 1;

    private LinearLayout mLayout;
    private View mRecall;

    private ImageView mXrayFlag;
    private TextView mXrayCountdown;
    private Button mXrayButton;

    public RaceFlagViewerFragment() {

    }

    public static RaceFlagViewerFragment newInstance() {
        RaceFlagViewerFragment fragment = new RaceFlagViewerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.race_flag_screen, container, false);

        mLayout = ViewHolder.get(layout, R.id.flags);
        mRecall = ViewHolder.get(layout, R.id.individual_recall);

        mXrayButton = ViewHolder.get(layout, R.id.flag_down);
        if (mXrayButton != null) {
            mXrayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                    procedure.removeIndividualRecall(MillisecondsTimePoint.now());
                }
            });
        }
        mXrayFlag = ViewHolder.get(layout, R.id.flag);
        if (mXrayFlag != null) {
            mXrayFlag.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), Flags.XRAY.name(), 96));
        }
        mXrayCountdown = ViewHolder.get(layout, R.id.xray_down);

        return layout;
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        updateFlags(now);
    }

    private void updateFlags(TimePoint timePoint) {
        if (getRace() == null || getRaceState() == null || getRaceState().getStartTime() == null) {
            return;
        }

        if (mLayout != null) {
            mLayout.setVisibility(View.GONE);
        }
        if (mRecall != null) {
            mRecall.setVisibility(View.GONE);
        }
        RacingProcedure procedure = getRaceState().getTypedRacingProcedure();
        if (!procedure.isIndividualRecallDisplayed()) {
            if (mLayout != null) {
                mLayout.setVisibility(View.VISIBLE);
            }
            FlagPoleState poleState = getRaceState().getRacingProcedure().getActiveFlags(getRaceState().getStartTime(), timePoint);
            List<FlagPole> currentState = poleState.getCurrentState();
            List<FlagPole> upcoming = poleState.computeUpcomingChanges();
            FlagPole nextPole = FlagPoleState.getMostInterestingFlagPole(upcoming);
            mLayout.removeAllViews();
            int size = 0;
            Flags flag;
            for (FlagPole flagPole : currentState) {
                size++;
                flag = flagPole.getUpperFlag();
                mLayout.addView(renderFlag(timePoint, poleState, flag, isNextFlag(flag, nextPole), currentState.size() == size, flagPole.isDisplayed(), UPPER_FLAG));
                if (!flagPole.getLowerFlag().equals(Flags.NONE)) {
                    flag = flagPole.getLowerFlag();
                    mLayout.addView(renderFlag(timePoint, poleState, flag, isNextFlag(flag, nextPole), currentState.size() == size, false, LOWER_FLAG));
                }
            }
        } else {
            String time = getString(R.string.until_xray_removed);
            if (mXrayCountdown != null) {
                TimePoint flagDown = procedure.getIndividualRecallRemovalTime();
                if (timePoint.before(flagDown)) {
                    time = time.replace("#TIME#", TimeUtils.formatDurationUntil(flagDown.minus(timePoint.asMillis()).asMillis()).replace("-", ""));
                    mXrayCountdown.setText(time);
                } else {
                    procedure.removeIndividualRecall(timePoint);
                }
            }

            if (mRecall != null) {
                mRecall.setVisibility(View.VISIBLE);
            }
        }
    }

    private boolean isNextFlag(Flags flag, FlagPole pole) {
        return pole != null && flag.equals(pole.getUpperFlag());
    }

    private RelativeLayout renderFlag(TimePoint timePoint, FlagPoleState poleState, final Flags flag, boolean isNext, boolean lastEntry, boolean isDisplayed, int flagType) {
        RelativeLayout layout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.race_flag, mLayout, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        layout.setLayoutParams(layoutParams);

        TimePoint changeAt = poleState.getNextStateValidFrom();
        ImageView flagView = (ImageView) layout.findViewById(R.id.flag);
        TextView textView = (TextView) layout.findViewById(R.id.flag_text);
        View downView = layout.findViewById(R.id.arrow_down);
        View upView = layout.findViewById(R.id.arrow_up);
        View line = layout.findViewById(R.id.line);
        if (lastEntry) {
            line.setVisibility(View.GONE);
        }

        flagView.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), flag.name(), 96));
        if (flag == Flags.CLASS && getRace().getFleet().getColor() != null) {
            flagView.setBackgroundColor(getFleetColorId());
        }
        flagView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), flag.name(), Toast.LENGTH_SHORT).show();
            }
        });

        textView.setText("");
        if (changeAt != null && isNext) {
            textView.setVisibility(View.VISIBLE);
            String timer;
            if (changeAt.after(timePoint)) {
                timer = TimeUtils.formatDurationUntil(changeAt.minus(timePoint.asMillis()).asMillis());
            } else {
                timer = TimeUtils.formatDurationSince(timePoint.minus(changeAt.asMillis()).asMillis());
            }
            textView.setText(timer.replace("-", ""));
            if (flagType == UPPER_FLAG) {
                if (isDisplayed) {
                    downView.setVisibility(View.VISIBLE);
                    upView.setVisibility(View.GONE);
                } else {
                    downView.setVisibility(View.GONE);
                    upView.setVisibility(View.VISIBLE);
                }
            } else {
                downView.setVisibility(View.GONE);
                upView.setVisibility(View.VISIBLE);
            }
        } else {
            if (isDisplayed) {
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
