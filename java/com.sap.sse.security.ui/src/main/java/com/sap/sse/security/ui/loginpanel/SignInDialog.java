package com.sap.sse.security.ui.loginpanel;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sse.security.ui.client.StringMessages;
import com.sap.sse.security.ui.client.component.AbstractUserDialog;
import com.sap.sse.security.ui.client.shared.oauthlogin.OAuthLogin;
import com.sap.sse.security.ui.shared.UserManagementServiceAsync;

public class SignInDialog extends AbstractUserDialog {
    private final Css css;

    public SignInDialog(StringMessages stringMessages, UserManagementServiceAsync userManagementService, Css css, DialogCallback<UserData> callback) {
        super(stringMessages, stringMessages.signIn(), userManagementService, /* user */ null, /* validator */ null, callback);
        this.css = css;
    }

    @Override
    protected Widget getAdditionalWidget() {
        Grid result = new Grid(3, 2);
        result.setWidget(0, 0, new Label(getStringMessages().username()));
        result.setWidget(0, 1, getNameBox());
        result.setWidget(1, 0, new Label(getStringMessages().password()));
        result.setWidget(1, 1, getPwBox());
        result.setWidget(2, 0, new OAuthLogin(getUserManagementService()));
        return result;
    }
}
