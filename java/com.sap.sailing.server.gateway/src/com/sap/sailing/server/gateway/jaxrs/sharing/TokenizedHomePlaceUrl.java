package com.sap.sailing.server.gateway.jaxrs.sharing;

import java.net.URI;

import javax.ws.rs.core.UriInfo;

public class TokenizedHomePlaceUrl {
    private static final String GWT_PREFIX = "/gwt";
    private static final String HOME_HTML = "/Home.html";
    private static final String STARTOFPARAMS = "/:";
    private static final String STARTOFTOKENPATH = "/#";
    private static final String EVENT_ID_PARAM = "eventId";
    private static final String REGATTA_ID_PARAM = "regattaId";
    private static final String LEADERBOARD_GROUP_ID_PARAM = "leaderboardGroupId";

    private String baseUrl;
    private boolean tokenPresent;
    
    public TokenizedHomePlaceUrl(UriInfo uri) {
        final URI baseUri = uri.getBaseUri();
        this.baseUrl = baseUri + GWT_PREFIX + HOME_HTML;
    }
    
    public String asEventUrl(String eventId) {
        String url = new String(baseUrl);
        url += STARTOFTOKENPATH + "/event";
        url += addToken(eventId, EVENT_ID_PARAM);
        return url;
    }
    
    public String asSeriesUrl(String eventId) {
        String url = new String(baseUrl);
        url += STARTOFTOKENPATH + "/series";
        url += addToken(eventId, REGATTA_ID_PARAM);
        return url;
    }

    public String asRegattaUrl(String eventId) {
        String url = new String(baseUrl);
        url += STARTOFTOKENPATH + "/regatta/overview";
        url += addToken(eventId, LEADERBOARD_GROUP_ID_PARAM);
        return url;
    }
    
    private String addToken(String tokenId, String tokenKey) {
        String url = new String(baseUrl);
        if(!tokenPresent) {
            url += STARTOFPARAMS;
            tokenPresent = true;
        }else {
            url += "&";
        }
        url += tokenKey + "=" + tokenId;
        return url;
    }
    
    public String getBaseUrl() {
        return baseUrl;
    }
}
