package com.sap.sailing.server.gateway.jaxrs.sharing;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

public class TokenizedHomePlaceUrl {
    private static final String EVENT_PATH = "/event";
    private static final String SERIES_PATH = "/series";
    private static final String REGATTA_OVERVIEW_PATH = "/regatta/overview";
    private static final String GWT_PREFIX = "/gwt";
    private static final String HOME_HTML = "/Home.html";
    private static final String STARTOFPARAMS = "/:";
    private static final String STARTOFTOKENPATH = "/#";
    private static final String EVENT_ID_PARAM = "eventId";
    private static final String REGATTA_ID_PARAM = "regattaId";
    private static final String LEADERBOARD_GROUP_ID_PARAM = "leaderboardGroupId";

    private URL baseUrl;
    private boolean tokenPresent;

    public TokenizedHomePlaceUrl(HttpServletRequest request) {
        try {
            baseUrl = new URL(request.getScheme(), request.getServerName(), request.getServerPort(),
                    GWT_PREFIX + HOME_HTML);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
    
    public String asEventPlaceLink(String eventId) {
        String url = new String(baseUrl.toString());
        url += STARTOFTOKENPATH + EVENT_PATH;
        url += addToken(eventId, EVENT_ID_PARAM);
        return url;
    }
    
    public String asSeriesPlaceLink(String eventId) {
        String url = new String(baseUrl.toString());
        url += STARTOFTOKENPATH + SERIES_PATH;
        url += addToken(eventId, REGATTA_ID_PARAM);
        return url;
    }

    public String asRegattaPlaceLink(String eventId) {
        String url = new String(baseUrl.toString());
        url += STARTOFTOKENPATH + REGATTA_OVERVIEW_PATH;
        url += addToken(eventId, LEADERBOARD_GROUP_ID_PARAM);
        return url;
    }
    
    private String addToken(String tokenId, String tokenKey) {
        String url = new String(baseUrl.toString());
        if(!tokenPresent) {
            url += STARTOFPARAMS;
            tokenPresent = true;
        }else {
            url += "&";
        }
        url += tokenKey + "=" + tokenId;
        return url;
    }
    
    public URL getBaseUrl() {
        return baseUrl;
    }
}
