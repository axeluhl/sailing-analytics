package com.sap.sailing.android.tracking.app.utils;

import android.content.Context;
import android.content.Intent;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TransmittingService;

/**
 * Helper class that starts services.
 * @author Lukas Zielinski
 *
 */
public class ServiceHelper {

	//private final static String TAG = ServiceHelper.class.getName();

	protected static ServiceHelper mInstance;

	public static synchronized ServiceHelper getInstance() {
		if (mInstance == null) {
			mInstance = new ServiceHelper();
		}
		return mInstance;
	}

	/**
	 * Start transmitting service. It should be run at least once when the app
	 * starts for the first time, and every time, when a tracking gps-fix is 
	 * stored, to ensure it gets sent to the API-server.
	 */
	public void startTransmittingService(Context context) {
		Intent intent = new Intent(context, TransmittingService.class);
		intent.setAction(context.getString(R.string.transmitting_service_start));
		context.startService(intent);
	}
	
	/**
	 * Start tracking service with a given checkinDigest. The checkinDigest is used to determine
	 * the event from the database in order to set the host-address correctly.
	 * @param checkinDigest of the event
	 */
	public void startTrackingService(Context context, String checkinDigest)
	{
		Intent intent = new Intent(context, TrackingService.class);
		intent.setAction(context.getString(R.string.tracking_service_start));
		intent.putExtra(context.getString(R.string.tracking_service_checkin_digest_parameter), checkinDigest);
		context.startService(intent);
	}
	
	/**
	 * Stop tracking service.
	 */
	public void stopTrackingService(Context context)
	{
		Intent intent = new Intent(context, TrackingService.class);
		intent.setAction(context.getString(R.string.tracking_service_stop));
		context.startService(intent);
	}
}