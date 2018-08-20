package com.sap.sailing.android.shared.util;

import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONObject;

import com.sap.sailing.android.shared.data.LoginData;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.shared.data.http.LoginGetRequest;
import com.sap.sailing.android.shared.util.LoginTask.LoginTaskListener;

import android.content.Context;

public class LoginTask extends AbstractAsyncJsonTask<LoginData, Void, String, LoginTaskListener> {
    private final static String TOKEN_REQUEST = "/security/api/restsecurity/access_token";

    /**
     * @param listener
     *            a listener to be called back when task execution has completed; the listener will only be
     *            {@link WeakReference referenced weakly} here, so if the caller lets go of all strong references to it
     *            then it becomes eligible for garbage collection, and no call back will take place anymore.
     */
    public LoginTask(Context context, String baseUrl, LoginTaskListener listener) throws MalformedURLException {
        super(context, listener, new URL(baseUrl + TOKEN_REQUEST));
    }

    @Override
    protected String getResult(JSONObject result) {
        return (String) result.get("access_token");
    }

    @Override
    protected HttpRequest createRequest(LoginData params) {
        return new LoginGetRequest(getUrl(), getContext(), params);
    }

    public interface LoginTaskListener extends AbstractAsyncTaskListener<String> {
    }
}
