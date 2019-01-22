package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;

public class RaceTimeView extends android.support.v7.widget.AppCompatTextView implements TickListener {

    private RaceState state;

    public RaceTimeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRaceState(RaceState state) {
        this.state = state;
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

    @Override
    public void notifyTick(TimePoint now) {
        if (state == null) {
            return;
        }
        if (state.getStartTime() == null) {
            return;
        }
        String duration = TimeUtils.formatDuration(now, state.getStartTime());
        setText(duration);
        float textSize = getContext().getResources().getDimension(R.dimen.textSize_40);
        if (!TextUtils.isEmpty(duration) && duration.length() >= 6) {
            textSize = getContext().getResources().getDimension(R.dimen.textSize_32);
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }
}
