package com.sap.sse.security.ui.oauth.client;

import java.util.HashMap;
import java.util.Map;

import com.sap.sse.security.shared.dto.AccountDTO;

/**
 * The object we send back and forth between client and server.
 * Note: More information is stored in server side in ServersideSession
 * object.
 * @author muquit@muquit.com
 */
public class SocialUserDTO extends AccountDTO {
    private static final long serialVersionUID = 1L;

    private String sessionId;
    
    private Map<String, String> properties = new HashMap<String, String>();
    
    public SocialUserDTO() {
        super("Unkown Social Provider");
    }
    
    public SocialUserDTO(String provider) {
        super(provider);
    }
    
    public String getSessionId()
    {
        return sessionId;
    }
    public void setSessionId(String sessionId)
    {
        this.sessionId=sessionId;
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

