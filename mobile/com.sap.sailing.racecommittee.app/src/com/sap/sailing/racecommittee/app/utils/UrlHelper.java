package com.sap.sailing.racecommittee.app.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import android.net.Uri;

import com.sap.sse.common.Util;

public class UrlHelper {
    public static URL generateUrl(String baseURL, String path, List<Util.Pair<String, Object>> params) throws MalformedURLException {
        Uri.Builder builder = Uri.parse(baseURL).buildUpon();
        builder.path(path);
        if (params != null) {
            for (Util.Pair<String, Object> param : params) {
                builder.appendQueryParameter("" + param.getA(), "" + param.getB());
            }
        }
        return new URL(builder.build().toString());
    }
}
