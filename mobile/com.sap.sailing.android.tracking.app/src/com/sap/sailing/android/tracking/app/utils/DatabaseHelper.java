package com.sap.sailing.android.tracking.app.utils;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.EventGpsFixesJoined;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.SensorGps;
import com.sap.sailing.android.tracking.app.services.GpsFix;

public class DatabaseHelper {

	private final static String TAG = DatabaseHelper.class.getName();
	
	private static DatabaseHelper mInstance;
	private Context mContext;

	private DatabaseHelper(Context context) {
		mContext = context;
	}

	public static synchronized DatabaseHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DatabaseHelper(context);
		}

		return mInstance;
	}

	public List<GpsFix> getUnsentFixes(List<String> failedHosts, int updateBatchSize) {
		String selectionClause = SensorGps.GPS_SYNCED + " = 0";
		String projectionClauseStr = "events._id as _eid,sensor_gps.gps_time,sensor_gps.gps_latitude,"
				+ "sensor_gps.gps_longitude,sensor_gps.gps_speed,sensor_gps.gps_bearing,sensor_gps.gps_synced,"
				+ "events.event_server,sensor_gps._id as _gid";
		String[] projectionClause = projectionClauseStr.split(",");
		String sortAndLimitClause = SensorGps.GPS_TIME + " DESC LIMIT "
				+ updateBatchSize;

		if (failedHosts != null) {
			if (failedHosts.size() > 0) {
				StringBuffer buf = new StringBuffer();
				buf.append("( ");

				for (String failedHost : failedHosts) {
					buf.append("\"" + failedHost + "\",");
				}

				// remove the last comma
				buf.setLength(buf.length() - 1);
				buf.append(" )");

				selectionClause += " AND " + Event.EVENT_SERVER + " NOT IN "
						+ buf.toString();
			}
		}

		ArrayList<GpsFix> list = new ArrayList<GpsFix>();
		Cursor cur = mContext.getContentResolver().query(
				EventGpsFixesJoined.CONTENT_URI, projectionClause,
				selectionClause, null, sortAndLimitClause);
		while (cur.moveToNext()) {

			GpsFix gpsFix = new GpsFix();

			gpsFix.id = cur.getInt(cur.getColumnIndex("_gid"));
			gpsFix.timestamp = cur.getLong(cur.getColumnIndex(SensorGps.GPS_TIME));
			gpsFix.latitude = cur.getDouble(cur.getColumnIndex(SensorGps.GPS_LATITUDE));
			gpsFix.longitude = cur.getDouble(cur.getColumnIndex(SensorGps.GPS_LONGITUDE));
			gpsFix.speed = cur.getDouble(cur.getColumnIndex(SensorGps.GPS_SPEED));
			gpsFix.course = cur.getDouble(cur.getColumnIndex(SensorGps.GPS_BEARING));
			gpsFix.synced = cur.getInt(cur.getColumnIndex(SensorGps.GPS_SYNCED));
			gpsFix.host = cur.getString(cur.getColumnIndex(Event.EVENT_SERVER));
			gpsFix.eventId = cur.getString(cur.getColumnIndex("_eid"));

			list.add(gpsFix);

			if (list.size() >= updateBatchSize) {
				break;
			}
		}

		cur.close();
		return list;
	}
	
	public int getNumberOfUnsentGPSFixes()
	{
		String selectionClause = SensorGps.GPS_SYNCED + " = 0";
		String sortAndLimitClause = SensorGps.GPS_TIME + " DESC LIMIT 1";
	
		int eventId = -1;

		Cursor cur = mContext.getContentResolver().query(
				EventGpsFixesJoined.CONTENT_URI,
				new String[] { "events._id as _eid" }, 
				selectionClause, 
				null,
				sortAndLimitClause);
		
		while (cur.moveToNext()) {
			eventId = cur.getInt(0);
		}
		
		cur.close();
	
		if (eventId == -1)
		{
			if (BuildConfig.DEBUG)
			{
				ExLog.i(mContext, TAG, "no event id, reporting 0 gps-fixes.");
			}
			return 0;
		}
		
		String selectionClause2 = "events._id = " + eventId;
		Cursor countCursor = mContext.getContentResolver().query(
				EventGpsFixesJoined.CONTENT_URI,
				new String[] { "count(*) AS count" }, selectionClause2, null, null);

		countCursor.moveToFirst();
		int count = countCursor.getInt(0);
		countCursor.close();

		return count;
	}
	
	public int deleteGpsFixes(String[] fixIdStrings)
	{
		int numDeleted = 0;
		for (String idStr: fixIdStrings)
		{
			ContentValues updateValues = new ContentValues();
			updateValues.put(SensorGps.GPS_SYNCED, 1);
			Uri uri = ContentUris.withAppendedId(SensorGps.CONTENT_URI, Long.parseLong(idStr));
			numDeleted = mContext.getContentResolver().delete(uri, null, null);
		}
		
		return numDeleted;
	}
	
	public long getRowIdForEventId(String eventId)
	{
		int result = 0;
		
		ContentResolver cr = mContext.getContentResolver();
		Cursor cursor = cr.query(Event.CONTENT_URI, null, "event_id = \"" + eventId + "\"", null, null);
		cursor.moveToFirst();
		result = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
		cursor.close();
		return result;
	}


	/**
	 * for testing.
	 * 
	 * @param instance
	 */
	public static void injectInstance(DatabaseHelper instance) {
		mInstance = instance;
	}
}
