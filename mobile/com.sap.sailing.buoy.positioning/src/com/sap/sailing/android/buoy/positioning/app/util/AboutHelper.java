package com.sap.sailing.android.buoy.positioning.app.util;

import android.content.Context;
import android.content.Intent;

import com.sap.sailing.android.buoy.positioning.app.ui.activities.AboutActivity;

public class AboutHelper {

    public static void showInfoActivity(Context context) {
            Intent intent = new Intent(context, AboutActivity.class);
            context.startActivity(intent);
    }
}
