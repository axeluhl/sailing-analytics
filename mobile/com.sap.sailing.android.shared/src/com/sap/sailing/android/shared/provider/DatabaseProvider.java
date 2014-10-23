
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
import android.provider.BaseColumns;

import com.sap.sailing.android.shared.BuildConfig;
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
	private final static HashMap<String, String> dataGpsMap;

	static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, "data/gps", DATA_GPS);
		uriMatcher.addURI(AUTHORITY, "data/gps/#", DATA_GPS_ID);

		dataGpsMap = new HashMap<String, String>();
		dataGpsMap.put(DataGps._ID, DataGps._ID);
		dataGpsMap.put(DataGps._TIMESTAMP, DataGps._TIMESTAMP);
		dataGpsMap.put(DataGps.ACCURACY, DataGps.ACCURACY);
		dataGpsMap.put(DataGps.ALTITUDE, DataGps.ALTITUDE);
		dataGpsMap.put(DataGps.BEARING, DataGps.BEARING);
		dataGpsMap.put(DataGps.ELAPSEDREALTIME, DataGps.ELAPSEDREALTIME);
		dataGpsMap.put(DataGps.LATITUDE, DataGps.LATITUDE);
		dataGpsMap.put(DataGps.LONGITUDE, DataGps.LONGITUDE);
		dataGpsMap.put(DataGps.PROVIDER, DataGps.PROVIDER);
		dataGpsMap.put(DataGps.SPEED, DataGps.SPEED);
		dataGpsMap.put(DataGps.TIME, DataGps.TIME);
		dataGpsMap.put(DataGps._COUNT, DataGps._COUNT);
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
				count = db.delete(DataGps.TABLENAME, selection, selectionArgs);
				break;

			case DATA_GPS_ID:
				finalWhere = DataGps._ID + " = " + uri.getLastPathSegment();
				if (selection != null) {
					finalWhere += " AND " + selection;
				}
				count = db.delete(DataGps.TABLENAME, finalWhere, selectionArgs);
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
			return DataGps.CONTENT_TYPE;

		case DATA_GPS_ID:
			return DataGps.CONTENT_ITEM_TYPE;

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
			tableName = DataGps.TABLENAME;
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
			queryBuilder.setTables(DataGps.TABLENAME);
			if (projection == null) {
				queryBuilder.setProjectionMap(dataGpsMap);
			}
			if (sortOrder == null) {
				sortOrder = DataGps._TIMESTAMP + " DESC ";
			}
			break;

		case DATA_GPS_ID:
			queryBuilder.setTables(DataGps.TABLENAME);
			if (projection == null) {
				queryBuilder.setProjectionMap(dataGpsMap);
			}
			queryBuilder.appendWhere(DataGps._ID + " = " + uri.getLastPathSegment());
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
				count = db.update(DataGps.TABLENAME, values, selection, selectionArgs);
				break;

			case DATA_GPS_ID:
				finalWhere = DataGps._ID + " = " + uri.getLastPathSegment();
				if (selection != null) {
					finalWhere += " AND " + selection;
				}
				count = db.update(DataGps.TABLENAME, values, finalWhere, selectionArgs);
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

		private DataGps dataGps = new DataGps();

		public DatabaseHelper (Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate (SQLiteDatabase db) {
			dataGps.onCreate(db);
		}

		@Override
		public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
			dataGps.onUpgrade(db, oldVersion, newVersion);
		}

		@Override
		public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
			dataGps.onDowngrade(db, oldVersion, newVersion);
		}
	}

	public interface BaseTable {

		public final static String _TIMESTAMP = "_timestamp";

		public abstract void onCreate (SQLiteDatabase db);

		public abstract void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion);

		public abstract void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion);
	}

	public class DataGps implements BaseTable, BaseColumns {
		
		public final static String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.sap.sailing.content.gpsdata";
		public final static String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.sap.sailing.content.gpsdata";

		public final static String TABLENAME = "data_gps";

		public final static String ACCURACY = "accuracy";
		public final static String ALTITUDE = "altitude";
		public final static String BEARING = "bearing";
		public final static String ELAPSEDREALTIME = "elapsedrealtime";
		public final static String LATITUDE = "latitude";
		public final static String LONGITUDE = "longitude";
		public final static String PROVIDER = "provider";
		public final static String SPEED = "speed";
		public final static String TIME = "time";

		@Override
		public void onCreate (SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TABLENAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + _TIMESTAMP + " TEXT, "
				+ ACCURACY + " REAL, " + ALTITUDE + " REAL, " + BEARING + " REAL, " + ELAPSEDREALTIME + " TEXT, " + LATITUDE
				+ " REAL, " + LONGITUDE + " REAL, " + PROVIDER + " TEXT, " + SPEED + " REAL, " + TIME + " TEXT " + ");");
		}

		@Override
		public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
			onCreate(db);
		}

		@Override
		public void onDowngrade (SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + TABLENAME);
			onCreate(db);
		}
	}
}
