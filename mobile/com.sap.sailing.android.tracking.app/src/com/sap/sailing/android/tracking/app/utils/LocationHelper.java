package com.sap.sailing.android.tracking.app.utils;

import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class LocationHelper {

    /**
     * Helper method to check if device receives gps updates
     * @param context
     * @return
     */
    public static boolean isGPSEnabled(Context context) {
        LocationManager service = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Helper method to access androids location settings
     * @param context
     */
    public static void openLocationSettings(Context context) {
        context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }
}
