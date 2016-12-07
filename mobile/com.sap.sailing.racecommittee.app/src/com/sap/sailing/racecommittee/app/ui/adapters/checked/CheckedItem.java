package com.sap.sailing.racecommittee.app.ui.adapters.checked;

import android.graphics.drawable.Drawable;

public class CheckedItem {
    protected Drawable mImage;
    protected String mText;
    protected String mSubText;
    protected boolean mDisabled = false;

    public Drawable getImage() {
        return mImage;
    }

    public void setImage(Drawable image) {
        mImage = image;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getSubtext() {
        return mSubText;
    }

    public void setSubtext(String subText) {
        mSubText = subText;
    }

    public boolean isEnabled() {
        return !mDisabled;
    }

    public void setDisabled(boolean isDisabled) {
        mDisabled = isDisabled;
    }
}
