package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import android.graphics.drawable.Drawable;

public class CourseItem implements CheckedListItem{

    private Drawable image;
    private String text;
    private String subText;
    private boolean checked;

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable drawable) {
        image = drawable;
    }

    public String getText() {
        return text;
    }

    public void setText(String text){
        this.text = text;
    }

    public String getSubtext() {
        return subText;
    }

    public void setSubText(String subText) {
        this.subText = subText;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

}
