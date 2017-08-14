package com.sap.sse.security.ui.authentication.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.PopupPanel;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;

public class LoginPopup extends PopupPanel {
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css();
    private static final LoginPopupViewResources.CSS CSS = LoginPopupViewResources.INSTANCE.css();
    private LoginPopupContent lpop;

    public interface LoginPopupViewResources extends ClientBundle {
        public static final LoginPopupViewResources INSTANCE = GWT.create(LoginPopupViewResources.class);

        @Source("LoginPopup.gss")
        CSS css();

        public interface CSS extends CssResource {
            String mainLayout();

            String mobileLayout();
        }
    }

    public LoginPopup(boolean desktop, final Runnable onDismiss, final Runnable onMoreInfo) {
        LOCAL_CSS.ensureInjected();
        CSS.ensureInjected();
        this.addStyleName(LOCAL_CSS.flyover());
        if (desktop) {
            this.addStyleName(CSS.mainLayout());
        } else {
            this.addStyleName(CSS.mobileLayout());
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
