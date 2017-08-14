package com.sap.sse.security.ui.authentication.login;

import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;

public class LoginPopup extends PopupPanel {
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css();
    private LoginPopupContent lpop;

    public LoginPopup(boolean desktop, final Runnable onDismiss, final Runnable onMoreInfo) {
        LOCAL_CSS.ensureInjected();
        this.addStyleName(LOCAL_CSS.flyover());
        if (desktop) {
            this.addStyleName(LOCAL_CSS.flyover_small_hidden());
            this.addStyleName(LOCAL_CSS.flyover_position_grid());
        } else {
            this.addStyleName(LOCAL_CSS.flyover_mobile());
        }
        ensureDebugId("loginpopupNewUser");
        setAutoHideEnabled(true);
        lpop = new LoginPopupContent(() -> {
            hide();
            onDismiss.run();
        }, () -> {
            hide();
            onMoreInfo.run();
        });

        add(lpop);
    }
}
