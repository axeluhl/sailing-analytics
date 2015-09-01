package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import android.graphics.drawable.Drawable;

public abstract class CheckedListItem {

    protected Drawable image;
    protected String text;
    protected String subText;

    public Drawable getImage(){
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSubtext() {
        return null;
    }

    public void setSubtext(String subText) {
        this.subText = subText;
    }
}
