package com.sap.sse.security.ui.authentication.generic;

/**
 * Interface for a factory which creates links used within authentication management. 
 */
public interface GenericAuthenticationLinkFactory {

    String createUserProfileLink();

    String createEmailValidationLink();

    String createPasswordResetLink();

    String createMoreInfoAboutLoginLink();
}
