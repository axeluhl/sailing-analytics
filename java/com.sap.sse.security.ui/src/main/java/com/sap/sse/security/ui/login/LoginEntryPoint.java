package com.sap.sse.security.ui.login;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sse.security.ui.client.AbstractSecurityEntryPoint;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class LoginEntryPoint extends AbstractSecurityEntryPoint {
    /**
     * The URL parameter name used to pass an application name to this entry point. This name
     * will be put into the login view's header.
     */
    private static final String PARAM_APP = "app";
    
    @Override
    public void doOnModuleLoad() {
        super.doOnModuleLoad();
        final String appName = Window.Location.getParameter(PARAM_APP);
        RootPanel rootPanel = RootPanel.get();
        rootPanel.add(new LoginView(getUserManagementService(), getUserService(), appName==null?StringMessages.INSTANCE.signIn():appName));
    }
}
