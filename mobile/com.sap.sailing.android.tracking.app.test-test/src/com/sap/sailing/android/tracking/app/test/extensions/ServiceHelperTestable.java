package com.sap.sailing.android.tracking.app.test.extensions;

import com.sap.sailing.android.tracking.app.utils.ServiceHelper;

public class ServiceHelperTestable extends ServiceHelper {

	public static void injectInstance(ServiceHelper instance) {
		mInstance = instance;
	}
}