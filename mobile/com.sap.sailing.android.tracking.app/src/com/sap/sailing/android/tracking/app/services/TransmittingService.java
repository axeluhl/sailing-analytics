package com.sap.sailing.android.tracking.app.services;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.SensorGps;

public class TransmittingService extends Service {
	
	private int UPDATE_INTERVAL_DEFAULT = 3000;
	private int UPDATE_INTERVAL_POWERSAVE_MODE = 30000;
	private int currentUpdateInterval = UPDATE_INTERVAL_DEFAULT;
	
	private Timer timer;
	private Handler handler;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		handler = new Handler();
		
		if (intent != null) {
			if (intent.getAction() != null) {
				if (intent.getAction().equals(getString(R.string.transmitting_service_start))) {
					startTimer();
				} else if (intent.getAction().equals(getString(R.string.transmitting_service_switch_to_power_saving_mode))) {
					this.currentUpdateInterval = UPDATE_INTERVAL_POWERSAVE_MODE;
				} else if (intent.getAction().equals(getString(R.string.transmitting_service_switch_to_default_power_mode))) {
					this.currentUpdateInterval = UPDATE_INTERVAL_DEFAULT;
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
		ExLog.i(this, "TIMER:", "Background update-timer start");
	}

	private void stopTimer() {
		if (timer != null)
		{
			timer.stop();	
			ExLog.i(this, "TIMER:", "Background update-timer stop");
		}
	}
	
	private void sendFixesToAPI() {
		// first, lets fetch all unsent fixes
		System.out.println("sendFixesToAPI");
		List<GpsFix> fixes = getAllUnsentFixes();
		
		// SEND AWAY
		
		for (GpsFix fix : fixes)
		{
			//ExLog.i(this, "GPSFIX", "todo: send to api");
		}
		
		//markAsSynced(fixes);
	}
	
	private void markAsSynced(List<GpsFix> fixes)
	{
		// TODO: 
		
		for (GpsFix fix : fixes)
		{
			ContentValues updateValues = new ContentValues();
			updateValues.put(SensorGps.GPS_SYNCED, 1);
			Uri uri = ContentUris.withAppendedId(SensorGps.CONTENT_URI, fix.id);
			getContentResolver().update(uri, updateValues, null, null);
		}
		
	}
	
	private List<GpsFix> getAllUnsentFixes()
	{
		String selectionClause = SensorGps.GPS_SYNCED + " = 0";
		ArrayList<GpsFix> list = new ArrayList<GpsFix>();
		
		Cursor cur = getContentResolver().query(SensorGps.CONTENT_URI, null, selectionClause, null, null);
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
        }
		
		cur.close();
		return list;
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
				
				handler.post(new Runnable() {
					@Override
					public void run() {
						sendFixesToAPI();
					}
				});
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
