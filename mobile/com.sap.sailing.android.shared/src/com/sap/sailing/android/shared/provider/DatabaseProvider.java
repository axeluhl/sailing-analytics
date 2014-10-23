
package com.sap.sailing.android.shared.provider;

import java.util.Arrays;
import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.sap.sailing.android.shared.BuildConfig;
import com.sap.sailing.android.shared.data.database.GpsTable;
import com.sap.sailing.android.shared.logging.ExLog;

public class DatabaseProvider extends ContentProvider {

	private final static String TAG = DatabaseProvider.class.getName();

	public final static String AUTHORITY = "com.sap.sailing.android.shared.provider.db";
	public final static Uri DATA_GPS_URI = Uri.parse("content://" + AUTHORITY + "/data/gps");

	private DatabaseHelper dbHelper;
	private ContentResolver cr;

	private final static int DATA_GPS = 1;
	private final static int DATA_GPS_ID = 2;

	private final static UriMatcher uriMatcher;
	private final static HashMap<String, String> gpsTableMap;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "data/gps", DATA_GPS);
		uriMatcher.addURI(AUTHORITY, "data/gps/#", DATA_GPS_ID);

		gpsTableMap = new HashMap<String, String>();
		gpsTableMap.put(GpsTable._ID, GpsTable._ID);
		gpsTableMap.put(GpsTable._TIMESTAMP, GpsTable._TIMESTAMP);
		gpsTableMap.put(GpsTable.ACCURACY, GpsTable.ACCURACY);
		gpsTableMap.put(GpsTable.ALTITUDE, GpsTable.ALTITUDE);
		gpsTableMap.put(GpsTable.BEARING, GpsTable.BEARING);
		gpsTableMap.put(GpsTable.ELAPSEDREALTIME, GpsTable.ELAPSEDREALTIME);
		gpsTableMap.put(GpsTable.LATITUDE, GpsTable.LATITUDE);
		gpsTableMap.put(GpsTable.LONGITUDE, GpsTable.LONGITUDE);
		gpsTableMap.put(GpsTable.PROVIDER, GpsTable.PROVIDER);
		gpsTableMap.put(GpsTable.SPEED, GpsTable.SPEED);
		gpsTableMap.put(GpsTable.TIME, GpsTable.TIME);
		gpsTableMap.put(GpsTable._COUNT, GpsTable._COUNT);
	}

	@Override
	public int delete (Uri uri, String selection, String[] selectionArgs) {
		if (BuildConfig.DEBUG) {
			String message = "delete: uri=" + uri + " selection=[" + selection + "] args=" + Arrays.toString(selectionArgs);
			ExLog.i(getContext(), TAG, message);
		}

		int count = 0;
		String finalWhere;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db != null) {
			switch (uriMatcher.match(uri)) {
			case DATA_GPS:
				count = db.delete(GpsTable.TABLENAME, selection, selectionArgs);
				break;

			case DATA_GPS_ID:
				finalWhere = GpsTable._ID + " = " + uri.getLastPathSegment();
				if (selection != null) {
					finalWhere += " AND " + selection;
				}
				count = db.delete(GpsTable.TABLENAME, finalWhere, selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknown Uri: " + uri);
			}
		}
		cr.notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType (Uri uri) {
		if (BuildConfig.DEBUG) {
			String message = "getType: uri=" + uri;
			ExLog.i(getContext(), TAG, message);
		}

		switch (uriMatcher.match(uri)) {
		case DATA_GPS:
			return GpsTable.CONTENT_TYPE;

		case DATA_GPS_ID:
			return GpsTable.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknow Uri: " + uri);
		}
	}

	@Override
	public Uri insert (Uri uri, ContentValues initialValues) {
		if (BuildConfig.DEBUG) {
			String message = "insert: uri=" + uri + " initialValues=[" + initialValues == null ? "null" : initialValues.toString()
				+ "]";
			ExLog.i(getContext(), TAG, message);
		}

		String tableName;
		Uri contentUri;
		switch (uriMatcher.match(uri)) {
		case DATA_GPS:
			tableName = GpsTable.TABLENAME;
			contentUri = DATA_GPS_URI;
			break;

		default:
			throw new IllegalArgumentException("Unknown Uri: " + uri);
		}

		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		long rowId = 0;
		if (db != null) {
			rowId = db.insert(tableName, null, values);
		}
		if (rowId > 0) {
			cr.notifyChange(uri, null);
			return ContentUris.withAppendedId(contentUri, rowId);
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public boolean onCreate () {
		cr = getContext().getContentResolver();
		dbHelper = new DatabaseHelper(getContext());

		return (cr != null && dbHelper != null);
	}

	@Override
	public Cursor query (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		if (BuildConfig.DEBUG) {
			String message = "query: uri=" + uri + " projection=" + Arrays.toString(projection) + " selection=[" + selection
				+ "] args=" + Arrays.toString(selectionArgs) + " order=[" + sortOrder + "]";
			ExLog.i(getContext(), TAG, message);
		}

		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		switch (uriMatcher.match(uri)) {
		case DATA_GPS:
			queryBuilder.setTables(GpsTable.TABLENAME);
			if (projection == null) {
				queryBuilder.setProjectionMap(gpsTableMap);
			}
			if (sortOrder == null) {
				sortOrder = GpsTable._TIMESTAMP + " DESC ";
			}
			break;

		case DATA_GPS_ID:
			queryBuilder.setTables(GpsTable.TABLENAME);
			if (projection == null) {
				queryBuilder.setProjectionMap(gpsTableMap);
			}
			queryBuilder.appendWhere(GpsTable._ID + " = " + uri.getLastPathSegment());
			break;

		default:
			break;
		}

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		Cursor cursor = null;
		if (db != null) {
			cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		}
		if (cursor != null) {
			cursor.setNotificationUri(cr, uri);
		}
		return cursor;
	}

	@Override
	public int update (Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		if (BuildConfig.DEBUG) {
			String message = "update: uri=" + uri + " values=[" + values == null ? "null" : values.toString() + "] selection=["
				+ selection + "]" + " args=" + Arrays.toString(selectionArgs);
			ExLog.i(getContext(), TAG, message);
		}

		int count = 0;
		String finalWhere;
		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db != null) {
			switch (uriMatcher.match(uri)) {
			case DATA_GPS:
				count = db.update(GpsTable.TABLENAME, values, selection, selectionArgs);
				break;

			case DATA_GPS_ID:
				finalWhere = GpsTable._ID + " = " + uri.getLastPathSegment();
				if (selection != null) {
					finalWhere += " AND " + selection;
				}
				count = db.update(GpsTable.TABLENAME, values, finalWhere, selectionArgs);
				break;

			default:
				throw new IllegalArgumentException("Unknow Uri: " + uri);
			}
		}
		cr.notifyChange(uri, null);
		return count;
	}

	private class DatabaseHelper extends SQLiteOpenHelper {
		
		private final static String DATABASE_NAME = "sap_sailing.db";
		private final static int DATABASE_VERSION = 1;

		private GpsTable GpsTable = new GpsTable();

		public DatabaseHelper (Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate (SQLiteDatabase db) {
			GpsTable.onCreate(db);
		}

		@Override
		public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
			GpsTable.onUpgrade(db, oldVersion, newVersion);
		}

		@Override
		public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
			GpsTable.onDowngrade(db, oldVersion, newVersion);
		}
	}

}
