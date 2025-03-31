package com.sap.sailing.android.shared.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import com.sap.sailing.android.shared.data.http.HttpJsonGetRequest;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.shared.logging.ExLog;

import android.content.Context;
import android.os.AsyncTask;

public class LogoutTask extends AsyncTask<String, Void, Void> {

    private static final String TAG = LogoutTask.class.getName();
    private static final String LOGOUT_REQUEST = "/security/api/restsecurity/remove_access_token";

    private Context context;
    private URL url;

    public LogoutTask(Context ctx, String baseUrl) {
        context = ctx;
        try {
            url = new URL(baseUrl + LOGOUT_REQUEST);
        } catch (MalformedURLException e) {
            ExLog.e(context, TAG,
                    "Error: Failed to perform checking due to a MalformedURLException: " + e.getMessage());

        }
    }

    @Override
    protected Void doInBackground(String... params) {
        if (url != null && params != null && params.length > 0) {
            HttpRequest request = new HttpJsonGetRequest(context, url);
            try {
                request.execute();
            } catch (IOException e) {
                ExLog.e(context, TAG, "Logout not possible: " + e.getMessage());
            }
        }
        return null;
    }
}
