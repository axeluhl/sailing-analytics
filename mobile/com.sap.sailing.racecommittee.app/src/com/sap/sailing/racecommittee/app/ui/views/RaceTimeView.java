package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;
import com.sap.sse.common.impl.MillisecondsTimePoint;

public class RaceTimeView extends android.support.v7.widget.AppCompatTextView implements TickListener {

    private RaceState state;

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
        this.state = state;
        notifyTick(MillisecondsTimePoint.now());
    }

    @Override
    public void notifyTick(TimePoint now) {
        if (state == null || state.getStartTime() == null) {
            setVisibility(View.GONE);
            return;
        }
        setVisibility(View.VISIBLE);
        String duration = TimeUtils.formatDuration(now, state.getStartTime());
        setText(duration);
    }
}
