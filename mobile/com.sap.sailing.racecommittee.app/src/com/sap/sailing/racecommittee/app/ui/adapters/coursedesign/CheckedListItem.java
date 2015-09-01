package com.sap.sailing.racecommittee.app.ui.adapters.coursedesign;

import android.graphics.drawable.Drawable;

public interface CheckedListItem {

    Drawable getImage();

    String getText();

    String getSubtext();

    boolean isChecked();
}
