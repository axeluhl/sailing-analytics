package com.sap.sailing.android.shared.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AppUtils {

    private final static String TAG = AppUtils.class.getName();

    private AppUtils() {

    }

    public static PackageInfo getPackageInfo(Context app) {
        try {
            return app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public static String getBuildInfo(Context context) {
        String buildInfo = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(context.getAssets().open("build.info")));
            buildInfo = reader.readLine();
        } catch (IOException e) {
            Log.d(TAG, "Can't open file with build info", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return buildInfo;
    }
}
