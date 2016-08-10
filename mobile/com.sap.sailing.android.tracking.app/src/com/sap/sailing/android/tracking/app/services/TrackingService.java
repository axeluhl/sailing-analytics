package com.sap.sailing.android.tracking.app.services;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.ui.activities.TrackingActivity;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.valueobjects.EventInfo;
import com.sap.sailing.domain.common.Bearing;
import com.sap.sailing.domain.common.Speed;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.domain.common.impl.MeterPerSecondSpeedImpl;
import com.sap.sailing.domain.common.tracking.impl.FlatSmartphoneUuidAndGPSFixMovingJsonSerializer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.hardware.GeomagneticField;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class TrackingService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private NotificationManager notificationManager;
    private boolean locationUpdateRequested = false;
    private AppPreferences prefs;

    private GPSQualityListener gpsQualityListener;
    private final IBinder trackingBinder = new TrackingBinder();

    private static final String TAG = TrackingService.class.getName();

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION_ID = R.string.tracker_started;

    public final static int UPDATE_INTERVAL_IN_MILLIS_DEFAULT = 1000;
    private final static int UPDATE_INTERVAL_IN_MILLIS_POWERSAVE_MODE = 30000;
    private final static float BATTERY_POWER_SAVE_THRESHOLD = 0.2f;
    private boolean initialLocation;

    private String checkinDigest;
    private EventInfo event;

    /**
     * Must be synchronized upon while modifying the {@link #timerForDelayingSendingMessages} field
     * and while modifying the {@link #locationsQueuedBasedOnSendingInterval} list.
     */
    private final Object messageSendingTimerMonitor = new Object();

    /**
     * If {@code null}, no timer is currently active for sending out messages queued in
     * {@link #locationsQueuedBasedOnSendingInterval} after the send interval, and the next message sending intent
     * arriving can be forwarded to the sending service immediately, with the timer being started immediately afterwards
     * to delay the sending of messages arriving later until the resend interval has expired. If not {@code null},
     * messages that arrive in {@link #enqueueForSending(String, JSONObject)} will be appended to
     * {@link #locationsQueuedBasedOnSendingInterval}.
     * <p>
     *
     * When the timer "rings" it acquires the {@link #messageSendingTimerMonitor} and checks for messages to send. If
     * messages are found, they are removed from the {@link #locationsQueuedBasedOnSendingInterval} list, the monitor is
     * released and the messages are then forwarded to the sending service. The timer remains running. Otherwise, the
     * timer stops itself, sets this field to {@code null} and releases the monitor.
     */
    private Timer timerForDelayingSendingMessages;

    /**
     * When a {@link Location} is added and {@link #timerForDelayingSendingMessages} is {@code null}, a new timer
     * will be created and assigned to {@link #timerForDelayingSendingMessages}. Otherwise, we can assume that the
     * existing timer will pick up this new element upon its next turn.
     */
    private LinkedHashMap<String, List<Location>> locationsQueuedBasedOnSendingInterval;

    @Override
    public void onCreate() {
        super.onCreate();
        locationsQueuedBasedOnSendingInterval = new LinkedHashMap<>();
        prefs = new AppPreferences(this);

        initialLocation = true;
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
                        checkinDigest = intent.getExtras().getString(
                                getString(R.string.tracking_service_checkin_digest_parameter));

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

    private void startTracking() {
        googleApiClient.connect();
        locationUpdateRequested = true;

        ExLog.i(this, TAG, "Started Tracking");
        showNotification();

        prefs.setTrackerIsTracking(true);
        prefs.setTrackerIsTrackingCheckinDigest(checkinDigest);
    }

    private void stopTracking() {
        if (googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        }
        googleApiClient.disconnect();
        locationUpdateRequested = false;

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
        // no-op
    }

    private void reportGPSQualityBearingAndSpeed(float gpsAccuracy, float bearing, float speed, double latitude,
            double longitude, double altitude) {
        Bearing bearingImpl = null;
        Speed speedImpl = null;

        if (bearing != 0.0) {
            bearingImpl = new DegreeBearingImpl(bearing);
        }

        if (speed > 0.0) {
            speedImpl = new MeterPerSecondSpeedImpl(speed);
        }

        if (prefs.getDisplayHeadingWithSubtractedDeclination() && bearingImpl != null) {
            GeomagneticField geomagneticField = new GeomagneticField((float) latitude, (float) longitude,
                    (float) altitude, System.currentTimeMillis());
            bearingImpl.add(new DegreeBearingImpl(- geomagneticField.getDeclination()));
        }

        GPSQuality quality = GPSQuality.noSignal;
        if (gpsQualityListener != null) {
            if (gpsAccuracy > 48) {
                quality = GPSQuality.poor;
            } else if (gpsAccuracy > 10) {
                quality = GPSQuality.good;
            } else if (gpsAccuracy <= 10) {
                quality = GPSQuality.great;
            }

            gpsQualityListener.gpsQualityAndAccuracyUpdated(quality, gpsAccuracy, bearingImpl, speedImpl);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onLocationChanged(Location location) {
        if (initialLocation) {
            storeInitialTrackingTimestamp();
        }
        updateResendIntervalSetting();
        reportGPSQualityBearingAndSpeed(location.getAccuracy(), location.getBearing(), location.getSpeed(),
                location.getLatitude(), location.getLongitude(), location.getAltitude());
        final String postUrlStr = event.server + prefs.getServerGpsFixesPostPath();
        enqueueForSending(postUrlStr, location);
    }

    private JSONObject createJsonLocationFix(Location location) throws JSONException {
        JSONObject fixJson = new JSONObject();
        fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.BEARING_DEG, location.getBearing());
        fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.TIME_MILLIS, location.getTime());
        fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.SPEED_M_PER_S, location.getSpeed());
        fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.LON_DEG, location.getLongitude());
        fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.LAT_DEG, location.getLatitude());
        return fixJson;
    }
    
    private JSONArray createJsonLocationFixes(Iterable<Location> locations) throws JSONException {
        final JSONArray fixesAsJson = new JSONArray();
        for (final Location location : locations) {
            fixesAsJson.put(createJsonLocationFix(location));
        }
        return fixesAsJson;
    }
    
    private JSONObject createFixesMessage(Iterable<Location> locations) throws JSONException {
        final JSONObject fixesMessage = new JSONObject();
        final JSONArray fixesJson = createJsonLocationFixes(locations);
        fixesMessage.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.FIXES, fixesJson);
        fixesMessage.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.DEVICE_UUID, prefs.getDeviceIdentifier());
        return fixesMessage;
    }

    /**
     * Based on the {@link #prefs} and the {@link AppPreferences#getMessageSendingIntervalInMillis()} the message
     * sending intent is either immediately forwarded to the message sending service or it is enqueued for a timer
     * to pick it up in a bulk operation later, after the sending interval has expired.
     *
     * @param postUrl URL to send fixes
     * @param location the location fix to enqueue for sending to the URL specified by {@code postUrl}
     */
    private void enqueueForSending(String postUrl, Location location) {
        synchronized (messageSendingTimerMonitor) {
            newOrAppendPayload(postUrl, location);
            if (timerForDelayingSendingMessages == null) {
                timerForDelayingSendingMessages = new Timer("Message collecting timer", /* daemon */ true);
                final TimerTask timerTask = createTimerTask();
                timerForDelayingSendingMessages.schedule(timerTask, /* initial delay 0, send new fix immediately */ 0);
            }
        }
    }

    /**
     * Add the {@code payload} to the HashMap, if the postUrl isn't included. If the url is included the fixes will be
     * concat to the current waiting data.
     *
     * @param postUrl URL to send fixes
     * @param location the fix to store in {@link #locationsQueuedBasedOnSendingInterval}
     */
    private void newOrAppendPayload(String postUrl, Location location) {
        synchronized (messageSendingTimerMonitor) {
            List<Location> locationsForUrl = locationsQueuedBasedOnSendingInterval.get(postUrl);
            if (locationsForUrl == null) {
                locationsForUrl = new ArrayList<>();
                locationsQueuedBasedOnSendingInterval.put(postUrl, locationsForUrl);
            } else {
                locationsForUrl.add(location);
            }
        }
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                try {
                    final List<Intent> intentsToSend = new ArrayList<>();
                    boolean reschedule = false;
                    synchronized (messageSendingTimerMonitor) {
                        if (!locationsQueuedBasedOnSendingInterval.isEmpty()) {
                            reschedule = true;
                            for (Map.Entry<String, List<Location>> pair : locationsQueuedBasedOnSendingInterval.entrySet()) {
                                    intentsToSend.add(MessageSendingService.createMessageIntent(TrackingService.this, pair.getKey(), null, UUID.randomUUID(),
                                            createFixesMessage(pair.getValue()).toString(), null));
                            }
                            locationsQueuedBasedOnSendingInterval.clear();
                        }
                        if (!reschedule) {
                            timerForDelayingSendingMessages.cancel();
                            timerForDelayingSendingMessages = null;
                        } else {
                            timerForDelayingSendingMessages.schedule(createTimerTask(), prefs.getMessageSendingIntervalInMillis());
                        }
                    }
                    for (final Intent intentToSend : intentsToSend) {
                        startService(intentToSend);
                    }
                } catch (JSONException e) {
                    ExLog.e(TrackingService.this, TAG, "Internal error converting location fixes to JSON message: "+e.getMessage());
                }
            }
        };
    }

    private void storeInitialTrackingTimestamp() {
        if (prefs.getTrackingTimerStarted() == 0) {
            prefs.setTrackingTimerStarted(System.currentTimeMillis());
        }
        initialLocation = false;
    }

    /**
     * Update whether message sending service should retry every 3 seconds or every 30.
     */
    private void updateResendIntervalSetting() {
        float batteryPct = getBatteryPercentage();
        boolean batteryIsCharging = prefs.getBatteryIsCharging();

        int updateInterval = UPDATE_INTERVAL_IN_MILLIS_DEFAULT;

        if (prefs.getEnergySavingEnabledByUser() || (batteryPct < BATTERY_POWER_SAVE_THRESHOLD && !batteryIsCharging)) {
            if (BuildConfig.DEBUG) {
                ExLog.i(this, "POWER-LEVELS", "in power saving mode");
            }

            updateInterval = UPDATE_INTERVAL_IN_MILLIS_POWERSAVE_MODE;
        } else {
            if (BuildConfig.DEBUG) {
                ExLog.i(this, "POWER-LEVELS", "in default power mode");
            }
        }

        prefs.setMessageResendIntervalInMillis(updateInterval);
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

    // Useful code for Bug 3048. Will stay commented for now.

     private void showNotification() {
     Intent intent = new Intent(this, TrackingActivity.class);
     intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
     | Intent.FLAG_ACTIVITY_SINGLE_TOP);
     PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
     Notification notification = new NotificationCompat.Builder(this)
     .setContentTitle(getText(R.string.app_name))
     .setContentText(getString(R.string.tracking_notification_text, event.name)).setContentIntent(pi)
         .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
         .setSmallIcon(R.drawable.ic_directions_boat)
         .setOngoing(true).build();
         notificationManager.notify(NOTIFICATION_ID, notification);
     }

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
        void gpsQualityAndAccuracyUpdated(GPSQuality quality, float gpsAccurracy, Bearing gpsBearing,
            Speed gpsSpeed);
    }

}