package com.sap.sailing.android.tracking.app.provider;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.sap.sailing.android.shared.logging.ExLog;
import com.sap.sailing.android.shared.util.SelectionBuilder;
import com.sap.sailing.android.tracking.app.BuildConfig;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Competitor;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Event;
import com.sap.sailing.android.tracking.app.provider.AnalyticsContract.Message;
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
    private static final int EVENT_ID_COMPETITOR = 202;
    
    private static final int EVENT_COMPETITOR = 300;
    
    private static final int MESSAGE = 400;
    private static final int MESSAGE_ID = 401;
    
    private static final int SENSOR_GPS = 500;
    private static final int SENSOR_GPS_ID = 501;
    
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AnalyticsContract.CONTENT_AUTHORITY;
        
        matcher.addURI(authority, "competitors", COMPETITOR);
        matcher.addURI(authority, "competitors/*", COMPETITOR_ID);
        
        matcher.addURI(authority, "events/competitors", EVENT_COMPETITOR);

        matcher.addURI(authority, "events", EVENT);
        matcher.addURI(authority, "events/*", EVENT_ID);
        matcher.addURI(authority, "events/*/competitors", EVENT_ID_COMPETITOR);

        matcher.addURI(authority, "messages", MESSAGE);
        matcher.addURI(authority, "messages/#", MESSAGE_ID);
        
        matcher.addURI(authority, "sensor_gps", SENSOR_GPS);
        matcher.addURI(authority, "sensor_gps/#", SENSOR_GPS_ID);
        
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new AnalyticsDatabase(getContext());
        return true;
    }
    
    private void deleteDatabase() {
        Context context = getContext();
        mOpenHelper.close();
        AnalyticsDatabase.deleteDatabase(context);
        mOpenHelper = new AnalyticsDatabase(context);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (BuildConfig.DEBUG) {
            String message = "query: uri=" + uri + " projection=" + Arrays.toString(projection) + " selection=["
                    + selection + "] args=" + Arrays.toString(selectionArgs) + " order=[" + sortOrder + "]";
            ExLog.i(getContext(), TAG, message);
        }

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        
        switch (sUriMatcher.match(uri)) {
            default:
                final SelectionBuilder builder = buildExpandedSelection(uri);
                
                Cursor cursor = builder
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
            
        case EVENT_ID_COMPETITOR:
            return Event.CONTENT_ITEM_TYPE;
            
        case EVENT_COMPETITOR:
            return AnalyticsContract.EventCompetitor.CONTENT_TYPE;
            
        case MESSAGE:
            return Message.CONTENT_TYPE;
            
        case MESSAGE_ID:
            return Message.CONTENT_ITEM_TYPE;
            
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
            
        case EVENT_ID_COMPETITOR:
            db.insertOrThrow(Tables.EVENTS_COMPETITORS, null, values);
            notifyChange(uri);
            return Event.buildCompetitorsDirUri(values.getAsString(Event.EVENT_ID));
            
        case MESSAGE:
            db.insertOrThrow(Tables.MESSAGES, null, values);
            notifyChange(uri);
            return Message.buildMessageUri("XX");
            
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

        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (BuildConfig.DEBUG) {
            String message = "update: uri=" + uri + " values=[" + values == null ? "null" : values.toString()
                    + "] selection=[" + selection + "]" + " args=" + Arrays.toString(selectionArgs);
            ExLog.i(getContext(), TAG, message);
        }

        return 0;
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
            
        case EVENT:
            return builder.table(Tables.EVENTS);
            
        case EVENT_ID:
            final String event_id = Event.getEventId(uri);
            return builder.table(Tables.EVENTS)
                    .where(Event.EVENT_ID + " = ?", event_id);
            
        case EVENT_ID_COMPETITOR:
            final String event_competitor_id = Event.getEventId(uri);
            return builder.table(Tables.EVENTS_JOIN_COMPETITOR)
                    .mapToTable(BaseColumns._ID, Tables.EVENTS_COMPETITORS)
                    .mapToTable(Competitor.COMPETITOR_ID, Tables.COMPETITORS)
                    .mapToTable(Event.EVENT_ID, Tables.EVENTS)
                    .where(Event.EVENT_ID + " = ?", event_competitor_id);
            
        case EVENT_COMPETITOR:
            return builder.table(Tables.EVENTS_JOIN_COMPETITOR)
                    .mapToTable(BaseColumns._ID, Tables.EVENTS_COMPETITORS)
                    .mapToTable(Competitor.COMPETITOR_ID, Tables.COMPETITORS)
                    .mapToTable(Event.EVENT_ID, Tables.EVENTS);
            
        case MESSAGE:
            return builder.table(Tables.MESSAGES);
            
        case SENSOR_GPS:
            return builder.table(Tables.SENSOR_GPS);
            
        default:
            throw new UnsupportedOperationException("Unknown uri: " + uri); 
        }
    }
}
