package com.sap.sailing.android.shared.util;

import java.io.InputStream;
import java.io.InputStreamReader;
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

    private Context context;
    private URL url;
    private AuthCheckTaskListener listener;
    private Exception exception;

    public AuthCheckTask(Context ctx, String baseUrl, AuthCheckTaskListener taskListener) {
        try {
            context = ctx;
            url = new URL(baseUrl + HELLO_REQUEST);
            listener = taskListener;
        } catch (MalformedURLException e) {
            ExLog.e(context, TAG, "Error: Failed to perform checking due to a MalformedURLException: " + e.getMessage());
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean authenticated = false;
        if (url != null) {
            try {
                HttpRequest request = new HttpJsonGetRequest(url, context);
                InputStream responseStream = request.execute();

                JSONParser parser = new JSONParser();
                JSONObject json = (JSONObject) parser.parse(new InputStreamReader(responseStream));
                if (json.containsKey("authenticated")) {
                    authenticated = (Boolean) json.get("authenticated");
                }
            } catch (Exception e) {
                exception = e;
            }
        } else {
            exception = new IllegalArgumentException();
        }
        return authenticated;
    }

    @Override
    protected void onPostExecute(Boolean authenticated) {
        super.onPostExecute(authenticated);
        if (listener != null) {
            if (exception != null) {
                listener.onException(exception);
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
