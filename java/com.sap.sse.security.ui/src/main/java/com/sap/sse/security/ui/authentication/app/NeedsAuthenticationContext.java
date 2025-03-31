package com.sap.sse.security.ui.authentication.app;

/**
 * Common interface for all classes that need to be informed whenever the authentication changes.
 */
public interface NeedsAuthenticationContext {

    /**
     * To be called when the authentication changes.
     * 
     * @param authenticationContext the new {@link AuthenticationContext}.
     */
    void setAuthenticationContext(AuthenticationContext authenticationContext);
}
