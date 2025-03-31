package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
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
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

import java.util.List;

public class FlagTimeView extends LinearLayout {

    private final ImageView imageView;
    private final TextView textView;

    private final int flagSize;

    private RaceState state;
    private TimePoint startTime;
    private TimePoint finishingTime;
    private TimePoint nextTime;

    private TickListener listener;

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
        if (state != null) {
            state.getRacingProcedure().addChangedListener(procedureChangedListener);
        }
        if (listener != null) {
            final TimePoint timePoint = startTime != null ? startTime : finishingTime;
            TickSingleton.INSTANCE.registerListener(listener, timePoint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (state != null) {
            state.getRacingProcedure().removeChangedListener(procedureChangedListener);
        }
        unregisterListener();
    }

    public void setRaceState(RaceState state) {
        final TimePoint timePoint = MillisecondsTimePoint.now();
        this.state = state;
        state.getRacingProcedure().addChangedListener(procedureChangedListener);
        final RacingProcedure procedure = state.getTypedRacingProcedure();
        startTime = state.getStartTime();
        finishingTime = state.getFinishingTime();
        unregisterListener();
        switch (state.getStatus()) {
            case SCHEDULED:
            case STARTPHASE:
            case RUNNING:
                checkFlag(procedure, timePoint);
                break;
            case FINISHING:
                checkFinishingFlag(procedure, timePoint);
                break;
            default:
                setVisibility(GONE);
                clear();
        }
    }

    private void unregisterListener() {
        if (listener != null) {
            TickSingleton.INSTANCE.unregisterListener(listener);
            listener = null;
        }
    }

    private void checkFlag(ReadonlyRacingProcedure procedure, TimePoint timePoint) {
        if (startTime == null) {
            setVisibility(GONE);
            clear();
            return;
        }
        final FlagPoleState poleState = procedure.getActiveFlags(startTime, timePoint);
        nextTime = poleState.getNextStateValidFrom();
        if (nextTime == null || nextTime.before(timePoint)) {
            setVisibility(GONE);
            clear();
            return;
        }
        setVisibility(VISIBLE);
        setFlag(poleState);
        listener = now -> textView.setText(TimeUtils.formatDuration(now, nextTime != null ? nextTime : now, false));
        TickSingleton.INSTANCE.registerListener(listener, startTime);
    }

    private void setFlag(FlagPoleState poleState) {
        Drawable flag = null;
        Drawable arrow = null;
        final FlagPole nextPole = FlagPoleState.getMostInterestingFlagPole(poleState.computeUpcomingChanges());
        final List<FlagPole> poles = poleState.getCurrentState();
        for (FlagPole pole : poles) {
            final Flags upperFlag = pole.getUpperFlag();
            if (isNextFlag(upperFlag, nextPole)) {
                flag = FlagsResources.getFlagDrawable(getContext(), upperFlag.name(), flagSize);
                arrow = BitmapHelper.getAttrDrawable(getContext(), nextPole.isDisplayed() ? R.attr.arrow_up : R.attr.arrow_down);
            } else {
                final Flags lowerFlag = pole.getLowerFlag();
                if (lowerFlag != Flags.NONE) {
                    if (isNextFlag(lowerFlag, nextPole)) {
                        flag = FlagsResources.getFlagDrawable(getContext(), lowerFlag.name(), flagSize);
                        arrow = BitmapHelper.getAttrDrawable(getContext(), R.attr.arrow_up);
                    }
                }
            }
        }
        imageView.setImageDrawable(flag);
        textView.setCompoundDrawablesWithIntrinsicBounds(arrow, null, null, null);
    }

    private void checkFinishingFlag(ReadonlyRacingProcedure procedure, TimePoint timePoint) {
        if (startTime == null || finishingTime == null) {
            setVisibility(GONE);
            clear();
            return;
        }
        final FlagPoleState poleState = procedure.getActiveFlags(startTime, timePoint);
        final List<FlagPole> poles = poleState.getCurrentState();
        if (poles.isEmpty()) {
            setVisibility(GONE);
            clear();
            return;
        }
        final Drawable flag = FlagsResources.getFlagDrawable(getContext(), poles.get(0).getUpperFlag().name(), flagSize);
        setVisibility(VISIBLE);
        imageView.setImageDrawable(flag);
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        listener = now -> {
            final long millis = now.minus(finishingTime != null ? finishingTime.asMillis() : now.asMillis()).asMillis();
            textView.setText(TimeUtils.formatDurationSince(millis, false));
        };
        TickSingleton.INSTANCE.registerListener(listener, finishingTime);
    }

    private void clear() {
        imageView.setImageDrawable(null);
        textView.setCompoundDrawables(null, null, null, null);
        textView.setText(null);
    }

    private boolean isNextFlag(Flags flag, FlagPole pole) {
        return pole != null && flag.equals(pole.getUpperFlag());
    }

    private final RacingProcedureChangedListener procedureChangedListener = new BaseRacingProcedureChangedListener() {
        @Override
        public void onActiveFlagsChanged(ReadonlyRacingProcedure procedure) {
            super.onActiveFlagsChanged(procedure);
            unregisterListener();
            checkFlag(procedure, MillisecondsTimePoint.now());
        }
    };
}
