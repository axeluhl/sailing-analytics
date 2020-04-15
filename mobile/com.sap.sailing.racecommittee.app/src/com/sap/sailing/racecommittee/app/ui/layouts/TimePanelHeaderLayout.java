package com.sap.sailing.racecommittee.app.ui.layouts;

import com.sap.sailing.racecommittee.app.ui.utils.TouchEventListener;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class TimePanelHeaderLayout extends FrameLayout {

    private TouchEventListener mTouchEventListener;

    public TimePanelHeaderLayout(Context context) {
        this(context, null);
    }

    public TimePanelHeaderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimePanelHeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mTouchEventListener = new TouchEventListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mTouchEventListener != null) {
            return mTouchEventListener.onTouchEvent(event);
        } else {
            return super.onTouchEvent(event);
        }
    }

    public void setRunnable(Runnable clickRunnable) {
        if (mTouchEventListener != null) {
            mTouchEventListener.setClickRunnable(clickRunnable);
        }
    }
}
