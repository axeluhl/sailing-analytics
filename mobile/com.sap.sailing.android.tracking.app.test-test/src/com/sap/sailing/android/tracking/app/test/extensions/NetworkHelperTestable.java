package com.sap.sailing.android.tracking.app.test.extensions;

import android.content.Context;
import com.sap.sailing.android.tracking.app.utils.NetworkHelper;

public class NetworkHelperTestable extends NetworkHelper {
	
    public static void injectInstance(Context context, NetworkHelper instance)
    {
    	mContext = context;
    	mInstance = instance;
    }
}
