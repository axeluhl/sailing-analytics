package com.sap.sse.security.ui.loginpanel;

import com.google.gwt.resources.client.CssResource;

public interface LoginPanelCss extends CssResource {
    String loginPanel();
    
    @ClassName("loginPanel-welcomeMessage")
    String welcomeMessage();

    @ClassName("loginPanel-link")
    String link();

    @ClassName("loginPanel-userIcon")
    String userIcon();

    @ClassName("loginPanel-providerIcon")
    String providerIcon();
}
