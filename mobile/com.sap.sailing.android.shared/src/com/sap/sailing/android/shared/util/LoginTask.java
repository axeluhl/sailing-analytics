package com.sap.sailing.android.shared.util;

import java.io.InputStream;
import java.io.InputStreamReader;
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

    private URL url = null;
    private Context context;
    private LoginTaskListener listener;
    private Exception exception;

    public LoginTask(Context context, String baseUrl, LoginTaskListener listener) {
        try {
            this.context = context.getApplicationContext();
            this.url = new URL(baseUrl + TOKEN_REQUEST);
            this.listener = listener;
        } catch (MalformedURLException e) {
            ExLog.e(context, TAG, "Error: Failed to perform checking due to a MalformedURLException: " + e.getMessage());
        }
    }

    @Override
    protected String doInBackground(LoginData... params) {
        String access_token = null;
        if (url != null && params != null && params.length > 0) {
            try {
                HttpRequest request = new LoginGetRequest(url, context, params[0]);
                InputStream responseStream = request.execute();

                JSONParser parser = new JSONParser();
                JSONObject result = (JSONObject) parser.parse(new InputStreamReader(responseStream));
                access_token = (String) result.get("access_token");
            } catch (Exception e) {
                exception = e;
            }
        } else {
            exception = new IllegalArgumentException();
        }
        return access_token;
    }

    @Override
    protected void onPostExecute(String accessToken) {
        super.onPostExecute(accessToken);
        if (listener != null) {
            if (exception != null) {
                listener.onException(exception);
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
