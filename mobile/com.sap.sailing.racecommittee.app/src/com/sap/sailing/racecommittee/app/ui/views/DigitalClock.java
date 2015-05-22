package com.sap.sailing.racecommittee.app.ui.views;

import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.widget.TextView;
import com.sap.sailing.racecommittee.app.utils.TickListener;
import com.sap.sailing.racecommittee.app.utils.TickSingleton;

import java.util.Calendar;

public class DigitalClock extends TextView implements TickListener {
    private Calendar calendar = Calendar.getInstance();
    private final static String mFormat = "k:mm:ss";

    private boolean timerIsStopped = false;

    public DigitalClock(Context context) {
        super(context);
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DigitalClock(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onAttachedToWindow() {
        timerIsStopped = false;
        super.onAttachedToWindow();
        notifyTick();
        TickSingleton.INSTANCE.registerListener(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        TickSingleton.INSTANCE.unregisterListener(this);
        timerIsStopped = true;
    }

    public void notifyTick() {
        if (timerIsStopped) {
            return;
        }
        calendar.setTimeInMillis(System.currentTimeMillis());
        setText(DateFormat.format(mFormat, calendar));
        invalidate();
    }
}
