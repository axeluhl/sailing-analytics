package com.sap.sailing.android.tracking.app.test.extensions;

import android.content.Context;

import com.sap.sailing.android.tracking.app.utils.VolleyHelper;

public class VolleyHelperTestable extends VolleyHelper {

	public VolleyHelperTestable(Context context) {
		super(context);
	}

    public static void injectInstance(VolleyHelper instance)
    {
    	mInstance = instance;
    }
}