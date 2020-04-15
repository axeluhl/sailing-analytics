package com.sap.sailing.android.shared.data.http;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sap.sailing.android.shared.data.LoginData;

import android.content.Context;

public class LoginGetRequest extends HttpRequest {

    public final static String ContentType = "application/json;charset=UTF-8";
    private final LoginData loginData;

    public LoginGetRequest(URL url, Context context, LoginData login) {
        super(context, url);
        loginData = login;
    }

    @Override
    protected BufferedInputStream doRequest(HttpURLConnection connection) throws IOException {
        connection.setRequestMethod("GET");
        connection.setChunkedStreamingMode(0);

        connection.setRequestProperty("Content-Type", ContentType);
        connection.setRequestProperty("Accept", ContentType);
        connection.setRequestProperty("Authorization", "Basic " + loginData.getCredentials());

        return new BufferedInputStream(connection.getInputStream());
    }
}
