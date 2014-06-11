package com.sap.sse.security.userstore.shared;

import java.util.HashMap;
import java.util.Map;


/**
 * The object we send back and forth between client and server.
 * Note: More information is stored in server side in ServersideSession
 * object.
 * @author muquit@muquit.com
 */
public class SocialUserAccount implements Account
{
    private String sessionId;
    
    private Map<String, String> properties = new HashMap<String, String>();
    
    
    
    public String getSessionId()
    {
        return sessionId;
    }
    public void setSessionId(String sessionId)
    {
        this.sessionId=sessionId;
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
    
    public String getProperty(String key){
        return properties.get(key);
    }
    
    public void setProperty(String key, String value){
        properties.put(key, value);
    }
}

