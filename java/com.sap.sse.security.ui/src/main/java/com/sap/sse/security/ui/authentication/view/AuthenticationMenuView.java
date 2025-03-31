package com.sap.sse.security.ui.authentication.view;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Interface for menu items, which interacting with a {@link FlyoutAuthenticationView}.
 */
public interface AuthenticationMenuView extends IsWidget {

    /**
     * Sets the {@link Presenter}.
     * 
     * @param presenter
     *            the {@link Presenter} to set
     */
    void setPresenter(Presenter presenter);

    /**
     * Sets whether or not there is an authenticated user.
     * 
     * @param authenticated
     *            <code>true</code> if there is an authenticated user, <code>false</code> otherwise
     */
    void setAuthenticated(boolean authenticated);

    /**
     * Sets whether or not the {@link FlyoutAuthenticationView} is open/shown.
     * 
     * @param open
     *            <code>true</code> the {@link FlyoutAuthenticationView} is open, <code>false</code> otherwise
     */
    void setOpen(boolean open);

    /**
     * Shows the premium indicator below the user icon.
     * 
     * @param premium
     *            if premium is visible (active premium role) or not.
     */
    void showPremium(boolean premium);

    /**
     * Presenter interface to toggle {@link FlyoutAuthenticationView}'s visibility.
     */
    public interface Presenter {
        /**
         * Called to toggle visibility.
         */
        void toggleFlyout();
    }

}
