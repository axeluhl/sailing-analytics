package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sap.sailing.android.shared.util.BitmapHelper;
import com.sap.sailing.domain.abstractlog.race.RaceLogEventVisitor;
import com.sap.sailing.domain.abstractlog.race.RaceLogFlagEvent;
import com.sap.sailing.domain.abstractlog.race.impl.BaseRaceLogEventVisitor;
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

    private final ImageView imageView;
    private final TextView textView;

    private final int flagSize;

    private RaceState state;
    private RaceLogRaceStatus status;
    private RacingProcedure procedure;
    private TimePoint startTime;
    private TimePoint finishingTime;
    private TimePoint nextTime;

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

    private final RaceLogEventVisitor raceLogFlagEventListener = new BaseRaceLogEventVisitor() {
        @Override
        public void visit(RaceLogFlagEvent event) {
            final TimePoint now = MillisecondsTimePoint.now();
            setVisibility(VISIBLE);
            setProcedureFlag(now);
            notifyTick(now);
        }
    };

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TickSingleton.INSTANCE.registerListener(this);
        if (state != null) {
            state.getRaceLog().addListener(raceLogFlagEventListener);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        TickSingleton.INSTANCE.unregisterListener(this);
        if (state != null) {
            state.getRaceLog().removeListener(raceLogFlagEventListener);
        }
    }

    public void setRaceState(RaceState state) {
        final TimePoint now = MillisecondsTimePoint.now();
        this.state = state;
        state.getRaceLog().addListener(raceLogFlagEventListener);
        status = state.getStatus();
        procedure = state.getTypedRacingProcedure();
        startTime = state.getStartTime();
        finishingTime = state.getFinishingTime();
        switch (status) {
            case SCHEDULED:
            case STARTPHASE:
                setVisibility(VISIBLE);
                setProcedureFlag(now);
                notifyTick(now);
                break;
            case RUNNING:
                if (procedure.isIndividualRecallDisplayed() && now.before(procedure.getIndividualRecallRemovalTime())) {
                    setVisibility(VISIBLE);
                    setProcedureFlag(now);
                    notifyTick(now);
                }
                break;
            case FINISHING:
                setVisibility(VISIBLE);
                setFinishingFlag(now);
                notifyTick(now);
                break;
            default:
                setVisibility(GONE);
                clear();
        }
    }

    @Override
    public void notifyTick(TimePoint now) {
        switch (status) {
            case SCHEDULED:
            case STARTPHASE:
            case RUNNING:
                if (nextTime != null && !now.before(nextTime)) {
                    setProcedureFlag(now);
                }
                if (nextTime != null) {
                    textView.setText(TimeUtils.formatDuration(now, nextTime, false));
                }
                break;
            case FINISHING:
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                textView.setText(TimeUtils.formatDurationSince(now.minus(finishingTime.asMillis()).asMillis(), false));
                break;
        }
    }

    private void setProcedureFlag(TimePoint now) {
        Drawable flag = null;
        Drawable arrow = null;
        final FlagPoleState poleState = procedure.getActiveFlags(startTime, now);
        nextTime = poleState.getNextStateValidFrom();
        if (nextTime == null) {
            setVisibility(GONE);
            clear();
            return;
        }
        final List<FlagPole> poles = poleState.getCurrentState();
        final FlagPole nextPole = FlagPoleState.getMostInterestingFlagPole(poleState.computeUpcomingChanges());
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

    private void setFinishingFlag(TimePoint now) {
        final FlagPoleState poleState = procedure.getActiveFlags(startTime, now);
        final List<FlagPole> poles = poleState.getCurrentState();
        if (poles.isEmpty()) {
            clear();
            return;
        }
        final Drawable flag = FlagsResources.getFlagDrawable(getContext(), poles.get(0).getUpperFlag().name(), flagSize);
        imageView.setImageDrawable(flag);
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
    }

    private void clear() {
        imageView.setImageDrawable(null);
        textView.setCompoundDrawables(null, null, null, null);
        textView.setText(null);
    }

    private boolean isNextFlag(Flags flag, FlagPole pole) {
        return pole != null && flag.equals(pole.getUpperFlag());
    }
}
