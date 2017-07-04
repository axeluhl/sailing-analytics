package com.sap.sailing.android.shared.util;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

public class UniqueDeviceUuid {

    private static final String FILENAME = "Installation";
    private static final String UUID_KEY = "UUID";

    private static String mDeviceId;

    @SuppressLint("ApplySharedPref")
    public synchronized static String getUniqueId(Context context) {
        if (mDeviceId == null) {
            SharedPreferences preferences = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
            mDeviceId = preferences.getString(UUID_KEY, null);
            if (TextUtils.isEmpty(mDeviceId)) {
                mDeviceId = UUID.randomUUID().toString();
                preferences.edit().putString(UUID_KEY, mDeviceId).commit();
            }
        }

        return mDeviceId;
    }
}
