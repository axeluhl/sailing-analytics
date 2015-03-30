package com.sap.sailing.racecommittee.app.utils;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorRes;
import android.util.TypedValue;

public class ColorHelper {

    public static @ColorRes int getThemedColor(Context context, @AttrRes int colorId) {
        int color = 0;
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        if (theme.resolveAttribute(colorId, typedValue, true)) {
            color = context.getResources().getColor(typedValue.resourceId);
        }
        return color;
    }
}
