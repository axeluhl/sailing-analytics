package com.sap.sailing.android.buoy.positioning.app.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sap.sailing.android.buoy.positioning.app.R;
import com.sap.sailing.android.buoy.positioning.app.service.MarkerService;

public class MarkerUtils {

    private Context mContext;
    private static PendingIntent mPendingIntent;
    private static int mRequestCode = 42;

    private MarkerUtils(Context context) {
        mContext = context;
    }

    public static MarkerUtils withContext(Context context) {
        return new MarkerUtils(context);
    }

    public void startMarkerService(String checkinUrl) {
        AppPreferences preferences = new AppPreferences(mContext);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(mContext, MarkerService.class);
        intent.putExtra(mContext.getString(R.string.check_in_url_key), checkinUrl);
        int flags;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
        } else {
            flags = PendingIntent.FLAG_UPDATE_CURRENT;
        }
        mPendingIntent = PendingIntent.getService(mContext, mRequestCode, intent, flags);
        long dataRefreshInterval = preferences.getDataRefreshInterval() * 1000;
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), dataRefreshInterval,
                mPendingIntent);
    }

    public void stopMarkerService() {
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(mPendingIntent);
        mPendingIntent = null;
    }

    public void restartMarkerService() {
        if (mPendingIntent != null) {
            AppPreferences preferences = new AppPreferences(mContext);
            AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            long dataRefreshInterval = preferences.getDataRefreshInterval() * 1000;
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), dataRefreshInterval,
                    mPendingIntent);
        }
    }
}
