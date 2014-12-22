package com.sap.sailing.android.tracking.app.services;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.sending.ConnectivityChangedReceiver;
import com.sap.sailing.android.tracking.app.utils.AppPreferences;
import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper.NetworkHelperError;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper.NetworkHelperFailureListener;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper.NetworkHelperSuccessListener;
import com.sap.sailing.android.tracking.app.utils.UniqueDeviceUuid;
import com.sap.sailing.android.tracking.app.valueobjects.GpsFix;

/**
 * Service that handles sending of GPS-fixes (fixes) to the Sailing-API.
 * 
 * It checks, after a certain time-interval has passed, if any fixes are
 * available in the database and attempts to send them batch-wise.
 * 
 * (There are two strategies for sending, depending on whether tracking is
 * running. If it's running, only fixes for the current event are being sent. If
 * not, it tries to send fixes for previous events, as well. Assuming there are
 * older unsent fixes that for some reason have not been sent.)
 * 
 * It keeps track of when the last successful sending occurred and, if no new
 * data is available (i.e., if tracking is disabled), ends itself after a
 * certain period. It relies on being restarted by the {@link TrackingService}.
 * 
 * It also ends itself when connectivity is lost. In that case it relies on
 * being restarted by the {@link ConnectivityChangedReceiver}.
 * 
 * It switches into a power saving mode, if a certain battery discharge is
 * reached. It does, however, not do so, if the device is plugged in (= when it
 * is charging). The power saving mode causes the time-interval between check-
 * and transmit-cycles to be increased to conserve power.
 * 
 * @author Lukas Zielinski
 *
 */
public class TransmittingService extends Service {

	private static final String TAG = TransmittingService.class.getName();

	private final int UPDATE_BATCH_SIZE = 1000;
	private final int UPDATE_INTERVAL_DEFAULT = 3000;
	private final int UPDATE_INTERVAL_POWERSAVE_MODE = 30000;
	private final long AUTO_END_SERVICE_AFTER_NANO_PASSED = 10000000000L; // 10
																			// sec
	private final float BATTERY_POWER_SAVE_TRESHOLD = 0.2f;

	private int currentUpdateInterval = UPDATE_INTERVAL_DEFAULT;

	private boolean sendingAttempted = false;
	private boolean lastTransmissionFailed = false;
	private long lastTransmissionTimestamp = 0;

	private AppPreferences prefs;
	private Timer timer;
	private boolean timerRunning;

	private APIConnectivityListener apiConnectivityListener;
	private final IBinder transmittingBinder = new TransmittingBinder();

	/**
	 * True if sending, because if sending takes longer a new attempt should not
	 * be made.
	 */
	private static boolean currentlySending = false;

	@Override
	public IBinder onBind(Intent intent) {
		prefs = new AppPreferences(this);

		if (!timerRunning) {
			startTimer();
		}

		return transmittingBinder;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		prefs = new AppPreferences(this);

		if (intent != null) {
			if (intent.getAction() != null) {
				if (intent.getAction().equals(getString(R.string.transmitting_service_start))) {
					if (!timerRunning) {
						startTimer();
					}
				} else {
					stopTimer();
				}
			} else {
				stopTimer();
			}
		}

		return Service.START_STICKY;
	}

	private void startTimer() {
		timer = new Timer();
		timer.start();
		timerRunning = true;
		if (BuildConfig.DEBUG) {
			ExLog.i(this, "TIMER", "Background update-timer start");
		}

	}

	private void stopTimer() {
		if (timer != null) {
			timer.stop();
			timerRunning = false;
			if (BuildConfig.DEBUG) {
				ExLog.i(this, "TIMER", "Background update-timer stop");
			}
		}
	}

	private void markSuccessfulTransmission() {
		currentlySending = false;

		if (BuildConfig.DEBUG) {
			ExLog.i(this, "TRANSMISSION", "markSuccessfulTransmission");
		}

		lastTransmissionFailed = false;
		lastTransmissionTimestamp = System.nanoTime();
		reportApiConnectivity(APIConnectivity.reachableTransmissionSuccess);
	}

	private void markFailedTransmission() {
		currentlySending = false;

		if (BuildConfig.DEBUG) {
			ExLog.i(this, "TRANSMISSION", "markFailedTransmission");
		}

		lastTransmissionFailed = true;
		lastTransmissionTimestamp = 0;

		if (isConnected()) {
			reportApiConnectivity(APIConnectivity.reachableTransmissionError);
		} else {
			reportApiConnectivity(APIConnectivity.notReachable);
		}
	}

