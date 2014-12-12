package com.sap.sailing.android.tracking.app.utils;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;

public class JsonStatusOnlyRequest extends JsonObjectRequest {

	public JsonStatusOnlyRequest(String url, JSONObject jsonRequest, Listener<JSONObject> listener,
			ErrorListener errorListener) {
		super(url, jsonRequest, listener, errorListener);
	}

	public JsonStatusOnlyRequest(int method, String url, JSONObject jsonRequest,
			Listener<JSONObject> listener, ErrorListener errorListener) {
		super(method, url, jsonRequest, listener, errorListener);
	}

	@Override
	protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data,
					HttpHeaderParser.parseCharset(response.headers));
			// here's the new code, if jsonString.length() == 0 don't parse
			if (jsonString.length() == 0) {
				return Response.success(null, HttpHeaderParser.parseCacheHeaders(response));
			}
			// end of patch
			return Response.success(new JSONObject(jsonString),
					HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JSONException je) {

			return Response.error(new ParseError(je));
		}
	}

}
