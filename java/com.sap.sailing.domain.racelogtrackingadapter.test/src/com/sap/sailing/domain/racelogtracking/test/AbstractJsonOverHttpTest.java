package com.sap.sailing.domain.racelogtracking.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

import com.sap.sse.util.HttpUrlConnectionHelper;

public abstract class AbstractJsonOverHttpTest {
    protected static final String URL_BASE = "127.0.0.1";
    protected static final int PORT = 8888;
    protected static final String URL_SS = "/sailingserver";
    protected static final String URL_TR = "/tracking";
    protected static final String URL_RF = "/recordFixes";

    protected String getUrl(String endOfUrl) {
        return "http://" + URL_BASE + ":" + PORT + endOfUrl;
    }

    protected String executeRequest(String method, String targetURL, String body) throws IOException {
        // Create connection
        URL url = new URL(targetURL);
        System.out.println(url);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Content-Type", "application/json");

        connection.setUseCaches(false);
        connection.setDoInput(true);

        // Send request
        if (method.equals("POST")) {
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(body);
        }

        InputStream is = null;
        boolean error = false;
        if (connection.getResponseCode() >= 400) {
            is = connection.getErrorStream();
            error = true;
        } else {
            is = connection.getInputStream();
        }

        if (is == null) {
            return "";
        }
        final Charset cs = HttpUrlConnectionHelper.getCharsetFromConnectionOrDefault(connection, "UTF-8");
        BufferedReader rd = new BufferedReader(new InputStreamReader(is, cs));
        String line;
        StringBuffer response = new StringBuffer();
        while ((line = rd.readLine()) != null) {
            response.append(line);
            response.append('\r');
        }
        rd.close();
        if (error) {
            System.err.println(response.toString());
            return "";
        }

        return response.toString();
    }

    protected Object getJson(String url) throws IllegalStateException, IOException, ParseException {
        return JSONValue.parseWithException(executeRequest("GET", url, ""));
    }

}
