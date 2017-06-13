package com.sap.sailing.android.shared.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import android.content.Context;
import android.os.AsyncTask;

import com.sap.sailing.android.shared.data.http.HttpJsonGetRequest;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.shared.logging.ExLog;

public class AuthCheckTask extends AsyncTask<Void, Void, Boolean> {

    private static final String TAG = AuthCheckTask.class.getName();

    private final static String HELLO_REQUEST = "/security/api/restsecurity/hello";

    private Context mContext;
    private URL mUrl;
    private WeakReference<AuthCheckTaskListener> mListener;
    private Exception mException;

    public AuthCheckTask(Context ctx, String baseUrl, AuthCheckTaskListener taskListener) {
        try {
            mContext = ctx;
            mUrl = new URL(baseUrl + HELLO_REQUEST);
            mListener = new WeakReference<>(taskListener);
        } catch (MalformedURLException e) {
            ExLog.e(mContext, TAG, "Error: Failed to perform checking due to a MalformedURLException: " + e.getMessage());
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean authenticated = false;
        if (mUrl != null) {
            try {
                HttpRequest request = new HttpJsonGetRequest(mContext, mUrl);
                InputStream responseStream = request.execute();

                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(new InputStreamReader(responseStream));
                if (json.containsKey("authenticated")) {
                    authenticated = (Boolean) json.get("authenticated");
                }
            } catch (Exception e) {
                mException = e;
            }
        } else {
            mException = new IllegalArgumentException();
        }
        return authenticated;
    }

    @Override
    protected void onPostExecute(Boolean authenticated) {
        super.onPostExecute(authenticated);
        AuthCheckTaskListener listener = mListener.get();
        if (listener != null) {
            if (mException != null) {
                listener.onException(mException);
            } else {
                listener.onRequestReceived(authenticated);
            }
        }
    }

    public interface AuthCheckTaskListener {
        void onRequestReceived(Boolean authenticated);

        void onException(Exception exception);
    }
}
