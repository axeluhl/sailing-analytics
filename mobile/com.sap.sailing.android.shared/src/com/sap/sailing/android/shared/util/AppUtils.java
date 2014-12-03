package com.sap.sailing.android.shared.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class AppUtils {
    public static PackageInfo getPackageInfo(Context app) {
        try {
            return app.getPackageManager().getPackageInfo(app.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }
}
