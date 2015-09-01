package com.sap.sailing.racecommittee.app.domain.impl;

import android.graphics.drawable.Drawable;

public class MainScheduleItem {
    private String mCaption;
    private String mValue;
    private Drawable mDrawable;
    private Runnable mRunnable;

    public MainScheduleItem(String caption, String value, Drawable drawable, Runnable runnable) {
        mCaption = caption;
        mDrawable = drawable;
        mValue = value;
        mRunnable = runnable;
    }

    public String getCaption() {
        return mCaption;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String value) {
        mValue = value;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public Runnable getRunnable() {
        return mRunnable;
    }
}
