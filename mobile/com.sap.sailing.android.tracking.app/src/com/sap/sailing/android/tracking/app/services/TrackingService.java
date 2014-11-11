package com.sap.sailing.android.tracking.app.services;

import java.util.concurrent.ScheduledExecutorService;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.provider.AnalyticsContract.SensorGps;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.RegattaActivity;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.VolleyHelper;

public class TrackingService extends Service implements ConnectionCallbacks, OnConnectionFailedListener,
        LocationListener {

    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private NotificationManager notificationManager;
    private boolean locationUpdateRequested = false;
    private AppPreferences prefs;
    private ScheduledExecutorService scheduler;

    private static final String TAG = TrackingService.class.getName();

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION_ID = R.string.tracker_started;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = new AppPreferences(this);

        // http://developer.android.com/training/location/receive-location-updates.html
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(prefs.getGPSFixInterval());
        locationRequest.setFastestInterval(prefs.getGPSFixFastestInterval());

        locationClient = new LocationClient(this, this, this);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction() != null) {
                if (intent.getAction().equals(getString(R.string.tracking_service_stop))) {
                    stopTracking();
                } else {
                    startTracking();
                }
            } else {
                stopTracking();
            }
        }
        return Service.START_STICKY;
    }

    public void startTracking() {
        locationClient.connect();
        locationUpdateRequested = true;

        ExLog.i(this, TAG, "Started Tracking");
        showNotification();
    }

    public void stopTracking() {
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        }
        locationClient.disconnect();
        locationUpdateRequested = false;
        if (scheduler != null) {
            scheduler.shutdown();
        }
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
            locationClient.requestLocationUpdates(locationRequest, this);
        }
    }

    @Override
    public void onDisconnected() {
        ExLog.i(this, TAG, "LocationClient was disconnected");
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onLocationChanged(Location location) {
        ContentResolver cr = getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(SensorGps.GPS_ACCURACY, location.getAccuracy());
        cv.put(SensorGps.GPS_ALTITUDE, location.getAltitude());
        cv.put(SensorGps.GPS_BEARING, location.getBearing());
        cv.put(SensorGps.GPS_DEVICE, prefs.getDeviceIdentifier());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            cv.put(SensorGps.GPS_ELAPSED_REALTIME, location.getElapsedRealtimeNanos());
        }
        cv.put(SensorGps.GPS_LATITUDE, location.getLatitude());
        cv.put(SensorGps.GPS_LONGITUDE, location.getLongitude());
        cv.put(SensorGps.GPS_PROVIDER, location.getProvider());
        cv.put(SensorGps.GPS_SPEED, location.getSpeed());
        cv.put(SensorGps.GPS_TIME, location.getTime());

        Uri result = cr.insert(SensorGps.CONTENT_URI, cv);

        JSONObject json = new JSONObject();
        try {
            json.put("bearingDeg", cv.get(SensorGps.GPS_BEARING));
            json.put("timeMillis", cv.get(SensorGps.GPS_TIME));
            json.put("speedMperS", cv.get(SensorGps.GPS_SPEED));
            json.put("lonDeg", cv.get(SensorGps.GPS_LONGITUDE));
            json.put("deviceUuid", prefs.getDeviceIdentifier());
            json.put("latDeg", cv.get(SensorGps.GPS_LATITUDE));
        } catch (JSONException ex) {
            ExLog.i(this, TAG, "Error while building geolocation json " + ex.getMessage());
        }
        VolleyHelper.getInstance(this).addRequest(
                new JsonObjectRequest(prefs.getServerURL() + "/tracking/recordFixesFlatJson", json, new FixListener(
                        SensorGps.getGpsId(result)), new FixErrorListener()));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        stopTracking();
        notificationManager.cancel(NOTIFICATION_ID);
        Toast.makeText(this, R.string.tracker_stopped, Toast.LENGTH_SHORT).show();
    }

    private void showNotification() {
        Intent intent = new Intent(this, RegattaActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
        CharSequence text = getText(R.string.tracker_started);
        Notification notification = new NotificationCompat.Builder(this).setContentTitle(getText(R.string.app_name))
                .setContentText(text).setContentIntent(pi).setSmallIcon(R.drawable.icon).build();
        notification.flags |= Notification.FLAG_NO_CLEAR;
        startForeground(NOTIFICATION_ID, notification);
    }

    private class FixListener implements Listener<JSONObject> {

        private String id;

        public FixListener(String id) {
            this.id = id;
        }

        @Override
        public void onResponse(JSONObject response) {
            getContentResolver().delete(SensorGps.CONTENT_URI, SensorGps._ID + " = ?", new String[] { id });
        }
    }

    private class FixErrorListener implements ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
            ExLog.e(TrackingService.this, TAG, "Error while sending GPS fix " + error.getMessage());
        }
    }
}