package com.sap.sailing.android.shared.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.sap.sailing.android.shared.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.DisplayMetrics;
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

    /**
     * Locks the phone screen to portrait and tablets to landscape
     *
     * @param activity
     *            Activity to lock
     */
    public static void lockOrientation(Activity activity) {
        if (with(activity).isPhone()) {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        } else {
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        }
    }

    public PackageInfo getPackageInfo() {
        try {
            return mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    public String getInstallerPackageName() {
        return mContext.getPackageManager().getInstallerPackageName(mContext.getPackageName());
    }

    public String getStoreName() {
        String installer = getInstallerPackageName();
        if (!TextUtils.isEmpty(installer)) {
            String[] storeNames = mContext.getResources().getStringArray(R.array.store_names);
            String[] storePackages = mContext.getResources().getStringArray(R.array.store_packages);
            int len = storePackages.length;
            for (int i = 0; i < len; i++) {
                if (installer.equals(storePackages[i])) {
                    return storeNames[i];
                }
            }
        }

        return mContext.getString(R.string.sideload);
    }

    public boolean isSideLoaded() {
        return getStoreName().equals(mContext.getString(R.string.sideload));
    }

    /**
     * Read the build.info from the asset folder, which is written at build time with git information
     *
     * @return content of the build.info (or build_gradle.info) file or an empty string, if no file was found
     */
    public String getBuildInfo() {
        String buildInfo = "";
        BufferedReader reader = null;
        try {
            String file = "build.info";
            String[] files = mContext.getAssets().list("");
            for (String item : files) {
                if ("build_gradle.info".equals(item)) {
                    file = item;
                    break;
                }
            }
            reader = new BufferedReader(new InputStreamReader(mContext.getAssets().open(file)));
            buildInfo = reader.readLine();
        } catch (IOException e) {
            Log.d(TAG, "Can't open file with build info", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error trying to read build info", e);
                }
            }
        }

        return buildInfo;
    }

    /**
     * Returns true, if device is identified as phone
     *
     * @return true, if probably a phone
     */
    public boolean isPhone() {
        return !isTablet();
    }

    /**
     * Returns true, if device is identified as tablet
     *
     * @return true, if probably a tablet
     */
    public boolean isTablet() {
        return is7inch() || is10inch();
    }

    /**
     * Returns true, if device is identified as 7" tablet or larger
     *
     * @return true, if probably min 7" tablet
     */
    public boolean is7inch() {
        return (getSmallestWidth() >= 600);
    }

    /**
     * Returns true, if device is identified as 10" tablet or larger
     *
     * @return true, if probably min 10" tablet
     */
    public boolean is10inch() {
        return (getSmallestWidth() >= 720);
    }

    /**
     * Returns true, is device is used in portrait mode
     *
     * @return true, if portrait mode
     */
    public boolean isPortrait() {
        return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    /**
     * Returns true, if device is used in landscape mode
     *
     * @return true, if landscape mode
     */
    public boolean isLandscape() {
        return mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * Returns true, if device has a medium density screen
     *
     * @return true, if device is used in medium density mode
     */
    public boolean isMDPI() {
        return getDensity() == DisplayMetrics.DENSITY_MEDIUM;
    }

    /**
     * Returns true, if device has a high density screen
     *
     * @return true, if device is used in high density mode
     */
    public boolean isHDPI() {
        return getDensity() == DisplayMetrics.DENSITY_HIGH;
    }

    private int getDensity() {
        return mContext.getResources().getDisplayMetrics().densityDpi;
    }

    private int getSmallestWidth() {
        return mContext.getResources().getConfiguration().smallestScreenWidthDp;
    }
}
