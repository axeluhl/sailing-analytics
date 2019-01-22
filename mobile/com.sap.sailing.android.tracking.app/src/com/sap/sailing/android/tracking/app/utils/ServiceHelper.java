package com.sap.sailing.android.tracking.app.utils;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;

import android.content.Context;
import android.content.Intent;

/**
 * Helper class that starts services.
 *
 * @author Lukas Zielinski
 */
public class ServiceHelper {

    // private final static String TAG = ServiceHelper.class.getName();

    protected static ServiceHelper mInstance;

    public static synchronized ServiceHelper getInstance() {
        if (mInstance == null) {
            mInstance = new ServiceHelper();
        }
        return mInstance;
    }

    /**
     * Start tracking service with a given checkinDigest. The checkinDigest is used to determine the event from the
     * database in order to set the host-address correctly.
     *
     * @param checkinDigest
     *            of the event
     */
    public void startTrackingService(Context context, String checkinDigest) {
        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(context.getString(R.string.tracking_service_start));
        intent.putExtra(context.getString(R.string.tracking_service_checkin_digest_parameter), checkinDigest);
        context.startService(intent);
    }

    /**
     * Stop tracking service.
     */
    public void stopTrackingService(Context context) {
        Intent intent = new Intent(context, TrackingService.class);
        intent.setAction(context.getString(R.string.tracking_service_stop));
        context.startService(intent);
    }
}