package com.sap.sailing.racecommittee.app.utils;

import android.app.Activity;
import com.sap.sailing.racecommittee.app.AppConstants;
import com.sap.sailing.racecommittee.app.AppPreferences;
import com.sap.sailing.racecommittee.app.R;

public class ThemeHelper {

    public static void setTheme(Activity activity) {
        String theme = AppPreferences.on(activity).getTheme();
        if (theme.equals(AppConstants.LIGHT_THEME)) {
            activity.setTheme(R.style.AppTheme_Light);
        } else {
            activity.setTheme(R.style.AppTheme_Dark);
        }
    }
}
