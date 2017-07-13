package com.sap.sailing.android.shared.util;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.simple.JSONObject;

import com.sap.sailing.android.shared.data.http.HttpJsonGetRequest;
import com.sap.sailing.android.shared.data.http.HttpRequest;
import com.sap.sailing.android.shared.util.AuthCheckTask.AuthCheckTaskListener;

import android.content.Context;

public class AuthCheckTask extends AbstractAsyncJsonTask<Void, Void, Boolean, AuthCheckTaskListener> {
    private final static String HELLO_REQUEST = "/security/api/restsecurity/hello";

    public AuthCheckTask(Context context, String baseUrl, AuthCheckTaskListener listener) throws MalformedURLException {
        super(context, listener, new URL(baseUrl + HELLO_REQUEST));
    }

    @Override
    protected HttpRequest createRequest(Void params) {
        return new HttpJsonGetRequest(getContext(), getUrl());
    }

    @Override
    protected Boolean getResult(JSONObject json) {
        Boolean authenticated = false;
        if (json.containsKey("authenticated")) {
            authenticated = (Boolean) json.get("authenticated");
        }
        return authenticated;
    }

    public interface AuthCheckTaskListener extends AbstractAsyncTaskListener<Boolean> {
    }
}
