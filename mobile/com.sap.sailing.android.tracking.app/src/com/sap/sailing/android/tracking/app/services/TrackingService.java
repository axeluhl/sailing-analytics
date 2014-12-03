package com.sap.sailing.android.tracking.app.services;

import java.util.concurrent.ScheduledExecutorService;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
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
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.SensorGps;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;

public class TrackingService extends Service implements ConnectionCallbacks,
		OnConnectionFailedListener, LocationListener {

	private LocationClient locationClient;
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

	private String eventId;
	private long eventRowId;

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
			if (BuildConfig.DEBUG) {
				ExLog.i(this, TAG, "Starting Tracking Service with eventId: "
						+ eventId);
			}

			if (intent.getAction() != null) {
				if (intent.getAction().equals(
						getString(R.string.tracking_service_stop))) {
					stopTracking();
				} else {
					if (intent.getExtras() != null) {
						eventId = intent
								.getExtras()
								.getString(getString(R.string.tracking_service_event_id_parameter));
						
						eventRowId = getRowIdForEventId(eventId);
						
						if (BuildConfig.DEBUG) {
							ExLog.i(this, TAG, "Starting Tracking Service with eventId: "+ eventId);
							ExLog.i(this, TAG, "And with event._id: " + eventRowId);
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
	
	private long getRowIdForEventId(String eventId)
	{
		int result = 0;
		
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(Event.CONTENT_URI, null, "event_id = \"" + eventId + "\"", null, null);
		cursor.moveToFirst();
		result = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
		cursor.close();
		return result;
	}

	public void startTracking() {
		locationClient.connect();
		locationUpdateRequested = true;

		ExLog.i(this, TAG, "Started Tracking");
		// showNotification();

		prefs.setTrackerIsTracking(true);
		prefs.setTrackerIsTrackingEventId(eventId);
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

		prefs.setTrackerIsTracking(false);
		prefs.setTrackerIsTrackingEventId(null);

		stopSelf();
		ExLog.i(this, TAG, "Stopped Tracking");
	}

	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
		ExLog.e(this, TAG,
				"Failed to connect to Google Play Services for location updates");
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

	public void reportGPSQuality(float gpsAccurracy) {
		GPSQuality quality = GPSQuality.noSignal;

		if (gpsQualityListener != null) {
			if (gpsAccurracy > 163) {
				quality = GPSQuality.poor;
			} else if (gpsAccurracy > 48) {
				quality = GPSQuality.good;
			} else if (gpsAccurracy < 48) {
				quality = GPSQuality.great;
			}

			gpsQualityListener.gpsQualityAndAccurracyUpdated(quality,
					gpsAccurracy);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	@Override
	public void onLocationChanged(Location location) {

		reportGPSQuality(location.getAccuracy());

		ContentResolver cr = getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(SensorGps.GPS_LATITUDE, location.getLatitude());
		cv.put(SensorGps.GPS_LONGITUDE, location.getLongitude());
		cv.put(SensorGps.GPS_PROVIDER, location.getProvider());
		cv.put(SensorGps.GPS_SPEED, location.getSpeed());
		cv.put(SensorGps.GPS_TIME, location.getTime());
		cv.put(SensorGps.GPS_BEARING, location.getBearing());
		cv.put(SensorGps.GPS_EVENT_FK, eventRowId);

		cr.insert(SensorGps.CONTENT_URI, cv);

		ensureTransmittingServiceIsRunning();
	}

	/**
	 * start transmitting service when a new fix arrives, because it ends
	 * itself, if there's no data to send.
	 */
	private void ensureTransmittingServiceIsRunning() {
		if (BuildConfig.DEBUG) {
			ExLog.i(this, TAG,
					"ensureTransmittingServiceIsRunning, starting TransmittingService");
		}

		Intent intent = new Intent(this, TransmittingService.class);
		intent.setAction(getString(R.string.transmitting_service_start));
		this.startService(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return trackingBinder;
	}

	@Override
	public void onDestroy() {
		stopTracking();
		notificationManager.cancel(NOTIFICATION_ID);
		Toast.makeText(this, R.string.tracker_stopped, Toast.LENGTH_SHORT)
				.show();
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
		public void gpsQualityAndAccurracyUpdated(GPSQuality quality,
				float gpsAccurracy);
	}

}