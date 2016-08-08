package com.sap.sailing.android.tracking.app.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
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
    private final static float BATTERY_POWER_SAVE_TRESHOLD = 0.2f;
    private boolean initialLocation;

    private String checkinDigest;
    private EventInfo event;
    
    /**
     * Must be synchronized upon while modifying the {@link #timerForDelayingSendingMessages} field
     * and while modifying the {@link #messagesQueuedBasedOnSendingInterval} list.
     */
    private final Object messageSendingTimerMonitor = new Object();
    
    /**
     * If {@code null}, no timer is currently active for sending out messages queued in
     * {@link #messagesQueuedBasedOnSendingInterval} after the send interval, and the next message sending intent
     * arriving can be forwarded to the sending service immediately, with the timer being started immediately afterwards
     * to delay the sending of messages arriving later until the resend interval has expired. If not {@code null},
     * messages that arrive in {@link #enqueueForSending(Intent)} will be appended to
     * {@link #messagesQueuedBasedOnSendingInterval}.
     * <p>
     * 
     * When the timer "rings" it acquires the {@link #messageSendingTimerMonitor} and checks for messages to send. If
     * messages are found, they are removed from the {@link #messagesQueuedBasedOnSendingInterval} list, the monitor is
     * released and the messages are then forwarded to the sending service. The timer remains running. Otherwise, the
     * timer stops itself, sets this field to {@code null} and releases the monitor.
     */
    private Timer timerForDelayingSendingMessages;
    
    /**
     * When a message is added and {@link #timerForDelayingSendingMessages} is {@code null}, a new timer
     * will be created and assigned to {@link #timerForDelayingSendingMessages}. Otherwise, we can assume that the
     * existing timer will pick up this new element upon its next turn.
     */
    private List<Intent> messagesQueuedBasedOnSendingInterval;

    @Override
    public void onCreate() {
        super.onCreate();
        messagesQueuedBasedOnSendingInterval = new LinkedList<>();
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

    public void startTracking() {
        googleApiClient.connect();
        locationUpdateRequested = true;

        ExLog.i(this, TAG, "Started Tracking");
        showNotification();

        prefs.setTrackerIsTracking(true);
        prefs.setTrackerIsTrackingCheckinDigest(checkinDigest);
    }

    public void stopTracking() {
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

    public void reportGPSQualityBearingAndSpeed(float gpsAccurracy, float bearing, float speed, double latitude,
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
            if (gpsAccurracy > 48) {
                quality = GPSQuality.poor;
            } else if (gpsAccurracy > 10) {
                quality = GPSQuality.good;
            } else if (gpsAccurracy <= 10) {
                quality = GPSQuality.great;
            }

            gpsQualityListener.gpsQualityAndAccurracyUpdated(quality, gpsAccurracy, bearingImpl, speedImpl);
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
        JSONObject json = new JSONObject();
        try {
            JSONArray jsonArray = new JSONArray();
            JSONObject fixJson = new JSONObject();
            fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.BEARING_DEG, location.getBearing());
            fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.TIME_MILLIS, location.getTime());
            fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.SPEED_M_PER_S, location.getSpeed());
            fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.LON_DEG, location.getLongitude());
            fixJson.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.LAT_DEG, location.getLatitude());
            jsonArray.put(fixJson);
            json.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.FIXES, jsonArray);
            json.put(FlatSmartphoneUuidAndGPSFixMovingJsonSerializer.DEVICE_UUID, prefs.getDeviceIdentifier());
            String postUrlStr = event.server + prefs.getServerGpsFixesPostPath();
            final Intent messageSendingIntent = MessageSendingService.createMessageIntent(
                    this, postUrlStr, null, UUID.randomUUID(), json.toString(), null);
            enqueueForSending(messageSendingIntent);
        } catch (JSONException ex) {
            ExLog.i(this, TAG, "Error while building geolocation json " + ex.getMessage());
        }
    }

    /**
     * Based on the {@link #prefs} and the {@link AppPreferences#getMessageSendingIntervalInMillis()} the message
     * sending intent is either immediately forwarded to the message sending service or it is enqueued for a timer
     * to pick it up in a bulk operation later, after the sending interval has expired.
     */
    private void enqueueForSending(final Intent messageSendingIntent) {
        synchronized (messageSendingTimerMonitor) {
            messagesQueuedBasedOnSendingInterval.add(messageSendingIntent);
            if (timerForDelayingSendingMessages == null) {
                timerForDelayingSendingMessages = new Timer("Message collecting timer", /* daemon */ true);
                final TimerTask timerTask = createTimerTask();
                timerForDelayingSendingMessages.schedule(timerTask, /* initial delay */ 0);
            }
        }
    }

    private TimerTask createTimerTask() {
        return new TimerTask() {
            @Override
            public void run() {
                final List<Intent> intentsToSend = new ArrayList<>();
                boolean reschedule = false;
                synchronized (messageSendingTimerMonitor) {
                    if (!messagesQueuedBasedOnSendingInterval.isEmpty()) {
                        reschedule = true;
                        intentsToSend.addAll(messagesQueuedBasedOnSendingInterval);
                        messagesQueuedBasedOnSendingInterval.clear();
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

        if (prefs.getEnergySavingEnabledByUser() || (batteryPct < BATTERY_POWER_SAVE_TRESHOLD && !batteryIsCharging)) {
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
        void gpsQualityAndAccurracyUpdated(GPSQuality quality, float gpsAccurracy, Bearing gpsBearing,
            Speed gpsSpeed);
    }

}