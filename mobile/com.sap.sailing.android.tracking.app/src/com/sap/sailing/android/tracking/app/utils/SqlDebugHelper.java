package com.sap.sailing.android.tracking.app.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Leaderboard;

public class SqlDebugHelper {
	
	static final public String TAG = SqlDebugHelper.class.getName();
	
	public static void dumpAllTablesToConsole(Context context)
	{
		ExLog.w(context, TAG, "--- PRINT ALL SQL" );
		ExLog.w(context, TAG, "+++ LEADERBOARDS +++");
		
		ContentResolver cr = context.getContentResolver();
		Cursor c1 = cr.query(Leaderboard.CONTENT_URI, null, null, null, null);
		
		while (c1.moveToNext())
		{
			StringBuffer sb = new StringBuffer();
			sb.append("_ID: " + c1.getString(c1.getColumnIndex(BaseColumns._ID)) + ", ");
			sb.append("NAME: " + c1.getString(c1.getColumnIndex(Leaderboard.LEADERBOARD_NAME)) + "\n");
			sb.append("DIGEST: " + c1.getString(c1.getColumnIndex(Leaderboard.LEADERBOARD_CHECKIN_DIGEST)) + "\n");
			ExLog.w(context, TAG, "Leaderboard: " + sb.toString());
		}
		
		ExLog.w(context, TAG, "+++ EVENTS +++");
		
		c1.close();
		
		Cursor c2 = cr.query(Event.CONTENT_URI, null, null, null, null);
		
		while (c2.moveToNext())
		{
			StringBuffer sb = new StringBuffer();
			sb.append("_ID: " + c2.getString(c2.getColumnIndex(BaseColumns._ID)) + ", ");
			sb.append("ID: " + c2.getString(c2.getColumnIndex(Event.EVENT_ID)) + ", ");
			sb.append("NAME: " + c2.getString(c2.getColumnIndex(Event.EVENT_NAME)) + ", ");
			sb.append("DATE START: " + c2.getString(c2.getColumnIndex(Event.EVENT_DATE_START)) + ", ");
			sb.append("DATE END: " + c2.getString(c2.getColumnIndex(Event.EVENT_DATE_END)) + ", ");
			sb.append("IMAGE-URL: " + c2.getString(c2.getColumnIndex(Event.EVENT_IMAGE_URL)) + ", ");
			sb.append("SERVER: " + c2.getString(c2.getColumnIndex(Event.EVENT_SERVER)) + "\n");
			sb.append("DIGEST: " + c2.getString(c2.getColumnIndex(Event.EVENT_CHECKIN_DIGEST)) + "\n");

			ExLog.w(context, TAG, "Event: " + sb.toString());
		}

		ExLog.w(context, TAG, "+++ COMPETITORS +++");
		
		c2.close();
		
		Cursor c3 = cr.query(Competitor.CONTENT_URI, null, null, null, null);
		
		while (c3.moveToNext())
		{
			StringBuffer sb = new StringBuffer();
			sb.append("_ID: " + c3.getString(c3.getColumnIndex(BaseColumns._ID)) + ", ");
			sb.append("ID: " + c3.getString(c3.getColumnIndex(Competitor.COMPETITOR_ID)) + ", ");
			sb.append("DISPLAY NAME: " + c3.getString(c3.getColumnIndex(Competitor.COMPETITOR_DISPLAY_NAME)) + ", ");
			sb.append("NATIONALITY: " + c3.getString(c3.getColumnIndex(Competitor.COMPETITOR_NATIONALITY)) + ", ");
			sb.append("COUNTRY CODE: " + c3.getString(c3.getColumnIndex(Competitor.COMPETITOR_COUNTRY_CODE)) + ", ");
			sb.append("SAIL-ID: " + c3.getString(c3.getColumnIndex(Competitor.COMPETITOR_SAIL_ID)) + "\n");
			sb.append("DIGEST: " + c3.getString(c3.getColumnIndex(Competitor.COMPETITOR_CHECKIN_DIGEST)) + "\n");
			
			ExLog.w(context, TAG, "Competitor: " + sb.toString());
		}
		
		c3.close();
		
		ExLog.w(context, TAG, "--- END OF SQL PRINTOUT" );
	}
}
