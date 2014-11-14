package com.sap.sailing.android.tracking.app.services;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.IBinder;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.SensorGps;
import com.sap.sailing.android.tracking.app.services.sending.ConnectivityChangedReceiver;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.VolleyHelper;

/**
 * Service that handles sending of GPS-fixes (fixes) to the Sailing-API.
 * 
 * It checks, after a certain time-interval has passed, if any fixes are available in the database 
 * and attempts to send them batch-wise.
 * 
 * It keeps track of when the last successful sending occurred and, if no new data is available 
 * (i.e., if tracking is disabled), ends itself after a certain period. It relies on being restarted 
 * by the {@link TrackingService}. 
 * 
 * It also ends itself when connectivity is lost. In that case it relies on being restarted by 
 * the {@link ConnectivityChangedReceiver}.   
 * 
 * It switches into a power saving mode, if a certain battery discharge is reached. It does, however,
 * not do so, if the device is plugged in (= when it is charging). The power saving mode causes the
 * time-interval between check- and transmit-cycles to be increased to conserve power.
 * 
 * @author lukas
 *
 */
public class TransmittingService extends Service {
	
	private static final String TAG = TransmittingService.class.getName();
	
	private final int UPDATE_BATCH_SIZE = 10;
	private final int UPDATE_INTERVAL_DEFAULT = 3000;
	private final int UPDATE_INTERVAL_POWERSAVE_MODE = 30000;
	private final long AUTO_END_SERVICE_AFTER_NANO_PASSED = 10000000000L; // 10 sec
	private final float BATTERY_POWER_SAVE_TRESHOLD = 0.2f;
	
	private int currentUpdateInterval = UPDATE_INTERVAL_DEFAULT;
	
	private boolean sendingAttempted = false;
	private boolean lastTransmissionFailed = false;
	private long lastTransmissionTimestamp = 0;
	
	private AppPreferences prefs;
	private Timer timer;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		prefs = new AppPreferences(this);
		
		if (intent != null) {
			if (intent.getAction() != null) {
				if (intent.getAction().equals(getString(R.string.transmitting_service_start))) {
					startTimer();
				} else {
					stopTimer();
				}
			}
			else
			{
				stopTimer();
			}
		}
		
