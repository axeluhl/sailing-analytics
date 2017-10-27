package com.sap.sailing.gwt.home.desktop.app;

import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;
import com.sap.sse.security.ui.authentication.login.LoginHintPopup;

/**
 * Specific version of {@link LoginHintPopup} that meets the positioning of the page header in Home.html.
 */
public class DesktopLoginHintPopup extends LoginHintPopup {
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css();

    public DesktopLoginHintPopup(AuthenticationManager authenticationManager, DesktopPlacesNavigator placesNavigator) {
        super(authenticationManager, () -> placesNavigator.goToPlace(placesNavigator.getMoreLoginInfo()), null);
        this.addStyleName(LOCAL_CSS.flyover_small_hidden());
        this.addStyleName(LOCAL_CSS.flyover_position_grid());
    }
}