	/**
	 * If time interval has passed without any transmission, the service can
	 * turn itself off. Same for
	 * 
	 * @return
	 */
	private boolean serviceCanShutItselfDown() {
		long currentNanoTime = System.nanoTime();

		if (BuildConfig.DEBUG) {
			ExLog.i(this, "TRANSMISSION", "serviceCanShutItselfDown?");
			ExLog.i(this, "TRANSMISSION", "DELTA:" + (currentNanoTime - lastTransmissionTimestamp));
		}

		// if network is not connected, shutdown. ConnevtivityChangedReceiver
		// should restart us.
		if (!isConnected()) {
			enabledConnectivityReceiver(); // make sure ConnectivityReceiver is
											// running
			return true;
		}

		// if we had a failure, never go to sleep until data is sent
		// successfully.
		if (lastTransmissionFailed) {
			if (BuildConfig.DEBUG) {
				ExLog.i(this, "TRANSMISSION", "returning false, have failed transmission");
			}

			return false;
		}

		if (!sendingAttempted) {
			if (BuildConfig.DEBUG) {
				ExLog.i(this, "TRANSMISSION", "returning true, no sending attempt");
			}

			return true;
		}

		if (currentNanoTime - lastTransmissionTimestamp > AUTO_END_SERVICE_AFTER_NANO_PASSED) {
			if (BuildConfig.DEBUG) {
				ExLog.i(this, "TRANSMISSION", "returning true, timeout");
			}

			return true;
		}

		if (BuildConfig.DEBUG) {
			ExLog.i(this, "TRANSMISSION", "returning false, don't terminate");
		}

		return false;
	}

	private void timerFired() {
		float batteryPct = getBatteryPercentage();
		boolean batteryIsCharging = prefs.getBatteryIsCharging();

		if (prefs.getEnergySavingEnabledByUser()
				|| (batteryPct < BATTERY_POWER_SAVE_TRESHOLD && !batteryIsCharging)) {
			if (BuildConfig.DEBUG) {
				ExLog.i(this, "POWER-LEVELS", "in power saving mode");
			}

			currentUpdateInterval = UPDATE_INTERVAL_POWERSAVE_MODE;
		} else {
			if (BuildConfig.DEBUG) {
				ExLog.i(this, "POWER-LEVELS", "in default power mode");
			}

			currentUpdateInterval = UPDATE_INTERVAL_DEFAULT;
		}

		sendFixesToAPI(null);
		reportUnsentGPSFixesCount(DatabaseHelper.getInstance().getNumberOfUnsentGPSFixes(
				getBaseContext()));
	}

	private boolean getTrackingServiceIsCurrentlyTracking() {
		return prefs.getTrackerIsTracking();
	}

