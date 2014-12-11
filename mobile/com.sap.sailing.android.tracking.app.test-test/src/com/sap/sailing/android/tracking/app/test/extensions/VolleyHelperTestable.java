package com.sap.sailing.android.tracking.app.test.extensions;

import com.sap.sailing.android.tracking.app.utils.VolleyHelper;

public class VolleyHelperTestable extends VolleyHelper {

    public static void injectInstance(VolleyHelper instance)
    {
    	mInstance = instance;
    }
}