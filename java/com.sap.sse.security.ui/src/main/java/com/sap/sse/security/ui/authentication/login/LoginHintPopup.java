package com.sap.sse.security.ui.authentication.login;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;

public class LoginHintPopup extends PopupPanel {
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css();
    private LoginHintContent lpop;
    private Runnable dismissCallback;

    public LoginHintPopup(AuthenticationManager authenticationManager, Runnable moreInfoCallback) {
        LOCAL_CSS.ensureInjected();
        this.setStyleName(LOCAL_CSS.flyover());
        ensureDebugId("loginHintPopup");
        setAutoHideEnabled(true);
        lpop = new LoginHintContent(() -> {
            hide();
            dismissCallback.run();
        }, () -> {
            hide();
            dismissCallback.run();
            moreInfoCallback.run();
        });
        lpop.addStyleName(LOCAL_CSS.flyover_content());

        final SimplePanel wrapper = new SimplePanel();
        wrapper.addStyleName(LOCAL_CSS.flyover_content_wrapper());
        wrapper.add(lpop);
        add(wrapper);
        
        authenticationManager.checkNewUserPopup(this::hide, dismissCallback -> {
            this.dismissCallback = dismissCallback;
            show();
        });
    }
    
    @Override
    public void show() {
        super.show();
        getElement().getStyle().clearLeft();
    }
}
