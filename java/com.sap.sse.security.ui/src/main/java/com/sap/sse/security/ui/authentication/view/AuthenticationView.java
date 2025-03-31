package com.sap.sse.security.ui.authentication.view;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sse.security.ui.authentication.AuthenticationCallback;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactory;
import com.sap.sse.security.ui.authentication.AuthenticationPlaceManagementController;
import com.sap.sse.security.ui.authentication.WrappedPlaceManagementController.PlaceManagementConfiguration;

/**
 * Interface for views working as content display within the wrapped authentication management.
 * 
 * @see AuthenticationPlaceManagementController#AuthenticationPlaceManagementController(AuthenticationClientFactory,
 *      AuthenticationCallback, AuthenticationView, EventBus)
 * @see PlaceManagementConfiguration#getDisplay()
 */
public interface AuthenticationView extends AcceptsOneWidget, IsWidget {
    
    /**
     * Set the given header text or hide header, if the no text is given. 
     * 
     * @param heading the header text to set, or <code>null</code> to hide the header
     */
    void setHeading(String heading);
    
}
