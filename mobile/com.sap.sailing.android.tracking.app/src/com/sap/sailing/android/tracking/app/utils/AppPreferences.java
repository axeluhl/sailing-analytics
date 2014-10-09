package com.sap.sailing.android.tracking.app.utils;

import android.content.Context;

import com.sap.sailing.android.shared.util.PrefUtils;
import com.sap.sailing.android.tracking.app.R;

public class AppPreferences {
    protected final Context context;

    public AppPreferences(Context context) {
        this.context = context;
    }
    
    public String getDeviceIdentifier() {
        return PrefUtils.getString(context, R.string.preference_device_identifier_key,
                R.string.preference_device_identifier_default);
    }
    
    public String getServerURL() {
        return PrefUtils.getString(context, R.string.preference_server_url_key,
                R.string.preference_server_url_default);
    }
}
