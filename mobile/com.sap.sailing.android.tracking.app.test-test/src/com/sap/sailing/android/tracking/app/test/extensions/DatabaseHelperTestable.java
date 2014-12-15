package com.sap.sailing.android.tracking.app.test.extensions;

import com.sap.sailing.android.tracking.app.utils.DatabaseHelper;

public class DatabaseHelperTestable extends DatabaseHelper {

	public DatabaseHelperTestable() {
		super();
	}

	public static void injectInstance(DatabaseHelper instance) {
		mInstance = instance;
	}

}
