package com.sap.sailing.gwt.home.desktop.app;

import com.sap.sailing.gwt.home.shared.usermanagement.view.AuthenticationMenuView;

public interface DesktopApplicationTopLevelView extends ApplicationTopLevelView<DesktopResettableNavigationPathDisplay> {
    AuthenticationMenuView getAuthenticationMenuView();
}
