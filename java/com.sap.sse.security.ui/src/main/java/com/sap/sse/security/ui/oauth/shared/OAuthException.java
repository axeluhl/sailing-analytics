package com.sap.sse.security.ui.oauth.shared;

public class OAuthException extends Exception {
    
    /**
     * 
     */
    private static final long serialVersionUID = 7014475448354180158L;

    public OAuthException() {
        super("OAuthException!");
    }

    public OAuthException(String message) {
        super("OAuthException: "+ message);
        // TODO Auto-generated constructor stub
    }
    
    

}
