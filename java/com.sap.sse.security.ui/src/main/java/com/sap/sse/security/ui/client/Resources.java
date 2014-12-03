package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.sap.sse.security.ui.loginpanel.LoginPanelCss;

public interface Resources extends ClientBundle {

    public static final Resources INSTANCE =  GWT.create(Resources.class);

    @Source("com/sap/sse/security/ui/loginpanel/LoginPanel.css")
    LoginPanelCss loginPanelCss();
}
