package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;

import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceTimeView extends android.support.v7.widget.AppCompatTextView implements TickListener {

    private TimePoint startTime;

    public RaceTimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
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
        startTime = state.getStartTime();
        switch (state.getStatus()) {
            case UNKNOWN:
            case UNSCHEDULED:
            case FINISHED:
                setVisibility(GONE);
                setText(null);
                break;
            default:
                setVisibility(VISIBLE);
                notifyTick(MillisecondsTimePoint.now());
        }
    }

    @Override
    public void notifyTick(TimePoint now) {
        if (startTime != null) {
            String duration = TimeUtils.formatDuration(now, startTime);
            setText(duration);
        }
    }
}
