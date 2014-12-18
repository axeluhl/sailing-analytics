package com.sap.sailing.android.tracking.app.utils;

import org.json.JSONObject;

import android.app.Application;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

public class VolleyHelper extends Application {

    private static final String TAG = VolleyHelper.class.getName();
    
    private RequestQueue mRequestQueue;
    private Context mContext;
    
    protected static VolleyHelper mInstance;
    
    protected VolleyHelper(Context context){
    	super();
    	mContext = context;
    }
    
    public static synchronized VolleyHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleyHelper(context);
        }
        
        return mInstance;
    }
    
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }
        
        return mRequestQueue;
    }

	public <T> void enqueueRequest(String urlStr, JSONObject requestJsonObject,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		addRequest(new JsonObjectOrStatusOnlyRequest(urlStr, requestJsonObject, listener,
				errorListener));
	}
    
    public <T> void addRequest(Request<T> request) {
        addRequest(request, TAG);
    }

    public <T> void addRequest(Request<T> request, Object tag) {
        request.setTag((tag == null) ? TAG : tag);
        getRequestQueue().add(request);
    }
    
    public void cancelRequest(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll((tag == null) ? TAG : tag);
        }
    }
}
