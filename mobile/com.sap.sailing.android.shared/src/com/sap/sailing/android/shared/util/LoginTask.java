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

import com.sap.sailing.android.shared.data.LoginData;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.shared.data.http.LoginGetRequest;
import com.sap.sailing.android.shared.logging.ExLog;

public class LoginTask extends AsyncTask<LoginData, Void, String> {

    private static final String TAG = LoginTask.class.getName();

    private final static String TOKEN_REQUEST = "/security/api/restsecurity/access_token";

    private URL mUrl = null;
    private Context mContext;
    private WeakReference<LoginTaskListener> mListener;
    private Exception mException;

    public LoginTask(Context context, String baseUrl, LoginTaskListener listener) {
        try {
            mContext = context.getApplicationContext();
            mUrl = new URL(baseUrl + TOKEN_REQUEST);
            mListener = new WeakReference<>(listener);
        } catch (MalformedURLException e) {
            ExLog.e(context, TAG, "Error: Failed to perform checking due to a MalformedURLException: " + e.getMessage());
        }
    }

    @Override
    protected String doInBackground(LoginData... params) {
        String access_token = null;
        if (mUrl != null && params != null && params.length > 0) {
            try {
                HttpRequest request = new LoginGetRequest(mUrl, mContext, params[0]);
                InputStream responseStream = request.execute();

                JSONParser parser = new JSONParser();
                JSONObject result = (JSONObject) parser.parse(new InputStreamReader(responseStream));
                access_token = (String) result.get("access_token");
            } catch (Exception e) {
                mException = e;
            }
        } else {
            mException = new IllegalArgumentException();
        }
        return access_token;
    }

    @Override
    protected void onPostExecute(String accessToken) {
        super.onPostExecute(accessToken);
        LoginTaskListener listener = mListener.get();
        if (listener != null) {
            if (mException != null) {
                listener.onException(mException);
            } else {
                listener.onTokenReceived(accessToken);
            }
        }
    }

    public interface LoginTaskListener {
        void onTokenReceived(String accessToken);

        void onException(Exception exception);
    }
}
