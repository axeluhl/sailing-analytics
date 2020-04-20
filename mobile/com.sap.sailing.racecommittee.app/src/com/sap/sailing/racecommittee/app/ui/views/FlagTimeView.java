package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import com.sap.sailing.domain.abstractlog.race.state.RaceState;
import com.sap.sailing.domain.common.racelog.RaceLogRaceStatus;
import com.sap.sailing.racecommittee.app.R;
import com.sap.sailing.racecommittee.app.utils.TimeUtils;
import com.sap.sse.common.TimePoint;

public class FlagTimeView extends BaseTimeView {
    private RaceState state;
    private String timer;

    public FlagTimeView(@NonNull final Context context, final @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRaceState(RaceState state) {
        this.state = state;
    }

    public void setTimer(String timer) {
        this.timer = timer;
    }

    @Override
    public void notifyTick(TimePoint now) {
        if (state == null) { return; }
        if (state.getStatus() == RaceLogRaceStatus.FINISHING) {
            final String time = TimeUtils.formatDurationSince(now.minus(state.getFinishingTime().asMillis()).asMillis());
            setText(time);
        } else {
            setText(timer);
        }
    }
}
