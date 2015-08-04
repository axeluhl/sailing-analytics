package com.sap.sailing.android.shared.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Log;

public class AppUtils {

    private final static String TAG = AppUtils.class.getName();

    private Context mContext;

    private AppUtils(Context context) {
        mContext = context;
    }

    public static AppUtils with(Context context) {
        return new AppUtils(context);
    }

    public PackageInfo getPackageInfo() {
        try {
            return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public String getBuildInfo() {
        String buildInfo = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open("build.info")));
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

    public boolean isTablet() {
        int screenLayout = mContext.getResources().getConfiguration().screenLayout;
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && (
            ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) || (
                (screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE)));
    }
}
