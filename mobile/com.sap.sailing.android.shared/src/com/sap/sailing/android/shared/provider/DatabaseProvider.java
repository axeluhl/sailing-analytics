
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
import com.sap.sailing.android.shared.data.database.MessageTable;
import com.sap.sailing.android.shared.logging.ExLog;

public class DatabaseProvider extends ContentProvider {

	private final static String TAG = DatabaseProvider.class.getName();

	public final static String AUTHORITY = "com.sap.sailing.android.shared.provider.db";
	public final static Uri GPS_URI = Uri.parse("content://" + AUTHORITY + "/gps");
	public final static Uri MESSAGE_URI = Uri.parse("content://" + AUTHORITY + "/message");

	private DatabaseHelper dbHelper;
	private ContentResolver cr;

	private final static int GPS = 0;
	private final static int GPS_ID = 1;
	private final static int MESSAGE = 10;
	private final static int MESSAGE_ID = 11;

	private final static UriMatcher uriMatcher;
	private final static HashMap<String, String> gpsMap;
	private final static HashMap<String, String> messageMap;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "gps", GPS);
		uriMatcher.addURI(AUTHORITY, "gps/#", GPS_ID);
		uriMatcher.addURI(AUTHORITY, "message", MESSAGE);
		uriMatcher.addURI(AUTHORITY, "message/#", MESSAGE_ID);

		gpsMap = new HashMap<String, String>();
		gpsMap.put(GpsTable._ID, GpsTable._ID);
		gpsMap.put(GpsTable._TIMESTAMP, GpsTable._TIMESTAMP);
		gpsMap.put(GpsTable.ACCURACY, GpsTable.ACCURACY);
		gpsMap.put(GpsTable.ALTITUDE, GpsTable.ALTITUDE);
		gpsMap.put(GpsTable.BEARING, GpsTable.BEARING);
		gpsMap.put(GpsTable.ELAPSEDREALTIME, GpsTable.ELAPSEDREALTIME);
		gpsMap.put(GpsTable.LATITUDE, GpsTable.LATITUDE);
		gpsMap.put(GpsTable.LONGITUDE, GpsTable.LONGITUDE);
		gpsMap.put(GpsTable.PROVIDER, GpsTable.PROVIDER);
		gpsMap.put(GpsTable.SPEED, GpsTable.SPEED);
		gpsMap.put(GpsTable.TIME, GpsTable.TIME);
		gpsMap.put(GpsTable._COUNT, GpsTable._COUNT);

		messageMap = new HashMap<String, String>();
		messageMap.put(MessageTable._ID, MessageTable._ID);
		messageMap.put(MessageTable.URL, MessageTable.URL);
		messageMap.put(MessageTable.CALLBACK_PAYLOAD, MessageTable.CALLBACK_PAYLOAD);
		messageMap.put(MessageTable.PAYLOAD, MessageTable.PAYLOAD);
		messageMap.put(MessageTable.CALLBACK_CLASS_STRING, MessageTable.CALLBACK_CLASS_STRING);
		messageMap.put(MessageTable._COUNT, MessageTable._COUNT);
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
			case GPS:
				count = db.delete(GpsTable.TABLENAME, selection, selectionArgs);
				break;

			case GPS_ID:
				finalWhere = GpsTable._ID + " = " + uri.getLastPathSegment();
				if (selection != null) {
					finalWhere += " AND " + selection;
				}
				count = db.delete(GpsTable.TABLENAME, finalWhere, selectionArgs);
				break;

			case MESSAGE:
				count = db.delete(MessageTable.TABLENAME, selection, selectionArgs);
				break;

			case MESSAGE_ID:
				finalWhere = MessageTable._ID + " = " + uri.getLastPathSegment();
				if (selection != null) {
					finalWhere += " AND " + selection;
				}
				count = db.delete(MessageTable.TABLENAME, finalWhere, selectionArgs);
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
		case GPS:
			return GpsTable.CONTENT_TYPE;

		case GPS_ID:
			return GpsTable.CONTENT_ITEM_TYPE;

		case MESSAGE:
			return MessageTable.CONTENT_TYPE;

		case MESSAGE_ID:
			return MessageTable.CONTENT_ITEM_TYPE;

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
		case GPS:
			tableName = GpsTable.TABLENAME;
			contentUri = GPS_URI;
			break;

		case MESSAGE:
			tableName = MessageTable.TABLENAME;
			contentUri = MESSAGE_URI;
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
		case GPS:
			queryBuilder.setTables(GpsTable.TABLENAME);
			if (projection == null) {
				queryBuilder.setProjectionMap(gpsMap);
			}
			if (sortOrder == null) {
				sortOrder = GpsTable._TIMESTAMP + " DESC ";
			}
			break;

		case GPS_ID:
			queryBuilder.setTables(GpsTable.TABLENAME);
			if (projection == null) {
				queryBuilder.setProjectionMap(gpsMap);
			}
			queryBuilder.appendWhere(GpsTable._ID + " = " + uri.getLastPathSegment());
			break;

		case MESSAGE:
			queryBuilder.setTables(MessageTable.TABLENAME);
			if (projection == null) {
				queryBuilder.setProjectionMap(messageMap);
			}
			if (sortOrder == null) {
				sortOrder = MessageTable._ID + " DESC ";
			}
			break;

		case MESSAGE_ID:
			queryBuilder.setTables(MessageTable.TABLENAME);
			if (projection == null) {
				queryBuilder.setProjectionMap(messageMap);
			}
			queryBuilder.appendWhere(MessageTable._ID + " = " + uri.getLastPathSegment());
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
			case GPS:
				count = db.update(GpsTable.TABLENAME, values, selection, selectionArgs);
				break;

			case GPS_ID:
				finalWhere = GpsTable._ID + " = " + uri.getLastPathSegment();
				if (selection != null) {
					finalWhere += " AND " + selection;
				}
				count = db.update(GpsTable.TABLENAME, values, finalWhere, selectionArgs);
				break;

			case MESSAGE:
				count = db.update(MessageTable.TABLENAME, values, selection, selectionArgs);
				break;

			case MESSAGE_ID:
				finalWhere = MessageTable._ID + " = " + uri.getLastPathSegment();
				if (selection != null) {
					finalWhere += " AND " + selection;
				}
				count = db.update(MessageTable.TABLENAME, values, finalWhere, selectionArgs);
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

		private GpsTable gpsTable = new GpsTable();
		private MessageTable messageTable = new MessageTable();

		public DatabaseHelper (Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate (SQLiteDatabase db) {
			gpsTable.onCreate(db);
			messageTable.onCreate(db);
		}

		@Override
		public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
			gpsTable.onUpgrade(db, oldVersion, newVersion);
			messageTable.onUpgrade(db, oldVersion, newVersion);
		}

		@Override
		public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
			gpsTable.onDowngrade(db, oldVersion, newVersion);
			messageTable.onDowngrade(db, oldVersion, newVersion);
		}
	}
}
