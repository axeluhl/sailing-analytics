package com.sap.sailing.android.tracking.app.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;

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
		// IMPLEMENT ME
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