	private void sendFixesToAPI(List<String> failedHosts) {
		// first, lets fetch all unsent fixes
		List<GpsFix> fixes = DatabaseHelper.getInstance().getUnsentFixes(getBaseContext(),
				failedHosts, UPDATE_BATCH_SIZE);
		// store ids so we can delete the rows later
		ArrayList<String> ids = new ArrayList<String>();

		// create JSON
		JSONArray jsonArray = new JSONArray();
		String currentEventId = null;
		String host = null;

		for (GpsFix fix : fixes) {
			if (currentEventId == null) {
				currentEventId = fix.eventId;
				host = fix.host;
			}

			if (currentEventId.equals(fix.eventId)) {
				// still have more unsent Gps-Fixes for this event.
				ids.add(String.valueOf(fix.id));

				JSONObject json = new JSONObject();
				try {
					json.put("course", fix.course);
					json.put("timestamp", fix.timestamp);
					json.put("speed", fix.speed);
					json.put("longitude", fix.longitude);
					json.put("latitude", fix.latitude);
				} catch (JSONException ex) {
					ExLog.i(this, TAG, "Error while building geolocation json " + ex.getMessage());
				}

				jsonArray.put(json);
			} else {
				// we don't have any more Gps-fixes for this batch, end
				// collection, proceed with sending.
				break;
			}

		}
		
		if (jsonArray.length() > 0) {
			// send
			String[] idsArr = new String[ids.size()];

			JSONObject requestObject = new JSONObject();

			try {
				requestObject.put("deviceUuid", UniqueDeviceUuid.getUniqueId(this));
				requestObject.put("fixes", jsonArray);
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (BuildConfig.DEBUG) {
				ExLog.i(this, TAG, "sending gps fix json: " + requestObject.toString());
				ExLog.i(this, TAG,
						"url: " + host + prefs.getServerGpsFixesPostPath());
			}

			if (host != null) {
				if (currentlySending == true)
				{
					ExLog.i(this, TAG, "Can't send fixes, currentlySending flag is set!");
				}
				else
				{
					currentlySending = true;
					sendingAttempted = true; // will be set to false in the listeners

					HttpJsonPostRequest request;
					try {
						request = new HttpJsonPostRequest(new URL(host
								+ prefs.getServerGpsFixesPostPath()), requestObject.toString(), this);
					

					ExLog.i(this, TAG, "transmitting..");
					NetworkHelper.getInstance(this).executeHttpJsonRequestAsnchronously(
							request,
							new FixSubmitListener(ids.toArray(idsArr)),
							new FixSubmitErrorListener(host,
									getTrackingServiceIsCurrentlyTracking(), failedHosts));
					
					} catch (MalformedURLException e) {
						ExLog.w(this, TAG, "Warning, can't send fixes, MalformedURLException: " + e.getMessage());
					}
				}
			} else {
				ExLog.w(this, TAG, "Warning, can't send fixes, host is null!");
			}

		} else {

			if (serviceCanShutItselfDown()) {
				if (BuildConfig.DEBUG) {
					ExLog.i(this,
							TAG,
							"Nothing to send or timeout occurred, Transmitting Service is stopping timer and terminating itself.");
				}

				stopTimer();
				stopSelf();
			}
		}
	}

	/**
	 * Get a count of ALL unsent GPS-fixes.
	 * 
	 * This method is only interested in the GPS-fixes for the currently tracked
	 * event, and needs to find out the id of that event first.
	 * 
	 * @return
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

	private void deleteSynced(String[] fixIdStrings) {
		DatabaseHelper.getInstance().deleteGpsFixes(getBaseContext(), fixIdStrings);
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

	private void enabledConnectivityReceiver() {
		ConnectivityChangedReceiver.enable(this);
	}

	/**
	 * Report API connectivity to listening activity
	 * 
	 * @param apiConnectivity
	 */
	private void reportApiConnectivity(APIConnectivity apiConnectivity) {
		if (apiConnectivityListener != null) {
			apiConnectivityListener.apiConnectivityUpdated(apiConnectivity);
		}
	}

	/**
	 * Report the number of currently unsent GPS-fixes
	 * 
	 * @param unsentGPSFixesCount
	 */
	private void reportUnsentGPSFixesCount(int unsentGPSFixesCount) {
		if (apiConnectivityListener != null) {
			apiConnectivityListener.setUnsentGPSFixesCount(unsentGPSFixesCount);
		}
	}

	// might need this class in the future:
	//
	// private void markAsSynced(String[] fixIdStrings)
	// {
	// for (String idStr: fixIdStrings)
	// {
	// ContentValues updateValues = new ContentValues();
	// updateValues.put(SensorGps.GPS_SYNCED, 1);
	// Uri uri = ContentUris.withAppendedId(SensorGps.CONTENT_URI,
	// Long.parseLong(idStr));
	// getContentResolver().update(uri, updateValues, null, null);
	// }
	// }

	private class FixSubmitListener implements NetworkHelperSuccessListener {
		private String[] ids;

		public FixSubmitListener(String[] ids) {
			this.ids = ids;
		}

		@Override
		public void performAction(JSONObject response) {
			new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					deleteSynced(ids);
					return null;
				}
				
				@Override
				protected void onPostExecute(Void result) {
					super.onPostExecute(result);
					markSuccessfulTransmission();
				}
			}.execute();
		}
	}

	private class FixSubmitErrorListener implements NetworkHelperFailureListener {
		private String host;
		private List<String> failedHosts;
		private boolean currentlyTracking = false;

		public FixSubmitErrorListener(String host, boolean currentlyTracking,
				List<String> failedHosts) {
			this.failedHosts = failedHosts;
			this.host = host;
			this.currentlyTracking = true;
		}

		@Override
		public void performAction(NetworkHelperError error) {
			markFailedTransmission();
			ExLog.e(TransmittingService.this, TAG,
					"Error while sending GPS fix " + error.getMessage());

			if (!currentlyTracking) {
				if (failedHosts == null) {
					failedHosts = new ArrayList<String>();
					failedHosts.add(host);
				}

				sendFixesToAPI(this.failedHosts);
				ExLog.i(TransmittingService.this, TAG,
						"Retrying resend step with this failed hosts list: " + failedHosts);
			}
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
			reportApiConnectivity(APIConnectivity.noAttempt);
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

	public void registerAPIConnectivityListener(APIConnectivityListener listener) {
		apiConnectivityListener = listener;
	}

	public void unregisterAPIConnectivityListener() {
		apiConnectivityListener = null;
	}

	public class TransmittingBinder extends Binder {
		public TransmittingService getService() {
			ExLog.i(TransmittingService.this, TAG, "get Service..");
			return TransmittingService.this;
		}
	}

	public enum APIConnectivity {
		notReachable(0), reachableTransmissionSuccess(1), reachableTransmissionError(2), noAttempt(
				4);

		private final int apiConnectivity;

		APIConnectivity(int connectivity) {
			this.apiConnectivity = connectivity;
		}

		public int toInt() {
			return this.apiConnectivity;
		}
	}

	public interface APIConnectivityListener {
		public void apiConnectivityUpdated(APIConnectivity apiConnectivity);

		public void setUnsentGPSFixesCount(int count);
	}

}
