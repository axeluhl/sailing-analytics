package com.sap.sailing.android.buoy.positioning.app.provider;

import java.util.Arrays;

import android.content.ContentProvider;
import android.content.ContentResolver;
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

    private static final int CHECKIN_URI = 500;
    private static final int CHECKIN_URI_ID = 501;

    private static final int LEADERBOARDS_MARKS_JOINED = 600;

    private static final int MARKS_LEADERBOARDS_JOINED = 700;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = AnalyticsContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, Tables.LEADERBOARDS, LEADERBOARD);
        matcher.addURI(authority, Tables.LEADERBOARDS + "/*", LEADERBOARD_ID);

        matcher.addURI(authority, Tables.CHECKIN_URIS, CHECKIN_URI);
        matcher.addURI(authority, Tables.CHECKIN_URIS + "/*", CHECKIN_URI_ID);

        matcher.addURI(authority, Tables.MARKS, MARK);
        matcher.addURI(authority, Tables.MARKS + "/*", MARK_ID);

        matcher.addURI(authority, Tables.MARK_PINGS, MARK_PING);
        matcher.addURI(authority, Tables.MARK_PINGS + "/*", MARK_PING_ID);

        matcher.addURI(authority, "leaderboards_marks_joined", LEADERBOARDS_MARKS_JOINED);

        matcher.addURI(authority, "marks_leaderboards_joined", MARKS_LEADERBOARDS_JOINED);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new AnalyticsDatabase(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (BuildConfig.DEBUG) {
            ExLog.i(getContext(), TAG,
                "query() called with: uri = [" + uri + "], projection = [" + Arrays.toString(projection) + "], selection = [" + selection
                    + "], selectionArgs = [" + Arrays.toString(selectionArgs) + "], sortOrder = [" + sortOrder + "]");
        }

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        Cursor cursor;

        String table = null;
        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case LEADERBOARDS_MARKS_JOINED:
                table = Tables.LEADERBOARDS_MARKS_JOINED;
                break;

            case MARKS_LEADERBOARDS_JOINED:
                table = Tables.MARKS_LEADERBOARDS_JOINED;
                break;

            default:
                break;
        }

        if (table != null) {
            builder.setTables(table);
            cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        } else {
            final SelectionBuilder selectionBuilder = buildExpandedSelection(uri);

            cursor = selectionBuilder.where(selection, selectionArgs).query(db, false, projection, sortOrder, null);

            Context context = getContext();
            if (context != null) {
                cursor.setNotificationUri(context.getContentResolver(), uri);
            }
        }
        return cursor;
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
        if (BuildConfig.DEBUG) {
            ExLog.i(getContext(), TAG, "insert() called with: uri = [" + uri + "], values = [" + values + "]");
        }

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String table;
        switch (sUriMatcher.match(uri)) {
            case LEADERBOARD:
                table = Tables.LEADERBOARDS;
                break;

            case CHECKIN_URI:
                table = Tables.CHECKIN_URIS;
                break;

            case MARK:
                table = Tables.MARKS;
                break;

            case MARK_PING:
                table = Tables.MARK_PINGS;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        long id = db.insertOrThrow(table, null, values);
        notifyChange(uri);
        return uri.buildUpon().appendPath(String.valueOf(id)).build();
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (BuildConfig.DEBUG) {
            ExLog.i(getContext(), TAG,
                "delete() called with: uri = [" + uri + "], selection = [" + selection + "], selectionArgs = ["
                    + Arrays.toString(selectionArgs) + "]");
        }

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String table;
        switch (sUriMatcher.match(uri)) {
            case LEADERBOARD:
                table = Tables.LEADERBOARDS;
                break;

            case CHECKIN_URI:
                table = Tables.CHECKIN_URIS;
                break;

            case MARK:
                table = Tables.MARKS;
                break;

            case MARK_PING:
                table = Tables.MARK_PINGS;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int rowsDeleted = db.delete(table, selection, selectionArgs);
        notifyChange(uri);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (BuildConfig.DEBUG) {
            ExLog.i(getContext(), TAG,
                "update() called with: uri = [" + uri + "], values = [" + values + "], selection = [" + selection + "], selectionArgs = [" + Arrays
                    .toString(selectionArgs) + "]");
        }

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        String table;
        switch (sUriMatcher.match(uri)) {
            case LEADERBOARD:
                table = Tables.LEADERBOARDS;
                break;

            case MARK:
                table = Tables.MARKS;
                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        int rowsUpdated = db.update(table, values, selection, selectionArgs);
        notifyChange(uri);
        return rowsUpdated;
    }

    private void notifyChange(Uri uri) {
        ContentResolver cr = getContext().getContentResolver();
        if (cr != null) {
            cr.notifyChange(uri, null);
        }
    }

    private SelectionBuilder buildExpandedSelection(Uri uri) {
        final SelectionBuilder builder = new SelectionBuilder(getContext());

        switch (sUriMatcher.match(uri)) {
            case LEADERBOARD:
                return builder.table(Tables.LEADERBOARDS);

            case LEADERBOARD_ID:
                return builder.table(Tables.LEADERBOARDS).where(BaseColumns._ID + " = ?", uri.getLastPathSegment());

            case CHECKIN_URI:
                return builder.table(Tables.CHECKIN_URIS);

            case CHECKIN_URI_ID:
                return builder.table(Tables.CHECKIN_URIS).where(BaseColumns._ID + " = ?", uri.getLastPathSegment());

            case MARK:
                return builder.table(Tables.MARKS);

            case MARK_ID:
                return builder.table(Tables.MARKS).where(BaseColumns._ID + " = ?", uri.getLastPathSegment());

            case MARK_PING:
                return builder.table(Tables.MARK_PINGS);

            case MARK_PING_ID:
                return builder.table(Tables.MARK_PINGS).where(BaseColumns._ID + " = ?", uri.getLastPathSegment());

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }
}
