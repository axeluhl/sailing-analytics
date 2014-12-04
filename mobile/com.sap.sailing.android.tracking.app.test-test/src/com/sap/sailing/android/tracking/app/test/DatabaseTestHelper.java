package com.sap.sailing.android.tracking.app.test;

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.EventGpsFixesJoined;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.SensorGps;
import com.sap.sailing.android.tracking.app.services.GpsFix;

/**
 * Testing helper methods
 * @author Lukas Zielinski
 *
 */
public class DatabaseTestHelper {

	static final String TAG = DatabaseTestHelper.class.getName();
	
	static public void deleteAllGpsFixesFromDB(Context context)
	{
		ContentResolver cr = context.getContentResolver();
		cr.delete(SensorGps.CONTENT_URI, null, null);
		ExLog.i(context, TAG, "deleteAllGpsFixesFromDB");
	}
	
	static public ArrayList<GpsFix> getAllGpsFixesFromDB(Context context) 
	{
		ArrayList<GpsFix> result = new ArrayList<GpsFix>();
		String selectionClause = SensorGps.GPS_SYNCED + " = 0";
		// differs from trackingService projectionClause, taking event_id from sensor_gps.event_id,
		// because event does not exist.
		String projectionClauseStr = "sensor_gps.event_id as _eid,sensor_gps.gps_time,sensor_gps.gps_latitude,"
				+ "sensor_gps.gps_longitude,sensor_gps.gps_speed,sensor_gps.gps_bearing,sensor_gps.gps_synced,"
				+ "events.event_server,sensor_gps._id as _gid";
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(EventGpsFixesJoined.CONTENT_URI, projectionClauseStr.split(","), selectionClause, null, null);
		while (cursor.moveToNext()) {
			GpsFix gpsFix = new GpsFix();
			
			for (int i = 0; i < cursor.getColumnCount(); i++)
			{
				System.out.println(cursor.getColumnName(i) + " -~> " + cursor.getString(i));
			}
			
			gpsFix.id = cursor.getInt(cursor.getColumnIndex("_gid"));
			gpsFix.timestamp = cursor.getLong(cursor.getColumnIndex(SensorGps.GPS_TIME));
			gpsFix.latitude  = cursor.getDouble(cursor.getColumnIndex(SensorGps.GPS_LATITUDE));
			gpsFix.longitude  = cursor.getDouble(cursor.getColumnIndex(SensorGps.GPS_LONGITUDE));
			gpsFix.speed  = cursor.getDouble(cursor.getColumnIndex(SensorGps.GPS_SPEED));
			gpsFix.course  = cursor.getDouble(cursor.getColumnIndex(SensorGps.GPS_BEARING));
			gpsFix.synced = cursor.getInt(cursor.getColumnIndex(SensorGps.GPS_SYNCED));
			gpsFix.host = cursor.getString(cursor.getColumnIndex(Event.EVENT_SERVER));
			gpsFix.eventId = cursor.getString(cursor.getColumnIndex("_eid"));
			
			result.add(gpsFix);
		}
		
		cursor.close();
		
		ExLog.i(context, TAG, "getAllGpsFixesFromDB: " + result);
		return result;
	}
	
	static public int getNumberOfEventsFromDB(Context context)
	{
		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(Event.CONTENT_URI, null, null, null, null);
		int result = cursor.getCount();
		cursor.close();
		return result;
	}
	
	static public void deleteAllEventsFromDB(Context context)
	{
		ContentResolver cr = context.getContentResolver();
		cr.delete(Event.CONTENT_URI, null, null);
		ExLog.i(context, TAG, "deleteAllEventsFromDB");
	}

	public static void createNewGpsFixInDatabase(Context context, long eventId, double lat, double lon, double accuracy, long timestamp)
	{
		ContentResolver cr = context.getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(SensorGps.GPS_LATITUDE, lat);
		cv.put(SensorGps.GPS_LONGITUDE, lon);
		cv.put(SensorGps.GPS_ACCURACY, accuracy);
		cv.put(SensorGps.GPS_TIME, timestamp);
		cv.put(SensorGps.GPS_EVENT_FK, eventId);
		cv.put(SensorGps.GPS_SYNCED, 0);
		cr.insert(SensorGps.CONTENT_URI, cv);
	}
	
	public static long createNewEventInDBAndReturnItsId(Context context, String eventName, String eventId)
	{
		ContentResolver cr = context.getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(Event.EVENT_NAME, eventName);
		cv.put(Event.EVENT_ID, eventId);
		cv.put(Event.EVENT_SERVER, "127.0.0.1");
		Uri uri = cr.insert(Event.CONTENT_URI, cv);
		return ContentUris.parseId(uri);
	}
}
