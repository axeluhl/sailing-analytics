package com.sap.sailing.android.tracking.app.test.extensions;

import android.content.Context;

import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;

public class DatabaseHelperTestable extends DatabaseHelper {

	public DatabaseHelperTestable(Context context) {
		super(context);
	}

	public static void injectInstance(DatabaseHelper instance) {
		mInstance = instance;
	}

}
