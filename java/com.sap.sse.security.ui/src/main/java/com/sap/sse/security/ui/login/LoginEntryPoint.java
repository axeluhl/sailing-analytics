package com.sap.sse.security.ui.login;

import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sse.security.ui.client.AbstractSecurityEntryPoint;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class LoginEntryPoint extends AbstractSecurityEntryPoint {
    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        rootPanel.add(new LoginView(getUserManagementService(), getUserService(), StringMessages.INSTANCE,
                getApplicationName(StringMessages.INSTANCE.signIn())));
    }
}
