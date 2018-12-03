package com.sap.sse.security.ui.authentication.app;

import com.sap.sse.security.shared.WildcardPermission;
import com.sap.sse.security.shared.dto.UserDTO;

/**
 * Interface for authentication context representations providing access to the current {@link UserDTO user} object and
 * some convenience methods.
 */
public interface AuthenticationContext {
    
    /**
     * Determines if there is a logged in user.
     * 
     * @return <code>true</code> if a user is logged in, <code>false</code> otherwise
     */
    boolean isLoggedIn();

    /**
     * Provides access to the logged in {@link UserDTO user}, returning an object representing an anonymous, if there is
     * no logged in user.
     * 
     * @return a {@link UserDTO user} instance representing the logged in user or an anonymous, if no user is logged in
     */
    UserDTO getCurrentUser();
    
    /**
     * Provides the title text for displaying user information. Usually, this would be the full name or username.
     * 
     * @return the user information title text
     */
    String getUserTitle();
    
    /**
     * Provides the subtitle text for displaying user information. Usually, this would be the company name or email
     * address.
     * 
     * @return the user information subtitle text
     */
    String getUserSubtitle();
    
    boolean hasPermission(WildcardPermission permission);
}
