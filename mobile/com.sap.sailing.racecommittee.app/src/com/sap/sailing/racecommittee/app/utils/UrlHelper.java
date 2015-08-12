package com.sap.sailing.racecommittee.app.utils;

import android.net.Uri;
import com.sap.sse.common.Util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class UrlHelper {
    public static URL generateUrl(String baseURL, String path, List<Util.Pair> params) throws MalformedURLException {
        Uri.Builder builder = Uri.parse(baseURL).buildUpon();
        builder.path(path);
        for (Util.Pair param : params){
            builder.appendQueryParameter("" + param.getA(), "" + param.getB());
        }
        return new URL(builder.build().toString());
    }
}
