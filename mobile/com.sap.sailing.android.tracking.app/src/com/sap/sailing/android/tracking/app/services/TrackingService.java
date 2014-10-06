package com.sap.sailing.android.tracking.app.services;

import java.util.UUID;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.services.sending.MessageSendingService;
import com.sap.sailing.android.tracking.app.nmea.NmeaGprmcBuilder;

public class TrackingService extends Service implements ConnectionCallbacks, OnConnectionFailedListener,
LocationListener {
    private LocationClient locationClient;
    private LocationRequest locationRequest;
    private boolean locationUpdateRequested = false;
    
    private static final String TAG = TrackingService.class.getName();
    
    public static final int FIX_INTERVAL_MS = 500;
    public static final int FASTEST_FIX_INTERVAL_MS = 100;
    
    public static final String URL = "http://192.168.1.79";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // http://developer.android.com/training/location/receive-location-updates.html
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(FIX_INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_FIX_INTERVAL_MS);
        
        locationClient = new LocationClient(this, this, this);
    }

    /**
     * Class for clients to access. Because we know this service always runs in the same process as its clients, we
     * don't need to deal with IPC.
     */
    public class TrackingServiceBinder extends Binder {
        public TrackingService getService() {
            return TrackingService.this;
        }
    }

    private final TrackingServiceBinder binder = new TrackingServiceBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public void startTracking() {
        locationClient.connect();
        locationUpdateRequested = true;
        
        ExLog.i(this, TAG, "Started Tracking");
    }

    public void stopTracking() {
        if (locationClient.isConnected()) {
            locationClient.removeLocationUpdates(this);
        }
        locationClient.disconnect();
        locationUpdateRequested = false;
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

    @Override
    public void onLocationChanged(Location location) {
        String nmea = NmeaGprmcBuilder.buildNmeaStringFrom(location);
        startService(MessageSendingService.createMessageIntent(
                this, URL, null, UUID.randomUUID(), nmea, null));
        ExLog.i(this, TAG, "Sent NMEA to server:" + nmea);
        //TODO also store to SD card
    }
}
