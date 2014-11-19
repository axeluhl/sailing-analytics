package com.sap.sailing.android.tracking.app.services;

import java.util.concurrent.ScheduledExecutorService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.SensorGps;
import com.sap.sailing.android.tracking.app.ui.activities.RegattaActivity;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.server.gateway.deserialization.impl.FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer;

public class TrackingService extends Service implements ConnectionCallbacks, OnConnectionFailedListener,
        LocationListener {

    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private NotificationManager notificationManager;
    private boolean locationUpdateRequested = false;
    private AppPreferences prefs;
    private ScheduledExecutorService scheduler;

    private static final String TAG = TrackingService.class.getName();

    public static final String WEB_SERVICE_PATH = "/sailingserver/api/v1/gps_fixes";
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
        JSONObject json = new JSONObject();
        try {
            json.put(FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer.DEVICE_UUID,
                    prefs.getDeviceIdentifier());
            JSONArray fixes = new JSONArray();
            JSONObject fix = new JSONObject();
            fix.put(FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer.LAT_DEG, location.getLatitude());
            fix.put(FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer.LON_DEG, location.getLongitude());
            fix.put(FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer.TIME_MILLIS, location.getTime());
            fix.put(FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer.SPEED_M_PER_S, location.getSpeed());
            fix.put(FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer.BEARING_DEG, location.getBearing());
            fixes.put(0, fix);
            json.put(FlatSmartphoneUuidAndGPSFixMovingJsonDeserializer.FIXES, fixes);
        } catch (JSONException e) {
            ExLog.e(this, TAG, "Error serializing fix: " + e.getMessage());
        }
        ContentResolver cr = getContentResolver();
        ContentValues cv = new ContentValues();
        cv.put(SensorGps.GPS_LATITUDE, location.getLatitude());
        cv.put(SensorGps.GPS_LONGITUDE, location.getLongitude());
        cv.put(SensorGps.GPS_PROVIDER, location.getProvider());
        cv.put(SensorGps.GPS_SPEED, location.getSpeed());
        cv.put(SensorGps.GPS_TIME, location.getTime());

        cr.insert(SensorGps.CONTENT_URI, cv);
        
        checkIfTransmittingServiceIsRunningAndStartIfNot();
    }
    
    /**
     * start transmitting service when a new fix arrives, because it ends itself,
     * if there's no data to send.
     */
    private void checkIfTransmittingServiceIsRunningAndStartIfNot()
    {
    	if (!isMyServiceRunning(TransmittingService.class))
    	{
    		Intent intent = new Intent(this, TransmittingService.class);
            intent.setAction(getString(R.string.transmitting_service_start));
            this.startService(intent);
            
            if (BuildConfig.DEBUG)
            {
            	ExLog.i(this, TAG, "starting transmitting service, because it's not running while a location fix was received.");
            }
    	}
    }
    
    /**
     * check if a service is running, see: http://stackoverflow.com/a/5921190/3589281
     * @param serviceClass
     * @return
     */
	private boolean isMyServiceRunning(Class<?> serviceClass) {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	        if (serviceClass.getName().equals(service.service.getClassName())) {
	            return true;
	        }
	    }
	    return false;
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
}