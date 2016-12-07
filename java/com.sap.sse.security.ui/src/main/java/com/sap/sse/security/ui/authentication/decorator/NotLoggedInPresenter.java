package com.sap.sse.security.ui.authentication.decorator;

/**
 * Used by {@link AuthorizedContentDecorator} to trigger login when the button is pressed on the {@link NotLoggedInView}.
 *
 */
public interface NotLoggedInPresenter {
    /**
     * When called this must show the login form to the user.
     */
    void doTriggerLoginForm();
}
