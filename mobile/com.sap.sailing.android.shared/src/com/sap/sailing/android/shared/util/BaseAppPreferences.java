package com.sap.sailing.android.shared.util;

import com.sap.sailing.android.shared.R;

import android.content.Context;
import android.content.SharedPreferences;

public class BaseAppPreferences {
    protected final Context context;
    protected final SharedPreferences preferences;

    public BaseAppPreferences(Context context) {
        this.context = context;
        // multi process mode so that services read consistent values
        preferences = context.getSharedPreferences(BaseAppPreferences.class.getName(), Context.MODE_MULTI_PROCESS);
    }

    public String getDeviceIdentifier() {
        return UniqueDeviceUuid.getUniqueId(context);
    }

    public String getLastScannedQRCode() {
        return preferences.getString(context.getString(R.string.preference_last_scanned_qr_code), null);
    }

    public void setLastScannedQRCode(String lastQRCode) {
        preferences.edit().putString(context.getString(R.string.preference_last_scanned_qr_code), lastQRCode).apply();
    }
}
