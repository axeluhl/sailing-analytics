package com.sap.sailing.android.tracking.app.test.extensions;

import android.content.Context;

import com.sap.sailing.android.tracking.app.utils.ServiceHelper;

public class ServiceHelperTestable extends ServiceHelper {

	public ServiceHelperTestable(Context context) {
		super(context);
	}
	
	public static void injectInstance(ServiceHelper instance) {
		mInstance = instance;
	}
}