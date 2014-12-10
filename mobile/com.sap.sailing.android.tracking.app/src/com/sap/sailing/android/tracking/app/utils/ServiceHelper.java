package com.sap.sailing.android.tracking.app.utils;

import android.content.Context;
import android.content.Intent;

import com.sap.sailing.android.tracking.app.R;
import com.sap.sailing.android.tracking.app.services.TrackingService;
import com.sap.sailing.android.tracking.app.services.TransmittingService;

/**
 * Helper class that starts services.
 * @author Lukas Zielinski
 *
 */
public class ServiceHelper {

	private final static String TAG = ServiceHelper.class.getName();

	protected static ServiceHelper mInstance;
	protected Context mContext;

	protected ServiceHelper(Context context) {
		mContext = context;
	}

	public static synchronized ServiceHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new ServiceHelper(context);
		}

		return mInstance;
	}

	/**
	 * Start transmitting service. It should be run at least once when the app
	 * starts for the first time, and every time, when a tracking gps-fix is 
	 * stored, to ensure it gets sent to the API-server.
	 */
	public void startTransmittingService() {
		Intent intent = new Intent(mContext, TransmittingService.class);
		intent.setAction(mContext.getString(R.string.transmitting_service_start));
		mContext.startService(intent);
	}
	
	/**
	 * Start tracking service with a given eventId. The eventId is used to determine
	 * the event from the database in order to set the host-address correctly.
	 * @param eventId id of the event (not the row-id)
	 */
	public void startTrackingService(String eventId)
	{
		Intent intent = new Intent(mContext, TrackingService.class);
		intent.setAction(mContext.getString(R.string.tracking_service_start));
		intent.putExtra(mContext.getString(R.string.tracking_service_event_id_parameter), eventId);
		mContext.startService(intent);
	}
	
	/**
	 * Stop tracking service.
	 */
	public void stopTrackingService()
	{
		Intent intent = new Intent(mContext, TrackingService.class);
		intent.setAction(mContext.getString(R.string.tracking_service_stop));
		mContext.startService(intent);
	}
}
