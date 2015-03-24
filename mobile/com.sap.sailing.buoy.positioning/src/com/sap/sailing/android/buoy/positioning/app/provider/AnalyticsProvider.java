package com.sap.sailing.android.buoy.positioning.app.provider;

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

import com.sap.sailing.android.buoy.positioning.app.BuildConfig;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.CheckinUri;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.Leaderboard;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.Mark;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsContract.MarkPing;
import com.sap.sailing.android.buoy.positioning.app.provider.AnalyticsDatabase.Tables;
import com.sap.sailing.android.buoy.positioning.app.util.AppPreferences;
import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.SelectionBuilder;

public class AnalyticsProvider extends ContentProvider {

    private static final String TAG = AnalyticsProvider.class.getName();
    
    private AnalyticsDatabase mOpenHelper;
    
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    
    private static final int LEADERBOARD = 100;
    private static final int LEADERBOARD_ID = 101;
    
    private static final int MARK = 200;
    private static final int MARK_ID = 201;
    
    private static final int MARK_PING = 300;
    private static final int MARK_PING_ID = 301;
    
    private static final int MESSAGE = 400;
    private static final int MESSAGE_ID = 401;

    private static final int CHECKIN_URI = 500;
    private static final int CHECKIN_URI_ID = 501;
    
    private static final int LEADERBOARDS_MARKS_JOINED = 600;
    
    private static final int MARKS_LEADERBOARDS_JOINED = 700;
    
    

    
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AnalyticsContract.CONTENT_AUTHORITY;
        
        matcher.addURI(authority, "leaderboards", LEADERBOARD);
        matcher.addURI(authority, "leaderboards/*", LEADERBOARD_ID);

        matcher.addURI(authority, "checkin_uris", CHECKIN_URI);
        matcher.addURI(authority, "checkin_uris/*", CHECKIN_URI_ID);
        
        matcher.addURI(authority, "marks", MARK);
        matcher.addURI(authority, "marks/*", MARK_ID);

        matcher.addURI(authority, "mark_pings", MARK_PING);
        matcher.addURI(authority, "mark_pings/*", MARK_PING_ID);

        matcher.addURI(authority, "messages", MESSAGE);
        matcher.addURI(authority, "messages/#", MESSAGE_ID);
        
        matcher.addURI(authority, "leaderboards_marks_joined", LEADERBOARDS_MARKS_JOINED);
        
        matcher.addURI(authority, "marks_leaderboards_joined", MARKS_LEADERBOARDS_JOINED);
        
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
        if (BuildConfig.DEBUG && AppPreferences.getPrintDatabaseOperationDebugMessages()) {
            String message = "query: uri=" + uri + " projection=" + Arrays.toString(projection) + " selection=["
                    + selection + "] args=" + Arrays.toString(selectionArgs) + " order=[" + sortOrder + "]";
            ExLog.i(getContext(), TAG, message);
        }

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        Cursor cursor;
        
