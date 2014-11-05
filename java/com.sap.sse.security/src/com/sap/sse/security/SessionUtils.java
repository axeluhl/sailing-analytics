package com.sap.sse.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.session.Session;
import org.scribe.exceptions.OAuthException;
import org.scribe.model.Token;

public class SessionUtils {

    public static final String SESSION_ID = "GWTOAuthLoginDemo_sessionid";
    public static final String SESSION_REQUEST_TOKEN = "GWTOAuthLoginDemo_request_token";
    public static final String SESSION_NONCE = "GWTOAuthLoginDemo_nonce";
    public static final String SESSION_PROTECTED_URL = "GWTOAuthLoginDemo_protected_url";
    public static final String SESSION_ACCESS_TOKEN = "GWTOAuthLoginDemo_access_token";
    public static final String SESSION_YAHOO_GUID = "GWTOAuthLoginDemo_yahoo_guid";
    public static final String SESSION_AUTH_PROVIDER = "GWTOAuthLoginDemo_auth_provider";
    public static final String SOCIAL_USER = "Social_User";
    
    public static void saveRequestTokenToSession(Token requestToken) throws Exception {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new Exception(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        /*
         * ServersideSession sss = getServersideSession(); if (sss == null) { sss = new ServersideSession();
         * sss.setRequestToken(requestToken); session.setAttribute(SESSION,sss); } else {
         * sss.setRequestToken(requestToken); }
         */
        session.setAttribute(SESSION_REQUEST_TOKEN, requestToken);
    }

    public static void saveStateToSession(String state) throws Exception {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new Exception(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        /*
         * ServersideSession sss = getServersideSession(); if (sss == null) { sss = new ServersideSession();
         * sss.setState(state); session.setAttribute(SESSION,sss); } else { sss.setState(state); }
         */
        session.setAttribute(SESSION_NONCE, state);
    }

    public static String getProtectedResourceUrlFromSession() throws OAuthException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new OAuthException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        /*
         * ServersideSession sss = getServersideSession(); if (sss != null) return sss.getProtectedResourceUrl();
         * 
         * return null;
         */
        return (String) session.getAttribute(SESSION_PROTECTED_URL);
    }

    public static String getStateFromSession() throws Exception {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new Exception(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        /*
         * ServersideSession sss = getServersideSession(); if (sss != null) return sss.getState();
         * 
         * return null;
         */
        return (String) session.getAttribute(SESSION_NONCE);
    }

    public static String getSessionIdFromSession() throws OAuthException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new OAuthException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        /*
         * ServersideSession sss = getServersideSession(); if (sss != null) return sss.getSessionId();
         */
        String sessionId = (String) session.getAttribute(SESSION_ID);

        return sessionId;
    }
    
    public static Token getAccessTokenFromSession() throws OAuthException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new OAuthException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        /*
         * ServersideSession sss = getServersideSession(); if (sss != null) { return sss.getAccessToken(); } return
         * null;
         */
        return (Token) session.getAttribute(SESSION_ACCESS_TOKEN);

    }

    public static int getAuthProviderFromSession() throws OAuthException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new OAuthException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        /*
         * ServersideSession sss = getServersideSession(); if (sss != null) return sss.getAuthProvider();
         */
        return (Integer) session.getAttribute(SESSION_AUTH_PROVIDER);
    }
    
    public static void saveUsername(String username) {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            return;
        }
        session.setAttribute(SOCIAL_USER, username);
    }

    public static String loadUsername() {
        final String result;
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            result = null;
        } else {
            result = (String) session.getAttribute(SOCIAL_USER);
        }
        return result;
    }
    
    public static void saveSessionIdToSession(String sessionId) throws AuthenticationException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new AuthenticationException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        session.setAttribute(SESSION_ID, sessionId);
    }
    
    public static void saveProtectedResourceUrlToSession(String url) throws AuthenticationException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new AuthenticationException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        session.setAttribute(SESSION_PROTECTED_URL, url);
    }
    
    public static void saveAuthProviderToSession(int authProvider) throws AuthenticationException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new AuthenticationException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        session.setAttribute(SESSION_AUTH_PROVIDER, authProvider);
    }
    
    public static void saveAccessTokenToSession(Token accessToken) throws AuthenticationException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new AuthenticationException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        session.setAttribute(SESSION_ACCESS_TOKEN, accessToken);
    }

    public static void saveYahooGuidToSession(String guid) throws AuthenticationException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new AuthenticationException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        session.setAttribute(SESSION_YAHOO_GUID, guid);
    }
    
    public static String getYahooGuidFromSession() throws AuthenticationException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new AuthenticationException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        return (String) session.getAttribute(SESSION_YAHOO_GUID);
    }

    public static Token getRequestTokenFromSession() throws AuthenticationException {
        Session session = SecurityUtils.getSubject().getSession();
        if (session == null) {
            throw new AuthenticationException(ClientUtils.SESSION_EXPIRED_MESSAGE);
        }
        return (Token) session.getAttribute(SESSION_REQUEST_TOKEN);
    }
}
