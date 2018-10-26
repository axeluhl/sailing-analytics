package com.sap.sailing.racecommittee.app.ui.fragments.raceinfo;

import java.util.List;

import com.sap.sailing.android.shared.util.ViewHelper;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.ReadonlyRacingProcedure;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.impl.BaseRacingProcedureChangedListener;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.Util;
import com.sap.sse.common.impl.MillisecondsTimePoint;

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

public class RaceFlagViewerFragment extends BaseFragment {

    private static final int UPPER_FLAG = 0;
    private static final int LOWER_FLAG = 1;

    private LinearLayout mLayout;
    private View mRecall;

    private ImageView mXrayFlag;
    private TextView mXrayCountdown;
    private Button mXrayButton;

    private ProcedureChangedListener mProcedureListener;
    private FlagsCache mFlagCache;
    private int mFlagSize;

    public RaceFlagViewerFragment() {
        mProcedureListener = new ProcedureChangedListener();
        mFlagCache = null;
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

        mLayout = ViewHelper.get(layout, R.id.flags);
        mRecall = ViewHelper.get(layout, R.id.individual_recall);
        mFlagSize = getResources().getInteger(R.integer.flag_size_xlarge);

        mXrayButton = ViewHelper.get(layout, R.id.flag_down);
        if (mXrayButton != null) {
            mXrayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    RacingProcedure procedure = getRaceState().getTypedRacingProcedure();
                    procedure.removeIndividualRecall(MillisecondsTimePoint.now());
                }
            });
        }
        mXrayFlag = ViewHelper.get(layout, R.id.flag);
        if (mXrayFlag != null) {
            mXrayFlag.setImageDrawable(FlagsResources.getFlagDrawable(getActivity(), Flags.XRAY.name(), mFlagSize));
        }
        mXrayCountdown = ViewHelper.get(layout, R.id.xray_down);

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();

        mFlagCache = null;
        getRaceState().getRacingProcedure().addChangedListener(mProcedureListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        getRaceState().getRacingProcedure().removeChangedListener(mProcedureListener);
    }

    @Override
    public void notifyTick(TimePoint now) {
        super.notifyTick(now);

        updateFlags(now);
    }

    private void updateFlags(TimePoint now) {
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
                if (mFlagCache == null) {
                    mLayout.removeAllViews();
                    FlagPoleState poleState = getRaceState().getRacingProcedure()
                            .getActiveFlags(getRaceState().getStartTime(), now);
                    List<FlagPole> currentState = poleState.getCurrentState();
                    List<FlagPole> upcoming = poleState.computeUpcomingChanges();
                    FlagPole nextPole = FlagPoleState.getMostInterestingFlagPole(upcoming);
                    mFlagCache = new FlagsCache(poleState.getNextStateValidFrom(), nextPole);
                    int size = 0;
                    Flags flag;
                    for (FlagPole flagPole : currentState) {
                        size++;
                        flag = flagPole.getUpperFlag();
                        mLayout.addView(createFlagView(now, poleState, flag, isNextFlag(nextPole, flag),
                                currentState.size() == size, flagPole.isDisplayed(), UPPER_FLAG));
                        if (!flagPole.getLowerFlag().equals(Flags.NONE)) {
                            flag = flagPole.getLowerFlag();
                            mLayout.addView(createFlagView(now, poleState, flag, isNextFlag(nextPole, flag),
                                    currentState.size() == size, false, LOWER_FLAG));
                        }
                    }
                } else {
                    if (mFlagCache.nextPole != null) {
                        for (int i = 0; i < mLayout.getChildCount(); i++) {
                            ImageView flagImage = ViewHelper.get(mLayout.getChildAt(i), R.id.flag);
                            Flags flags = (Flags) flagImage.getTag();
                            if (isNextFlag(mFlagCache.nextPole, flags)) {
                                updateFlagView(now, mLayout.getChildAt(i));
                            }
                        }
                    }
                }
            }
        } else {
            if (mXrayCountdown != null && mRecall != null) {
                TimePoint flagDown = procedure.getIndividualRecallRemovalTime();
                if (now.before(flagDown)) {
                    mRecall.setVisibility(View.VISIBLE);
                    mXrayCountdown.setText(
                            getString(R.string.until_xray_removed, TimeUtils.formatDuration(now, flagDown, false)));
                }
            }
        }
    }

    private boolean isNextFlag(FlagPole flagPole, Flags flags) {
        return flagPole != null && flags.equals(flagPole.getUpperFlag());
    }

    private View updateFlagView(TimePoint now, View flagView) {
        TimePoint change = mFlagCache.mChange;
        if (change != null) {
            TextView flag_text = ViewHelper.get(flagView, R.id.flag_text);
            String timer = TimeUtils.formatDuration(now, change, false);
            flag_text.setText(timer);
        }
        return flagView;
    }

    private RelativeLayout createFlagView(TimePoint now, FlagPoleState poleState, final Flags flag, boolean isNext,
            boolean lastEntry, boolean isDisplayed, int flagType) {
        RelativeLayout layout = (RelativeLayout) getActivity().getLayoutInflater().inflate(R.layout.race_flag, mLayout,
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
        flagView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), flag.name(), Toast.LENGTH_SHORT).show();
            }
        });

        textView.setText("");
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

    private class ProcedureChangedListener extends BaseRacingProcedureChangedListener {

        @Override
        public void onActiveFlagsChanged(ReadonlyRacingProcedure racingProcedure) {
            super.onActiveFlagsChanged(racingProcedure);

            mFlagCache = null;
        }
    }

    private class FlagsCache {
        public FlagPole nextPole;
        private TimePoint mChange;

        public FlagsCache(TimePoint change, FlagPole pole) {
            mChange = change;
            nextPole = pole;
        }
    }
}
