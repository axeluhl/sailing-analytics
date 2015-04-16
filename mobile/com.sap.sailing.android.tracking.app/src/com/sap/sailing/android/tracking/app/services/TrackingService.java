package com.sap.sailing.android.tracking.app.services;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

public class TrackingService extends Service
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private NotificationManager notificationManager;
    private boolean locationUpdateRequested = false;
    private AppPreferences prefs;
    private ScheduledExecutorService scheduler;

    private GPSQualityListener gpsQualityListener;
    private final IBinder trackingBinder = new TrackingBinder();

    private static final String TAG = TrackingService.class.getName();

    public static final String WEB_SERVICE_PATH = "/sailingserver/api/v1/gps_fixes";
    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION_ID = R.string.tracker_started;

    private final int UPDATE_INTERVAL_DEFAULT = 3000;
    private final int UPDATE_INTERVAL_POWERSAVE_MODE = 30000;
    private final float BATTERY_POWER_SAVE_TRESHOLD = 0.2f;

    private String checkinDigest;
    private EventInfo event;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new AppPreferences(this);

        // http://developer.android.com/training/location/receive-location-updates.html
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(prefs.getGPSFixInterval());
        locationRequest.setFastestInterval(prefs.getGPSFixFastestInterval());

        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this).build();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (BuildConfig.DEBUG) {
                ExLog.i(this, TAG, "Starting Tracking Service with checkinDigest: " + checkinDigest);
            }

            if (intent.getAction() != null) {
                if (intent.getAction().equals(getString(R.string.tracking_service_stop))) {
                    stopTracking();
                } else {
                    if (intent.getExtras() != null) {
                        checkinDigest = intent.getExtras()
                            .getString(getString(R.string.tracking_service_checkin_digest_parameter));

                        event = DatabaseHelper.getInstance().getEventInfo(this, checkinDigest);

                        if (BuildConfig.DEBUG) {
                            ExLog.i(this, TAG, "Starting Tracking Service with checkinDigest: " + checkinDigest);
                        }

                        startTracking();
                    }
                }
            } else {
                stopTracking();
            }
        }
        return Service.START_STICKY;
    }

    public void startTracking() {
        googleApiClient.connect();
        locationUpdateRequested = true;

        ExLog.i(this, TAG, "Started Tracking");
        // showNotification();

        prefs.setTrackerIsTracking(true);
        prefs.setTrackerIsTrackingCheckinDigest(checkinDigest);
    }

    public void stopTracking() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
        googleApiClient.disconnect();
        locationUpdateRequested = false;

        if (scheduler != null) {
            scheduler.shutdown();
        }

        prefs.setTrackerIsTracking(false);
        prefs.setTrackerIsTrackingCheckinDigest(null);

        stopSelf();
        ExLog.i(this, TAG, "Stopped Tracking");
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        ExLog.e(this, TAG, "Failed to connect to Google Play Services for location updates");
    }

    @Override
    public void onConnected(Bundle arg0) {
        if (locationUpdateRequested) {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void reportGPSQualityBearingAndSpeed(float gpsAccurracy, float bearing, float speed, double latitude,
        double longitude, double altitude) {

        if (prefs.getDisplayHeadingWithSubtractedDeclination()) {
            GeomagneticField geomagneticField = new GeomagneticField((float) latitude, (float) longitude,
                (float) altitude, System.currentTimeMillis());
            bearing = bearing - geomagneticField.getDeclination();
        }

        GPSQuality quality = GPSQuality.noSignal;
        if (gpsQualityListener != null) {
            if (gpsAccurracy > 48) {
                quality = GPSQuality.poor;
            } else if (gpsAccurracy > 10) {
                quality = GPSQuality.good;
            } else if (gpsAccurracy <= 10) {
                quality = GPSQuality.great;
            }

            gpsQualityListener.gpsQualityAndAccurracyUpdated(quality, gpsAccurracy, bearing, speed);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onLocationChanged(Location location) {
        updateResendIntervalSetting();
        reportGPSQualityBearingAndSpeed(location.getAccuracy(), location.getBearing(), location.getSpeed(),
            location.getLatitude(), location.getLongitude(), location.getAltitude());

        JSONObject json = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject fixJson = new JSONObject();

            fixJson.put("course", location.getBearing());
            fixJson.put("timestamp", location.getTime());
            fixJson.put("speed", location.getSpeed());
            fixJson.put("longitude", location.getLongitude());
            fixJson.put("latitude", location.getLatitude());

            jsonArray.put(fixJson);

            json.put("fixes", jsonArray);
            json.put("deviceUuid", prefs.getDeviceIdentifier());

            String postUrlStr = event.server + prefs.getServerGpsFixesPostPath();

            startService(MessageSendingService
                .createMessageIntent(this, postUrlStr, null, UUID.randomUUID(), json.toString(), null));

        } catch (JSONException ex) {
            ExLog.i(this, TAG, "Error while building geolocation json " + ex.getMessage());
        }

        //		DatabaseHelper.getInstance().insertGPSFix(this, location.getLatitude(),
        //				location.getLongitude(), location.getSpeed(),
        //				location.getBearing(), location.getProvider(),
        //				location.getTime(), eventRowId);
        //		ensureTransmittingServiceIsRunning();
    }

    /**
     * Update whether message sending service should retry every 3 seconds or
     * every 30.
     */
    private void updateResendIntervalSetting() {
        float batteryPct = getBatteryPercentage();
        boolean batteryIsCharging = prefs.getBatteryIsCharging();

        int updateInterval = UPDATE_INTERVAL_DEFAULT;

        if (prefs.getEnergySavingEnabledByUser() || (batteryPct < BATTERY_POWER_SAVE_TRESHOLD && !batteryIsCharging)) {
            if (BuildConfig.DEBUG) {
                ExLog.i(this, "POWER-LEVELS", "in power saving mode");
            }

            updateInterval = UPDATE_INTERVAL_POWERSAVE_MODE;
        } else {
            if (BuildConfig.DEBUG) {
                ExLog.i(this, "POWER-LEVELS", "in default power mode");
            }
        }

        prefs.setMessageResendInterval(updateInterval);
    }

    /**
     * Get battery charging level
     *
     * @return battery charging level in interval [0,1]
     */
    private float getBatteryPercentage() {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, ifilter);

        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;

        if (BuildConfig.DEBUG) {
            ExLog.i(this, TAG, "Battery: " + (batteryPct * 100) + "%");
        }

        return batteryPct;
    }

    /**
     * start transmitting service when a new fix arrives, because it ends
     * itself, if there's no data to send.
     */
    //	private void ensureTransmittingServiceIsRunning() {
    //		if (BuildConfig.DEBUG) {
    //			ExLog.i(this, TAG,
    //					"ensureTransmittingServiceIsRunning, starting TransmittingService");
    //		}
    //
    //		ServiceHelper.getInstance().startTransmittingService(this);
    //	}
    @Override
    public IBinder onBind(Intent intent) {
        return trackingBinder;
    }

    @Override
    public void onDestroy() {
        stopTracking();
        notificationManager.cancel(NOTIFICATION_ID);
        Toast.makeText(this, R.string.tracker_stopped, Toast.LENGTH_SHORT).show();
    }

    // private void showNotification() {
    // Intent intent = new Intent(this, RegattaActivity.class);
    // intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
    // | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    // PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
    // CharSequence text = getText(R.string.tracker_started);
    // Notification notification = new NotificationCompat.Builder(this)
    // .setContentTitle(getText(R.string.app_name))
    // .setContentText(text).setContentIntent(pi)
    // .setSmallIcon(R.drawable.icon).build();
    // notification.flags |= Notification.FLAG_NO_CLEAR;
    // startForeground(NOTIFICATION_ID, notification);
    // }

    public void registerGPSQualityListener(GPSQualityListener listener) {
        gpsQualityListener = listener;
    }

    public void unregisterGPSQualityListener() {
        gpsQualityListener = null;
    }

    public class TrackingBinder extends Binder {
        public TrackingService getService() {
            return TrackingService.this;
        }
    }

    public enum GPSQuality {
        noSignal(0), poor(2), good(3), great(4);

        private final int gpsQuality;

        GPSQuality(int quality) {
            this.gpsQuality = quality;
        }

        public int toInt() {
            return this.gpsQuality;
        }
    }

    public interface GPSQualityListener {
        public void gpsQualityAndAccurracyUpdated(GPSQuality quality, float gpsAccurracy, float gpsBearing,
            float gpsSpeed);
    }

}