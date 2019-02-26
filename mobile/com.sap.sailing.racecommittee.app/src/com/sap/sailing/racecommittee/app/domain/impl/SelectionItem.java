package com.sap.sailing.racecommittee.app.domain.impl;

import android.graphics.drawable.Drawable;

public class SelectionItem {
    private String mCaption;
    private String mValue;
    private Drawable mDrawable;
    private Runnable mRunnable;
    private boolean mSwitch;
    private boolean mChecked;

    public SelectionItem(String caption, String value, Drawable drawable, boolean isSwitch, boolean checked,
            Runnable runnable) {
        mCaption = caption;
        mDrawable = drawable;
        mValue = value;
        mSwitch = isSwitch;
        mChecked = checked;
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

    public boolean isSwitch() {
        return mSwitch;
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    public boolean isChecked() {
        return mChecked;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public Runnable getRunnable() {
        return mRunnable;
    }
}