		return Service.START_STICKY;
	}
	
	private void startTimer() {
		timer = new Timer();
		timer.start();
		if (BuildConfig.DEBUG) {
			ExLog.i(this, "TIMER", "Background update-timer start");
		}
		
	}

	private void stopTimer() {
		if (timer != null)
		{
			timer.stop();	
			if (BuildConfig.DEBUG) {
				ExLog.i(this, "TIMER", "Background update-timer stop");
			}
			
		}
	}
	
	private void markSuccessfulTransmission()
	{
		if (BuildConfig.DEBUG) {
			ExLog.i(this, "TRANSMISSION", "markSuccessfulTransmission");
		}
		
		lastTransmissionFailed = false;
		lastTransmissionTimestamp = System.nanoTime();
	}
	
	private void markFailedTransmission()
	{
		if (BuildConfig.DEBUG) {
			ExLog.i(this, "TRANSMISSION", "markFailedTransmission");
		}
		
		lastTransmissionFailed = true;
		lastTransmissionTimestamp = 0;
	}
	
	/**
	 * If time interval has passed without any transmission, the service can turn
	 * itself off. Same for 
	 * @return
	 */
	private boolean serviceCanShutItselfDown()
	{
		long currentNanoTime = System.nanoTime();
		
		if (BuildConfig.DEBUG)
		{
			ExLog.i(this, "TRANSMISSION", "serviceCanShutItselfDown?");
			ExLog.i(this, "TRANSMISSION", "DELTA:" + (currentNanoTime - lastTransmissionTimestamp));
		}
		
		// if network is not connected, shutdown. ConnevtivityChangedReceiver should restart us.
		if (!isConnected())
		{
			enabledConnectivityReceiver(); // make sure ConnectivityReceiver is running
			return true;
		}
		
		// if we had a failure, never go to sleep until data is sent successfully.
		if (lastTransmissionFailed)
		{
			if (BuildConfig.DEBUG)
			{
				ExLog.i(this, "TRANSMISSION", "returning false, have failed transmission");	
			}
			
			return false;
		}
		
		if (!sendingAttempted)
		{
			if (BuildConfig.DEBUG)
			{
				ExLog.i(this, "TRANSMISSION", "returning true, no sending attempt");	
			}
			
			return true;
		}
		
		if (currentNanoTime - lastTransmissionTimestamp > AUTO_END_SERVICE_AFTER_NANO_PASSED)
		{
			if (BuildConfig.DEBUG)
			{
				ExLog.i(this, "TRANSMISSION", "returning true, timeout");	
			}
			
			return true;
		}
		
		if (BuildConfig.DEBUG)
		{
			ExLog.i(this, "TRANSMISSION", "returning false, don't terminate");	
		}
		
		return false;
	}
	
	private void timerFired()
	{
		float batteryPct = getBatteryPercentage();
		boolean batteryIsCharging = prefs.getBatteryIsCharging(); 
				
		if (batteryPct < BATTERY_POWER_SAVE_TRESHOLD && !batteryIsCharging)
		{
			if (BuildConfig.DEBUG)
			{
				ExLog.i(this, "POWER-LEVELS", "in power saving mode");	
			}
			
			currentUpdateInterval = UPDATE_INTERVAL_POWERSAVE_MODE;
		}
		else
		{
			if (BuildConfig.DEBUG)
			{
				ExLog.i(this, "POWER-LEVELS", "in default power mode");	
			}
			
			currentUpdateInterval = UPDATE_INTERVAL_DEFAULT;
		}
		
		sendFixesToAPI();
	}
	
	
	private void sendFixesToAPI() {
		// first, lets fetch all unsent fixes
		List<GpsFix> fixes = getUnsentFixes();
		
		// store ids
		ArrayList<String> ids = new ArrayList<String>();
		
		// create JSON
        JSONArray jsonArray = new JSONArray();
		
		for (GpsFix fix : fixes)
		{
			ids.add(String.valueOf(fix.id));
			
			JSONObject json = new JSONObject();
	        try {
	            json.put("bearingDeg", fix.course);
	            json.put("timeMillis", fix.timestamp);
	            json.put("speedMperS", fix.speed);
	            json.put("lonDeg", fix.longitude);
	            //json.put("deviceUuid", prefs.getDeviceIdentifier());
	            json.put("latDeg", fix.latitude);
	        } catch (JSONException ex) {
	            ExLog.i(this, TAG, "Error while building geolocation json " + ex.getMessage());
	        }
	        
	        jsonArray.put(json);
		}
		
		if (jsonArray.length() > 0) {
			// send

			String[] idsArr = new String[ids.size()];

			JSONObject requestObject = new JSONObject();
			
			try {
				requestObject.put("fixes", jsonArray);
				requestObject.put("deviceUuid", prefs.getDeviceIdentifier());
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			if (BuildConfig.DEBUG) {
				ExLog.i(this, TAG, "sending gps fix json: " + requestObject.toString());
				ExLog.i(this, TAG, "url: " + prefs.getServerURL() + prefs.getServerGpsFixesPostPath());
			}
			
			
			sendingAttempted = true;
			
			VolleyHelper.getInstance(this).addRequest(
					new JsonObjectRequest(prefs.getServerURL()
							+ prefs.getServerGpsFixesPostPath(), requestObject,
							new FixSubmitListener(ids.toArray(idsArr)),
							new FixSubmitErrorListener()));
		} else {
			
			if (serviceCanShutItselfDown())
			{
				if (BuildConfig.DEBUG) {
					ExLog.i(this, TAG, "Nothing to send or timeout occurred, Transmitting Service is stopping timer and terminating itself.");	
				}
				
				stopSelf();
				stopTimer();
			}
		}
	}
	
	private float getBatteryPercentage()
	{
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = this.registerReceiver(null, ifilter);
		
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

		float batteryPct = level / (float)scale;
		
		if (BuildConfig.DEBUG) {
			ExLog.i(this, TAG, "Battery: " + (batteryPct * 100) + "%");
		}
		
		
		return batteryPct;
	}
	
	private void deleteSynced(String[] fixIdStrings)
	{
		for (String idStr: fixIdStrings)
		{
			ContentValues updateValues = new ContentValues();
			updateValues.put(SensorGps.GPS_SYNCED, 1);
			Uri uri = ContentUris.withAppendedId(SensorGps.CONTENT_URI, Long.parseLong(idStr));
			getContentResolver().delete(uri, null, null);
		}
	}
	
	/**
     * checks if there is network connectivity
     * 
     * @return connectivity check value
     */
    private boolean isConnected() {
    	ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        }
        return activeNetwork.isConnected();
    }
    
    private void enabledConnectivityReceiver()
    {
    	ConnectivityChangedReceiver.enable(this);
    }
	
