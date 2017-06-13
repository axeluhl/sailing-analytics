package com.sap.sailing.android.shared.util;

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

    public LoginTask(Context context, String baseUrl, LoginTaskListener listener) throws MalformedURLException {
        super(context, baseUrl, listener, new URL(baseUrl + TOKEN_REQUEST));
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
