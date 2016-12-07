package com.sap.sse.security.ui.authentication;

/**
 * Callback interface providing methods to be able to configure the handling of navigations, which will leave the
 * wrapped authentication management, application specifically.
 */
public interface AuthenticationCallback {

    /**
     * Called after a user signs in successfully. Implementations maybe want to:
     * <ul>
     * <li>Hide the sign in dialog box</li>
     * <li>Show logged in user information</li>
     * <li>Navigate the application's welcome page</li>
     * <li>Navigate the user's profile page</li>
     * </ul> 
     */
    void handleSignInSuccess();

    /**
     * Called, if the user clicks the "User Profile" navigation control.
     */
    void handleUserProfileNavigation();
}