//  might need this class in the future:  
//
//	private void markAsSynced(String[] fixIdStrings)
//	{
//		for (String idStr: fixIdStrings)
//		{
//			ContentValues updateValues = new ContentValues();
//			updateValues.put(SensorGps.GPS_SYNCED, 1);
//			Uri uri = ContentUris.withAppendedId(SensorGps.CONTENT_URI, Long.parseLong(idStr));
//			getContentResolver().update(uri, updateValues, null, null);
//		}
//	}
	
	private List<GpsFix> getUnsentFixes()
	{
		String selectionClause = SensorGps.GPS_SYNCED + " = 0";
		String sortAndLimitClause = SensorGps.GPS_TIME + " DESC LIMIT " + UPDATE_BATCH_SIZE;
		
		ArrayList<GpsFix> list = new ArrayList<GpsFix>();
		
		Cursor cur = getContentResolver().query(SensorGps.CONTENT_URI, null, selectionClause, null, sortAndLimitClause);
		while (cur.moveToNext()) {
			GpsFix gpsFix = new GpsFix();
			
			gpsFix.id = cur.getInt(cur.getColumnIndex(SensorGps._ID));
			gpsFix.timestamp = cur.getLong(cur.getColumnIndex(SensorGps.GPS_TIME));
			gpsFix.latitude  = cur.getDouble(cur.getColumnIndex(SensorGps.GPS_LATITUDE));
			gpsFix.longitude  = cur.getDouble(cur.getColumnIndex(SensorGps.GPS_LONGITUDE));
			gpsFix.speed  = cur.getDouble(cur.getColumnIndex(SensorGps.GPS_SPEED));
			gpsFix.course  = cur.getDouble(cur.getColumnIndex(SensorGps.GPS_BEARING));
			gpsFix.synced = cur.getInt(cur.getColumnIndex(SensorGps.GPS_SYNCED));
			
			list.add(gpsFix);
			
			if (list.size() >= UPDATE_BATCH_SIZE)
			{
				break;
			}
        }
		
		cur.close();
		return list;
	}
	
	private class FixSubmitListener implements Listener<JSONObject> {

        private String[] ids;

        public FixSubmitListener(String[] ids) {
            this.ids = ids;
        }

        @Override
        public void onResponse(JSONObject response) {
        	System.out.println("FixListener#onResponse: " + response);
        	System.out.println("ids: " + ids);

        	deleteSynced(ids);
        	
        	markSuccessfulTransmission();
        	//markAsSynced(ids);
        }
    }
	
    private class FixSubmitErrorListener implements ErrorListener {

        @Override
        public void onErrorResponse(VolleyError error) {
        	markFailedTransmission();
            ExLog.e(TransmittingService.this, TAG, "Error while sending GPS fix " + error.getMessage());
        }
    }

	class GpsFix
	{
		public int id;
		public long timestamp;
		public double latitude;
		public double longitude;
		public double speed;
		public double course;
		public int synced;

		@Override
		public String toString() {
			return "ID: " + id + ", T: " + timestamp + ", LAT: " + latitude
					+ ", LON: " + longitude + ", SPD: " + speed + ", CRS: "
					+ course + ", SYN: " + synced;
		}
	}
	
	class Timer implements Runnable {
		public Thread t;
		public volatile boolean endExecution;
		
		@Override
		public void run() {
			while (!endExecution) {
				try {
					Thread.sleep(currentUpdateInterval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				timerFired();
			}
		}

		public void start() {
			endExecution = false;
			if (t == null) {
				t = new Thread(this);
				t.start();
			}
		}
		
		public void stop() {
			endExecution = true;
		}
	}
}
