package com.sap.sse.security.ui.authentication.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;

/**
 * Extended {@link AuthenticationView} interface for flyout based view implementations.
 */
public interface FlyoutAuthenticationView extends AuthenticationView {
    
    /**
     * Sets the {@link Presenter}
     * 
     * @param presenter
     *            the {@link Presenter} to set
     */
    void setPresenter(Presenter presenter);
    
    /**
     * Determine if the flyout is shown
     * 
     * @return <code>true</code> if the flyout is shown, <code>false</code> otherwise
     */
    boolean isShowing();
    
    /**
     * Show the {@link FlyoutAuthenticationView}.
     */
    void show();
    
    /**
     * Hide the {@link FlyoutAuthenticationView}.
     */
    void hide();
    
    /**
     * Set the {@link FlyoutAuthenticationView}'s auto hide partner.
     * 
     * @param autoHidePartner
     *            the auto hide partner to set
     *            
     * @see PopupPanel#addAutoHidePartner(Element)
     */
    void setAutoHidePartner(IsWidget autoHidePartner);
    
    /**
     * Presenter interface to handle visibility change
     * 
     * @see FlyoutAuthenticationView#setPresenter(Presenter)
     */
    public interface Presenter {
        /**
         * Call if the {@link FlyoutAuthenticationView}'s visibility changes 
         * 
         * @param isShowing <code>true</code> if the flyout is shown, <code>false</code> otherwise
         */
        void onVisibilityChanged(boolean isShowing);
    }

}
