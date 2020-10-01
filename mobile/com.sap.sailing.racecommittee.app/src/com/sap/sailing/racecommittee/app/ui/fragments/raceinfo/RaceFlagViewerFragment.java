package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedureChangedListener;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.TickListener;
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

    private TextView mXrayCountdown;

    private FlagsCache mFlagsCache;
    private int mFlagSize;

    public static RaceFlagViewerFragment newInstance() {
        RaceFlagViewerFragment fragment = new RaceFlagViewerFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.race_flag_screen, container, false);

        mLayout = ViewHelper.get(layout, R.id.flags);
        mRecall = ViewHelper.get(layout, R.id.individual_recall);
        mFlagSize = getResources().getInteger(R.integer.flag_size_xlarge);

        Button xrayButton = ViewHelper.get(layout, R.id.flag_down);
        if (xrayButton != null) {
            xrayButton.setOnClickListener(v -> {
                RacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                procedure.removeIndividualRecall(MillisecondsTimePoint.now());
            });
        }
        ImageView xrayFlag = ViewHelper.get(layout, R.id.flag);
        if (xrayFlag != null) {
            xrayFlag.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), Flags.XRAY.name(), mFlagSize));
        }
        mXrayCountdown = ViewHelper.get(layout, R.id.xray_down);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        getRaceState().getRacingProcedure().addChangedListener(procedureChangedListener);
        updateFlags(TimePoint.now());
    }

    @Override
    public void onStop() {
        super.onStop();
        getRaceState().getRacingProcedure().removeChangedListener(procedureChangedListener);
    }

    @Override
    public TickListener getStartTimeTickListener() {
        return this::onStartTimeTick;
    }

    private void onStartTimeTick(TimePoint now) {
        updateFlags(now);
    }

    private void updateFlags(TimePoint now) {
        if (getRaceState().getStartTime() == null) {
            if (mLayout != null) {
                mLayout.setVisibility(View.GONE);
            }
            if (mRecall != null) {
                mRecall.setVisibility(View.GONE);
            }
            return;
        }

        final RacingProcedure procedure = getRaceState().getRacingProcedure();
        if (procedure.isIndividualRecallDisplayed()) {
            if (mXrayCountdown != null && mRecall != null) {
                TimePoint flagDown = procedure.getIndividualRecallRemovalTime();
                if (now.after(flagDown)) {
                    mRecall.setVisibility(View.GONE);
                    return;
                }
                mRecall.setVisibility(View.VISIBLE);
                mXrayCountdown.setText(
                        getString(R.string.until_xray_removed, TimeUtils.formatDuration(now, flagDown, false)));
            }
            return;
        }

        final TimePoint startTime = getRaceState().getStartTime();
        final FlagPoleState poleState = procedure.getActiveFlags(startTime, now);
        final TimePoint nextTime = poleState.getNextStateValidFrom();
        if (nextTime == null || nextTime.before(now)) {
            if (mLayout != null) {
                mLayout.setVisibility(View.GONE);
            }
            if (mRecall != null) {
                mRecall.setVisibility(View.GONE);
            }
            return;
        }

        if (mLayout != null) {
            mLayout.setVisibility(View.VISIBLE);
            if (mFlagsCache == null) {
                mLayout.removeAllViews();
                final FlagPole nextPole = FlagPoleState.getMostInterestingFlagPole(poleState.computeUpcomingChanges());
                final List<FlagPole> poles = poleState.getCurrentState();
                mFlagsCache = new FlagsCache(nextTime, nextPole);
                int size = 0;
                for (FlagPole pole : poles) {
                    size++;
                    final Flags upperFlag = pole.getUpperFlag();
                    final boolean isNext = isNextFlag(nextPole, upperFlag);
                    final boolean lastEntry = poles.size() == size;
                    mLayout.addView(createFlagView(now, poleState, upperFlag, isNext, lastEntry, pole.isDisplayed(), UPPER_FLAG));
                    final Flags lowerFlag = pole.getLowerFlag();
                    if (lowerFlag != Flags.NONE) {
                        mLayout.addView(createFlagView(now, poleState, lowerFlag, false, lastEntry, false, LOWER_FLAG));
                    }
                }
            } else {
                if (mFlagsCache.nextPole != null) {
                    for (int i = 0; i < mLayout.getChildCount(); i++) {
                        ImageView flagImage = ViewHelper.get(mLayout.getChildAt(i), R.id.flag);
                        Flags flags = (Flags) flagImage.getTag();
                        if (isNextFlag(mFlagsCache.nextPole, flags)) {
                            updateTextView(now, mLayout.getChildAt(i));
                        }
                    }
                }
            }
        }
    }

    private boolean isNextFlag(FlagPole flagPole, Flags flags) {
        return flagPole != null && flags.equals(flagPole.getUpperFlag());
    }

    private void updateTextView(TimePoint now, View flagView) {
        final TimePoint until = mFlagsCache.nextTime != null ? mFlagsCache.nextTime : now;
        final TextView textView = ViewHelper.get(flagView, R.id.flag_text);
        final String text = TimeUtils.formatDuration(now, until, false);
        textView.setText(text);
    }

    private RelativeLayout createFlagView(TimePoint now, FlagPoleState poleState, final Flags flag, boolean isNext,
                                          boolean lastEntry, boolean isDisplayed, int flagType) {
        RelativeLayout layout = (RelativeLayout) requireActivity().getLayoutInflater().inflate(R.layout.race_flag, mLayout,
                false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        layout.setLayoutParams(layoutParams);

        TimePoint changeAt = poleState.getNextStateValidFrom();
        ImageView flagView = ViewHelper.get(layout, R.id.flag);
        TextView textView = ViewHelper.get(layout, R.id.flag_text);
        View downView = ViewHelper.get(layout, R.id.arrow_down);
        View upView = ViewHelper.get(layout, R.id.arrow_up);
        View line = ViewHelper.get(layout, R.id.line);
        if (lastEntry) {
            line.setVisibility(View.GONE);
        }

        flagView.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), flag.name(), mFlagSize));
        flagView.setTag(flag);
        if (flag == Flags.CLASS && getRace().getFleet().getColor() != null) {
            flagView.setBackgroundColor(getFleetColorId());
        }
        flagView.setOnClickListener(v -> Toast.makeText(v.getContext(), flag.name(), Toast.LENGTH_SHORT).show());

        textView.setText(null);
        if (changeAt != null && isNext) {
            textView.setVisibility(View.VISIBLE);
            String timer = TimeUtils.formatDuration(now, changeAt);
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
        Util.Triple<Integer, Integer, Integer> rgb = getRace().getFleet().getColor() == null
                ? new Util.Triple<>(0, 0, 0)
                : getRace().getFleet().getColor().getAsRGB();
        return Color.rgb(rgb.getA(), rgb.getB(), rgb.getC());
    }

    private final RacingProcedureChangedListener procedureChangedListener = new BaseRacingProcedureChangedListener() {
        @Override
        public void onActiveFlagsChanged(ReadonlyRacingProcedure racingProcedure) {
            super.onActiveFlagsChanged(racingProcedure);
            mFlagsCache = null;
        }
    };

    private static class FlagsCache {
        private TimePoint nextTime;
        public FlagPole nextPole;

        public FlagsCache(TimePoint nextTime, FlagPole pole) {
            this.nextTime = nextTime;
            nextPole = pole;
        }
    }
}
