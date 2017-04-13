package com.sap.sailing.racecommittee.app.utils;

import android.annotation.TargetApi;
import android.os.Build;

public final class GeneralHelper {

    private GeneralHelper() {

    }

    /**
     * Same as java.util.Object.equals (available at API 19 - we use minAPI 14)
     *
     * @param first  object to check
     * @param second object to check
     * @return true, if both objects are the same, else false
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean equals(Object first, Object second) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            return java.util.Objects.equals(first, second);
        } else {
            return (first == null) ? (second == null) : first.equals(second);
        }
    }
}
