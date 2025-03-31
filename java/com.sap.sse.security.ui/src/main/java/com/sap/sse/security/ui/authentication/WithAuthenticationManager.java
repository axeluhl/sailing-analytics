package com.sap.sse.security.ui.authentication;

/**
 * Interface which provides access to an {@link AuthenticationManager} instance.
 */
public interface WithAuthenticationManager {
    
    /**
     * @return the {@link AuthenticationManager}
     */
    AuthenticationManager getAuthenticationManager();
}
