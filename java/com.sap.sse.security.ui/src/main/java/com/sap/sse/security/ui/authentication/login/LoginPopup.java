package com.sap.sse.security.ui.authentication.login;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;

public class LoginPopup extends PopupPanel {
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css();
    private LoginPopupContent lpop;

    public LoginPopup(final Runnable onDismiss, final Runnable onMoreInfo) {
        LOCAL_CSS.ensureInjected();
        this.addStyleName(LOCAL_CSS.flyover());
        this.addStyleName(LOCAL_CSS.flyover_small_hidden());
        this.addStyleName(LOCAL_CSS.flyover_position_grid());
        ensureDebugId("loginpopupNewUser");
        setAutoHideEnabled(true);
        lpop = new LoginPopupContent(() -> {
            hide();
            onDismiss.run();
        }, () -> {
            hide();
            onMoreInfo.run();
        });
        lpop.addStyleName(LOCAL_CSS.flyover_content());

        final SimplePanel wrapper = new SimplePanel();
        wrapper.addStyleName(LOCAL_CSS.flyover_content_wrapper());
        wrapper.add(lpop);
        add(wrapper);
    }
    
    @Override
    public void show() {
        super.show();
        getElement().getStyle().clearLeft();
    }
}
