package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.FlagPoleState;
import com.sap.sailing.domain.abstractlog.race.state.racingprocedure.RacingProcedure;
import com.sap.sailing.domain.common.racelog.FlagPole;
import com.sap.sailing.domain.common.racelog.Flags;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.ui.utils.FlagsResources;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.List;

public class FlagTimeView extends LinearLayout implements TickListener {

    private static final String TAG = FlagTimeView.class.getSimpleName();

    private final ImageView imageView;
    private final TextView textView;

    private final int flagSize;

    private RaceState state;

    public FlagTimeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOrientation(LinearLayout.VERTICAL);
        inflate(context, R.layout.layout_flag_time, this);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.FlagTimeView, 0, 0);
        try {
            flagSize = a.getInteger(R.styleable.FlagTimeView_flagSize, 0);
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TickSingleton.INSTANCE.registerListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        TickSingleton.INSTANCE.unregisterListener(this);
    }

    public void setRaceState(RaceState state) {
        this.state = state;
        notifyTick(MillisecondsTimePoint.now());
    }

    @Override
    public void notifyTick(TimePoint now) {
        if (state == null || state.getStartTime() == null || state.getFinishedTime() != null) {
            setVisibility(View.GONE);
            return;
        }

        RacingProcedure procedure = state.getTypedRacingProcedure();
        LayerDrawable flag = null;
        Drawable arrow = null;
        String duration = null;
        if (!procedure.isIndividualRecallDisplayed()) {
            FlagPoleState poleState = state.getRacingProcedure().getActiveFlags(state.getStartTime(), now);
            List<FlagPole> currentState = poleState.getCurrentState();
            List<FlagPole> upcoming = poleState.computeUpcomingChanges();
            FlagPole nextPole = FlagPoleState.getMostInterestingFlagPole(upcoming);
            TimePoint change = poleState.getNextStateValidFrom();
            Flags currentFlag;

            if (change != null) {
                for (FlagPole pole : currentState) {
                    int isNext = 0;

                    currentFlag = pole.getUpperFlag();
                    if (isNextFlag(currentFlag, nextPole)) {
                        isNext = 1;
                    } else {
                        currentFlag = pole.getLowerFlag();
                        if (!Flags.NONE.equals(currentFlag)) {
                            if (isNextFlag(currentFlag, nextPole)) {
                                isNext = 2;
                            }
                        }
                    }

                    if (isNext != 0) {
                        flag = FlagsResources.getFlagDrawable(getContext(), currentFlag.name(), flagSize);
                        switch (isNext) {
                            case 1:
                                if (nextPole.isDisplayed()) {
                                    arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_up);
                                } else {
                                    arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_down);
                                }
                                break;

                            case 2:
                                arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_up);
                                break;

                            default:
                                ExLog.i(getContext(), TAG, "unknown flag");
                        }
                        duration = TimeUtils.formatDuration(now, poleState.getNextStateValidFrom());
                    }
                }
            } else if (state.getStatus() == RaceLogRaceStatus.FINISHING) {
                if (!currentState.isEmpty()) {
                    flag = FlagsResources.getFlagDrawable(getContext(), currentState.get(0).getUpperFlag().name(),
                            flagSize);
                }
                arrow = null;
                duration = TimeUtils.formatDurationSince(now.minus(state.getFinishingTime().asMillis()).asMillis(), false);
            }
        } else {
            TimePoint flagDown = procedure.getIndividualRecallRemovalTime();
            if (now.before(flagDown)) {
                flag = FlagsResources.getFlagDrawable(getContext(), Flags.XRAY.name(), flagSize);
                arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_down);
                duration = TimeUtils.formatDuration(now, flagDown);
            }
        }
        if (duration != null) {
            duration = duration.replace("-", "");
        }

        if (flag == null) {
            setVisibility(View.GONE);
            return;
        }

        setVisibility(View.VISIBLE);
        imageView.setImageDrawable(flag);
        textView.setCompoundDrawablesWithIntrinsicBounds(arrow, null, null, null);
        textView.setText(duration);
    }

    private boolean isNextFlag(Flags flag, FlagPole pole) {
        return pole != null && flag.equals(pole.getUpperFlag());
    }
}
