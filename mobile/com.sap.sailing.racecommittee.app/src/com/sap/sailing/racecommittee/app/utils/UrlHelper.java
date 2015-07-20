package com.sap.sailing.racecommittee.app.utils;

import android.net.Uri;

import java.net.MalformedURLException;
import java.net.URL;

public class UrlHelper {
    public static URL generateUrl(String baseURL, String path, Object[] params, Object[] values) throws MalformedURLException {
        Uri.Builder builder = Uri.parse(baseURL).buildUpon();
        builder.path(path);
        for (int index = 0; index < params.length; index++){
            builder.appendQueryParameter("" + params[index], "" + values[index]);
        }
        return new URL(builder.build().toString());
    }
}
