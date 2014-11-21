package com.sap.sse.security.shared;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.security.SecurityService;

/**
 * The object we send back and forth between client and server. See also
 * {@link SecurityService#createSocialUser(String, SocialUserAccount)}.
 */
public class SocialUserAccount implements Account {
    private static final long serialVersionUID = -1702877798960390855L;

    private String sessionId;

    private Map<String, String> properties = new HashMap<String, String>();

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @Override
    public AccountType getAccountType() {
        return AccountType.SOCIAL_USER;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
}
