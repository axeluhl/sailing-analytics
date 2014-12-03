package com.sap.sailing.android.tracking.app.provider;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.SelectionBuilder;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Leaderboard;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.SensorGps;
import com.sap.sailing.android.tracking.app.provider.AnalyticsDatabase.Tables;

public class AnalyticsProvider extends ContentProvider {

    private static final String TAG = AnalyticsProvider.class.getName();
    
    private AnalyticsDatabase mOpenHelper;
    
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    
    private static final int COMPETITOR = 100;
    private static final int COMPETITOR_ID = 101;
    
    private static final int EVENT = 200;
    private static final int EVENT_ID = 201;
    
    private static final int LEADERBOARD = 300;
    private static final int LEADERBOARD_ID = 301;
    
    private static final int MESSAGE = 400;
    private static final int MESSAGE_ID = 401;
    
    private static final int SENSOR_GPS = 500;
    private static final int SENSOR_GPS_ID = 501;
    
    private static final int EVENT_LEADERBOARD_COMPETITOR_JOINED = 600;
    
    private static final int EVENT_GPS_FIXES_JOINED = 700;
    
    private static final int LEADERBOARDS_EVENTS_JOINED = 800;
    
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AnalyticsContract.CONTENT_AUTHORITY;
        
        matcher.addURI(authority, "competitors", COMPETITOR);
        matcher.addURI(authority, "competitors/*", COMPETITOR_ID);
        
        matcher.addURI(authority, "leaderboards", LEADERBOARD);
        matcher.addURI(authority, "leaderboards/*", LEADERBOARD_ID);

        matcher.addURI(authority, "events", EVENT);
        matcher.addURI(authority, "events/*", EVENT_ID);

        matcher.addURI(authority, "messages", MESSAGE);
        matcher.addURI(authority, "messages/#", MESSAGE_ID);
        
        matcher.addURI(authority, "sensor_gps", SENSOR_GPS);
        matcher.addURI(authority, "sensor_gps/#", SENSOR_GPS_ID);
        
        matcher.addURI(authority, "event_leaderboard_competitor_joined", EVENT_LEADERBOARD_COMPETITOR_JOINED);
        
        matcher.addURI(authority, "event_gps_fix_joined",  EVENT_GPS_FIXES_JOINED);
        
        matcher.addURI(authority, "leaderboards_events_joined", LEADERBOARDS_EVENTS_JOINED);
        
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new AnalyticsDatabase(getContext());
        return true;
    }
    
