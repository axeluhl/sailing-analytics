package com.sap.sse.security.ui.authentication;


public interface AuthenticationCallback {
    
    void handleSignInSuccess();
    
    void handleUserProfileNavigation();
}