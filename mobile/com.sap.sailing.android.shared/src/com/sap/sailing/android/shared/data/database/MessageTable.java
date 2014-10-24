package com.sap.sailing.android.shared.data.database;

import android.content.ContentResolver;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;

public class MessageTable implements BaseTable, BaseColumns {

	public final static String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.sap.sailing.content.message";
	public final static String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.sap.sailing.content.message";

	public final static String TABLENAME = "messages";

	public final static String URL = "url";
	public final static String CALLBACK_PAYLOAD = "callbackPayload";
	public final static String PAYLOAD = "payload";
	public final static String CALLBACK_CLASS_STRING = "callbackClassString";

	@Override
	public void onCreate (SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TABLENAME + " (" + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + URL + " TEXT, "
			+ CALLBACK_PAYLOAD + " TEXT, " + PAYLOAD + " TEXT, " + CALLBACK_CLASS_STRING + " TEXT, " + ");");
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