//    private void deleteDatabase() {
//        Context context = getContext();
//        mOpenHelper.close();
//        AnalyticsDatabase.deleteDatabase(context);
//        mOpenHelper = new AnalyticsDatabase(context);
//    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (BuildConfig.DEBUG) {
            String message = "query: uri=" + uri + " projection=" + Arrays.toString(projection) + " selection=["
                    + selection + "] args=" + Arrays.toString(selectionArgs) + " order=[" + sortOrder + "]";
            ExLog.i(getContext(), TAG, message);
        }

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        Cursor cursor;
        
        switch (sUriMatcher.match(uri)) {
        
            case EVENT_LEADERBOARD_COMPETITOR_JOINED:
            	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            	qb.setTables(Tables.EVENTS_JOIN_LEADERBOARDS_JOIN_COMPETITORS);
            	cursor = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            	return cursor;
            	
            case EVENT_GPS_FIXES_JOINED:
            	SQLiteQueryBuilder eb = new SQLiteQueryBuilder();
            	eb.setTables(Tables.GPS_FIXES_JOIN_EVENTS);
            	cursor = eb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            	return cursor;
            	
            case LEADERBOARDS_EVENTS_JOINED:
            	SQLiteQueryBuilder el = new SQLiteQueryBuilder();
            	el.setTables(Tables.LEADERBOARDS_JOIN_EVENTS);
            	cursor = el.query(db, projection, selection, selectionArgs, null, null, sortOrder);
            	return cursor;
            	
            default:
            	final SelectionBuilder builder = buildExpandedSelection(uri);
                
                cursor = builder
                        .where(selection, selectionArgs)
                        .query(db, false, projection, sortOrder, null);
                
                Context context = getContext();
                if (context != null) {
                    cursor.setNotificationUri(context.getContentResolver(), uri);
                }
                
                return cursor;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case COMPETITOR:
            return Competitor.CONTENT_TYPE;
            
        case COMPETITOR_ID:
            return Competitor.CONTENT_ITEM_TYPE;
            
        case EVENT:
            return Event.CONTENT_TYPE;
            
        case EVENT_ID:
            return Event.CONTENT_ITEM_TYPE;
            
        case LEADERBOARD:
        	return Leaderboard.CONTENT_TYPE;
        	
        case LEADERBOARD_ID:
        	return Leaderboard.CONTENT_ITEM_TYPE;
            
        case SENSOR_GPS:
            return SensorGps.CONTENT_TYPE;
            
        case SENSOR_GPS_ID:
            return SensorGps.CONTENT_ITEM_TYPE;
            
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (BuildConfig.DEBUG) {
            String message = "insert: uri=" + uri + " values=[" + values.toString() + "]";
            ExLog.i(getContext(), TAG, message);
        }
        
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        switch (sUriMatcher.match(uri)) {
        case COMPETITOR:
            db.insertOrThrow(Tables.COMPETITORS, null, values);
            notifyChange(uri);
            return Competitor.buildCompetitorUri(values.getAsString(Competitor.COMPETITOR_ID));
            
        case EVENT:
            db.insertOrThrow(Tables.EVENTS, null, values);
            notifyChange(uri);
            return Event.buildEventUri(values.getAsString(Event.EVENT_ID));
            
        case LEADERBOARD:
        	db.insertOrThrow(Tables.LEADERBOARDS, null, values);
        	notifyChange(uri);
        	return Leaderboard.buildLeaderboardUri(values.getAsString(BaseColumns._ID));
            
        case SENSOR_GPS:
            db.insertOrThrow(Tables.SENSOR_GPS, null, values);
            notifyChange(uri);
            return SensorGps.buildSensorGpsUri("XX");
            
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (BuildConfig.DEBUG) {
            String message = "delete: uri=" + uri + " selection=[" + selection + "] args="
                    + Arrays.toString(selectionArgs);
            ExLog.i(getContext(), TAG, message);
        }
        
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (sUriMatcher.match(uri)) {
		
		case SENSOR_GPS:
			int numGpsFixesDeleted = db.delete(Tables.SENSOR_GPS, selection, selectionArgs);
			notifyChange(uri);
			return numGpsFixesDeleted;

		case SENSOR_GPS_ID:
			String idStr = uri.getLastPathSegment();
		    String where = SensorGps._ID + " = " + idStr;
		    int numGpsFixesWithIdDeleted = db.delete(Tables.SENSOR_GPS, where, selectionArgs);
			notifyChange(uri);
			return numGpsFixesWithIdDeleted;
			
        case COMPETITOR:
        	int numCompetitorRowsDeleted = db.delete(Tables.COMPETITORS, selection, selectionArgs);
            notifyChange(uri);
            return numCompetitorRowsDeleted;
            
        case EVENT:
        	int numEventRowsDeleted = db.delete(Tables.EVENTS, selection, selectionArgs);
            notifyChange(uri);
            return numEventRowsDeleted;
            
        case LEADERBOARD:
        	int numLeaderboardRowsDeleted = db.delete(Tables.LEADERBOARDS, selection, selectionArgs);
            notifyChange(uri);
            return numLeaderboardRowsDeleted;

		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}		
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (BuildConfig.DEBUG) {
			String message = "update: uri=" + uri + " values=[" + values == null ? "null"
					: values.toString() + "] selection=[" + selection + "]"
							+ " args=" + Arrays.toString(selectionArgs);
			ExLog.i(getContext(), TAG, message);
		}

		final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (sUriMatcher.match(uri)) {

		case SENSOR_GPS_ID:
			String idStr = uri.getLastPathSegment();
		    String where = SensorGps._ID + " = " + idStr;
			int numRowsAffected = db.update(Tables.SENSOR_GPS, values, where, selectionArgs);
			notifyChange(uri);
			return numRowsAffected;

		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}		
	}

    private void notifyChange(Uri uri) {
        getContext().getContentResolver().notifyChange(uri, null);
    }
    
    private SelectionBuilder buildExpandedSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder(getContext());
        
        switch(sUriMatcher.match(uri)) {
        case COMPETITOR:
            return builder.table(Tables.COMPETITORS);
            
        case COMPETITOR_ID:
            final String competitor_id = Competitor.getCompetitorId(uri);
            return builder.table(Tables.COMPETITORS)
                    .where(Competitor.COMPETITOR_ID + " = ?", competitor_id);
            
        case LEADERBOARD:
        	return builder.table(Tables.LEADERBOARDS);
        	
        case LEADERBOARD_ID:
        	final String leaderboard_id = Leaderboard.getLeaderboardId(uri);
            return builder.table(Tables.LEADERBOARDS)
                    .where(BaseColumns._ID + " = ?", leaderboard_id);
            
        case EVENT:
            return builder.table(Tables.EVENTS);
            
        case EVENT_ID:
            final String event_id = Event.getEventId(uri);
            return builder.table(Tables.EVENTS)
                    .where(Event.EVENT_ID + " = ?", event_id);
            
        case SENSOR_GPS:
            return builder.table(Tables.SENSOR_GPS);
            
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri); 
        }
    }
}
