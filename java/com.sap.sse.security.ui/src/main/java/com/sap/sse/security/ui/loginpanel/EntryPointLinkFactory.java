package com.sap.sse.security.ui.loginpanel;

import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.user.client.Window;

public class EntryPointLinkFactory {
    
    public static String createRegistrationLink(Map<String, String> parameters) {
        return createEntryPointLink("/security/ui/Register.html", parameters);
    }
    
    public static String createLoginLink(Map<String, String> parameters) {
        return createEntryPointLink("/security/ui/Login.html", parameters);
    }

    private static String createEntryPointLink(String baseLink, Map<String, String> parameters) {
        String debugParam = Window.Location.getParameter("gwt.codesvr");
        String localeParam = Window.Location.getParameter("locale");
        String link = baseLink;
        int i = 1;
        for(Entry<String, String> entry: parameters.entrySet()) {
            link += i == 1 ? "?" : "&";
            link += entry.getKey() + "=" + entry.getValue();
            i++;
        }
        if (debugParam != null && !debugParam.isEmpty()) {
            link += i == 1 ? "?" : "&";
            link += "gwt.codesvr=" + debugParam;
        }
        if (localeParam != null && !localeParam.isEmpty()) {
            link += i == 1 ? "?" : "&";
            link += "locale=" + localeParam;
        }
        return URLEncoder.encode(link);
    }
}
