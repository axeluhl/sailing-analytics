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
import android.database.Cursor;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.IBinder;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.SensorGps;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.VolleyHelper;

public class TransmittingService extends Service {
	
	private static final String TAG = TransmittingService.class.getName();
	
	private int UPDATE_BATCH_SIZE = 10;
	private int UPDATE_INTERVAL_DEFAULT = 3000;
	private int UPDATE_INTERVAL_POWERSAVE_MODE = 30000;
	private long AUTO_END_SERVICE_AFTER_NANO_PASSED = 10000000000L; // 10 sec
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
		ExLog.iDebug(this, "TIMER", "Background update-timer start");
	}

	private void stopTimer() {
		if (timer != null)
		{
			timer.stop();	
			ExLog.iDebug(this, "TIMER", "Background update-timer stop");
		}
	}
	
	private void markSuccessfulTransmission()
	{
		ExLog.iDebug(this, "TRANSMISSION", "markSuccessfulTransmission");
		lastTransmissionFailed = false;
		lastTransmissionTimestamp = System.nanoTime();
	}
	
	private void markFailedTransmission()
	{
		ExLog.iDebug(this, "TRANSMISSION", "markFailedTransmission");
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
		
		ExLog.iDebug(this, "TRANSMISSION", "serviceCanShutItselfDown?");
		ExLog.iDebug(this, "TRANSMISSION", "DELTA:" + (currentNanoTime - lastTransmissionTimestamp));
		
		// if we had a failure, never go to sleep until data is sent successfully.
		if (lastTransmissionFailed)
		{
			ExLog.iDebug(this, "TRANSMISSION", "returning false, have failed transmission");
			return false;
		}
		
		if (!sendingAttempted)
		{
			ExLog.iDebug(this, "TRANSMISSION", "returning true, no sending attempt");
			return true;
		}
		
		if (currentNanoTime - lastTransmissionTimestamp > AUTO_END_SERVICE_AFTER_NANO_PASSED)
		{
			ExLog.iDebug(this, "TRANSMISSION", "returning true, timeout");
			return true;
		}
		
		ExLog.iDebug(this, "TRANSMISSION", "returning false, don't terminate");
		return false;
	}
	
	private void timerFired()
	{
		float batteryPct = getBatteryPercentage();
		boolean batteryIsCharging = prefs.getBatteryIsCharging(); 
				
		if (batteryPct < BATTERY_POWER_SAVE_TRESHOLD && !batteryIsCharging)
		{
			ExLog.iDebug(this, "POWER-LEVELS", "in power saving mode");
			currentUpdateInterval = UPDATE_INTERVAL_POWERSAVE_MODE;
		}
		else
		{
			ExLog.iDebug(this, "POWER-LEVELS", "in default power mode");
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
			
			ExLog.iDebug(this, TAG, "sending gps fix json: " + requestObject.toString());
			ExLog.iDebug(this, TAG, "url: " + prefs.getServerURL() + prefs.getServerGpsFixesPostPath());
			
			sendingAttempted = true;
			
			VolleyHelper.getInstance(this).addRequest(
					new JsonObjectRequest(prefs.getServerURL()
							+ prefs.getServerGpsFixesPostPath(), requestObject,
							new FixSubmitListener(ids.toArray(idsArr)),
							new FixSubmitErrorListener()));
		} else {
			
			if (serviceCanShutItselfDown())
			{
				ExLog.iDebug(this, TAG, "Nothing to send or timeout occurred, Transmitting Service is stopping timer and terminating itself.");
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
		
		ExLog.iDebug(this, TAG, "Battery: " + (batteryPct * 100) + "%");
		
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