        switch (sUriMatcher.match(uri)) {
        
        case LEADERBOARDS_MARKS_JOINED:
        	SQLiteQueryBuilder el = new SQLiteQueryBuilder();
        	el.setTables(Tables.LEADERBOARDS_MARKS_JOINED);
        	cursor = el.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        	return cursor;
        	
        case MARKS_LEADERBOARDS_JOINED:
        	SQLiteQueryBuilder el2 = new SQLiteQueryBuilder();
        	el2.setTables(Tables.MARKS_LEADERBOARDS_JOINED);
        	cursor = el2.query(db, projection, selection, selectionArgs, null, null, sortOrder);
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
        case LEADERBOARD:
        	return Leaderboard.CONTENT_TYPE;
        	
        case LEADERBOARD_ID:
        	return Leaderboard.CONTENT_ITEM_TYPE;
        case CHECKIN_URI:
            return CheckinUri.CONTENT_TYPE;
        case CHECKIN_URI_ID:
            return CheckinUri.CONTENT_ITEM_TYPE;
        case MARK:
        	return Mark.CONTENT_TYPE;
        case MARK_ID:
        	return Mark.CONTENT_ITEM_TYPE;
        case MARK_PING:
        	return MarkPing.CONTENT_TYPE;
        case MARK_PING_ID:
        	return MarkPing.CONTENT_ITEM_TYPE;
            
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (BuildConfig.DEBUG && AppPreferences.getPrintDatabaseOperationDebugMessages()) {
            String message = "insert: uri=" + uri + " values=[" + values.toString() + "]";
            ExLog.i(getContext(), TAG, message);
        }
        
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        switch (sUriMatcher.match(uri)) {

        case LEADERBOARD:
        	long leaderboardId = db.insertOrThrow(Tables.LEADERBOARDS, null, values);
        	notifyChange(uri);
        	return Leaderboard.buildLeaderboardUri(String.valueOf(leaderboardId));
        case CHECKIN_URI:
            long checkinUriID = db.insertOrThrow(Tables.CHECKIN_URIS, null, values);
            notifyChange(uri);
            return CheckinUri.builCheckInUri(String.valueOf(checkinUriID));
        case MARK:
        	long markID = db.insertOrThrow(Tables.MARKS, null, values);
        	notifyChange(uri);
        	return Mark.builMarkUri(String.valueOf(markID));
        case MARK_PING:
        	long markPingId = db.insertOrThrow(Tables.MARK_PINGS, null, values);
        	notifyChange(uri);
        	return Mark.builMarkUri(String.valueOf(markPingId));
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (BuildConfig.DEBUG && AppPreferences.getPrintDatabaseOperationDebugMessages()) {
            String message = "delete: uri=" + uri + " selection=[" + selection + "] args="
                    + Arrays.toString(selectionArgs);
            ExLog.i(getContext(), TAG, message);
        }
        
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (sUriMatcher.match(uri)) {
			
        case LEADERBOARD:
        	int numLeaderboardRowsDeleted = db.delete(Tables.LEADERBOARDS, selection, selectionArgs);
            notifyChange(uri);
            return numLeaderboardRowsDeleted;
        case CHECKIN_URI:
            int numCheckinUriRowsDeleted = db.delete(Tables.CHECKIN_URIS, selection, selectionArgs);
            notifyChange(uri);
            return numCheckinUriRowsDeleted;
        case MARK:
        	int numMarkRowsDeleted = db.delete(Tables.MARKS, selection, selectionArgs);
            notifyChange(uri);
            return numMarkRowsDeleted;
        case MARK_PING:
        	int numMarkPingRowsDeleted = db.delete(Tables.MARK_PINGS, selection, selectionArgs);
            notifyChange(uri);
            return numMarkPingRowsDeleted;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}		
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (BuildConfig.DEBUG && AppPreferences.getPrintDatabaseOperationDebugMessages()) {
			String message = "update: uri=" + uri + " values=[" + values == null ? "null"
					: values.toString() + "] selection=[" + selection + "]"
							+ " args=" + Arrays.toString(selectionArgs);
			ExLog.i(getContext(), TAG, message);
		}

		//final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		switch (sUriMatcher.match(uri)) {
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
            
        case LEADERBOARD:
        	return builder.table(Tables.LEADERBOARDS);
        	
        case LEADERBOARD_ID:
        	final String leaderboard_id = Leaderboard.getLeaderboardId(uri);
            return builder.table(Tables.LEADERBOARDS)
                    .where(BaseColumns._ID + " = ?", leaderboard_id);
            
        case CHECKIN_URI:
            return builder.table(Tables.CHECKIN_URIS);
        case CHECKIN_URI_ID:
            final String checkinUriId = AnalyticsContract.CheckinUri.getCheckinUriId(uri);
            return builder.table(Tables.CHECKIN_URIS)
                    .where(BaseColumns._ID + " = ?", checkinUriId);
        case MARK:
            return builder.table(Tables.MARKS);
        case MARK_ID:
        	final String mark_id = Mark.getMarkId(uri);
            return builder.table(Tables.MARKS)
                    .where(BaseColumns._ID + " = ?", mark_id);
        case MARK_PING:
        	return builder.table(Tables.MARK_PINGS);
        case MARK_PING_ID:
        	final String mark_ping_id = MarkPing.getMarkPingId(uri);
            return builder.table(Tables.MARK_PINGS)
                    .where(BaseColumns._ID + " = ?", mark_ping_id);
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri); 
        }
    }
}
