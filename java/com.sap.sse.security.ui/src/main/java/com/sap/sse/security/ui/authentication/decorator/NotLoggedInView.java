package com.sap.sse.security.ui.authentication.decorator;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * A view that is shown by {@link AuthorizedContentDecorator} if the user isn't logged in or doen't have the required
 * permissions.
 * The view must show a message and a button that leads the user to the login form when being clicked.
 */
public interface NotLoggedInView extends IsWidget {

    /**
     * @param presenter the presenter to call when the "sign in" button is clicked
     */
    void setPresenter(NotLoggedInPresenter presenter);

    /**
     * @param message the message to show to the user
     */
    void setMessage(String message);

    /**
     * @param signInText the text to set on the "sign in" button
     */
    void setSignInText(String signInText);
}
