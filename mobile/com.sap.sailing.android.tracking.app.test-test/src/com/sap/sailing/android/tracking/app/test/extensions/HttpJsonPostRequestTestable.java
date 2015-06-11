package com.sap.sailing.android.tracking.app.test.extensions;

import java.lang.reflect.Field;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.sap.sailing.android.shared.data.http.HttpJsonPostRequest;

public class HttpJsonPostRequestTestable extends HttpJsonPostRequest {

	public HttpJsonPostRequestTestable(URL requestUrl, String body, Context context) {
		super(requestUrl, body, context);
	}

	public JSONObject getPayloadJSON() throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException, JSONException{
		Class<?> clazz = getClass().getSuperclass();
        Field requestBodyField = clazz.getDeclaredField("requestBody");
        requestBodyField.setAccessible(true);
        
		return new JSONObject((String)requestBodyField.get(this));
	}
	
	public URL getUrl() throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException
	{
		Class<?> clazz = getClass().getSuperclass();
        Field requestBodyField = clazz.getDeclaredField("url");
        requestBodyField.setAccessible(true);
        
		return (URL)requestBodyField.get(this);
	}

}
