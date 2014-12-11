package com.sap.sailing.android.tracking.app.test.extensions;

import android.content.Context;

import com.sap.sailing.android.tracking.app.utils.VolleyHelper;

public class VolleyHelperTestable extends VolleyHelper {

	public VolleyHelperTestable(Context context) {
		super(context);
	}

	private static Context mContext;
	
	@Override
	public Context getApplicationContext() {
		return mContext;
	}
	
    public static void injectInstance(Context context, VolleyHelper instance)
    {
    	mContext = context;
    	mInstance = instance;
    }
}