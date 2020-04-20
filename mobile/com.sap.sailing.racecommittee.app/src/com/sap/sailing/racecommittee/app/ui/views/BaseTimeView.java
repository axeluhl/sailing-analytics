package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

abstract public class BaseTimeView extends android.support.v7.widget.AppCompatTextView implements TickListener {

    public BaseTimeView(Context context, @Nullable AttributeSet attrs) {
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
}
