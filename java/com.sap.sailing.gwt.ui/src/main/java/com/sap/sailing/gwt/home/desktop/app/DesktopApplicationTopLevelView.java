package com.sap.sailing.gwt.home.desktop.app;

import com.sap.sse.security.ui.authentication.view.AuthenticationMenuView;

public interface DesktopApplicationTopLevelView extends ApplicationTopLevelView<DesktopResettableNavigationPathDisplay> {
    AuthenticationMenuView getAuthenticationMenuView();
}
