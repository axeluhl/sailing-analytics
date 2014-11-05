package com.sap.sse.security.ui.loginpanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AbstractUserDialog;
import com.sap.sse.security.ui.client.component.ForgotPasswordDialog;
import com.sap.sse.security.ui.client.i18n.StringMessages;
import com.sap.sse.security.ui.client.shared.oauthlogin.OAuthLogin;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class SignInDialog extends AbstractUserDialog {
    private final UserService userService;
    
    public SignInDialog(StringMessages stringMessages, UserManagementServiceAsync userManagementService, UserService userService, DialogCallback<UserData> callback) {
        super(stringMessages, stringMessages.signIn(), stringMessages.signIn(), userManagementService, /* user */ null, /* validator */ null, callback);
        this.userService = userService;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(4, 2);
        result.setWidget(0, 0, new Label(getStringMessages().username()));
        result.setWidget(0, 1, getNameBox());
        result.setWidget(1, 0, new Label(getStringMessages().password()));
        result.setWidget(1, 1, getPwBox());
        result.setWidget(2, 0, new OAuthLogin(getUserManagementService()));
        Anchor forgotPassword = new Anchor(getStringMessages().forgotPassword());
        result.setWidget(3, 0, forgotPassword);
        forgotPassword.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                new ForgotPasswordDialog(StringMessages.INSTANCE, getUserManagementService(), userService.getCurrentUser()).show();
            }
        });
        return result;
    }
}
