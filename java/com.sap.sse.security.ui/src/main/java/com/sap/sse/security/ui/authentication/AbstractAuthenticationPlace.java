package com.sap.sse.security.ui.authentication;

import com.google.gwt.place.shared.Place;
import com.sap.sse.security.ui.authentication.view.AuthenticationView;

/**
 * Super class for {@link Place}s within the authentication management, defining a header text for each {@link Place}.
 * 
 * @see AuthenticationView#setHeading(String)
 */
public abstract class AbstractAuthenticationPlace extends Place {
    
    /**
     * Defines a header text for the {@link Place}
     * 
     * @return the header test
     */
    public abstract String getHeaderText();
    
}
