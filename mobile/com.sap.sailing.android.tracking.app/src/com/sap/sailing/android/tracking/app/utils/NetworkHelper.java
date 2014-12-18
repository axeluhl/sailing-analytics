package com.sap.sailing.android.tracking.app.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;

import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.shared.logging.ExLog;

public class NetworkHelper {
	
	private final static String TAG = NetworkHelper.class.getName();
	
	static NetworkHelper mInstance;
	static Context mContext;
	
	public static NetworkHelper getInstance(Context context) {
		if (mInstance == null)
		{
			mInstance = new NetworkHelper();
			mContext = context;
		}
		return mInstance;
	}
    
    protected NetworkHelper(){
    	super();
    }

	public void executeHttpRequestAsnchronously(HttpRequest request,
			NetworkHelperSuccessListener successListener, NetworkHelperFailureListener failureListener) {
		
		NetworkRequestTask task = new NetworkRequestTask(successListener, failureListener, false);
		task.execute(request);
    }

	public void executeHttpJsonRequestAsnchronously(HttpRequest request,
			NetworkHelperSuccessListener successListener, NetworkHelperFailureListener failureListener) {
		
		NetworkRequestTask task = new NetworkRequestTask(successListener, failureListener, true);
		task.execute(request);
    }
	
	private class NetworkRequestTask extends AsyncTask<HttpRequest, Void, Void>
	{
		private NetworkHelperSuccessListener successListener;
		private NetworkHelperFailureListener failureListener;
		private boolean expectingJsonObject;

		public NetworkRequestTask(NetworkHelperSuccessListener successListener, 
				NetworkHelperFailureListener failureListener, boolean expectingJsonObject) {
			this.successListener = successListener;
			this.failureListener = failureListener;
			this.expectingJsonObject = expectingJsonObject;
		}
		
		@Override
		protected Void doInBackground(HttpRequest... params) {
			InputStream stream = null;
			HttpRequest request = params[0];
			try {
				stream = request.execute();
				String response = readStream(stream);
				
				if (expectingJsonObject)
				{
					JSONObject jsonObject = new JSONObject(response);
					successListener.performAction(jsonObject);
				}
				else
				{
					successListener.performAction(response);
				}
			} catch (IOException e) {
				failureListener.performAction();
			} catch (JSONException e) {
				ExLog.e(mContext, TAG, "Failed to parse JSON: " + e.getMessage());
				failureListener.performAction();
			}
			
			return null;
		}
		
		private String readStream(InputStream inputStream) throws IOException {
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        byte[] buffer = new byte[1024];
	        int length = 0;
	        while ((length = inputStream.read(buffer)) != -1) {
	            baos.write(buffer, 0, length);
	        }
	        return new String(baos.toByteArray(), Charset.defaultCharset());
	    }
	}
    
    public interface NetworkHelperSuccessListener { public void performAction(Object response); }
    public interface NetworkHelperFailureListener { public void performAction(); }
}
