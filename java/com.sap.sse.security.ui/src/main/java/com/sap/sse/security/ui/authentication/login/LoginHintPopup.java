package com.sap.sse.security.ui.authentication.login;

import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.UserManagementResources;
import com.sap.sse.security.ui.authentication.UserManagementResources.LocalCss;

/**
 * Popup that appears when an unauthenticated user visits the page to inform about the benefits of logging in.
 */
public class LoginHintPopup extends PopupPanel {
    private static final LocalCss LOCAL_CSS = UserManagementResources.INSTANCE.css();
    protected final LoginHintContent content;
    private Runnable dismissCallback;

    public LoginHintPopup(AuthenticationManager authenticationManager, Runnable moreInfoCallback, Runnable toLogin) {
        LOCAL_CSS.ensureInjected();
        this.setStyleName(LOCAL_CSS.flyover());
        ensureDebugId("loginHintPopup");
        setAutoHideEnabled(true);
        content = new LoginHintContent(() -> {
            hide();
            dismissCallback.run();
        }, () -> {
            hide();
            dismissCallback.run();
            moreInfoCallback.run();
        }, () -> {
            if(toLogin != null){
                toLogin.run();
            }
        });
        content.addStyleName(LOCAL_CSS.flyover_content());

        final SimplePanel wrapper = new SimplePanel();
        wrapper.addStyleName(LOCAL_CSS.flyover_content_wrapper());
        wrapper.add(content);
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
        getElement().getStyle().clearTop();
    }
}
