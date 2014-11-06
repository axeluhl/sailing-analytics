package com.sap.sse.security.ui.login;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface LoginViewResources extends ClientBundle {
    public static final LoginViewResources INSTANCE = GWT.create(LoginViewResources.class);

    @Source("com/sap/sse/security/ui/login/LoginViewResources.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String loginform();
        String loginform_header();
        String loginform_header_appname();
        String loginform_content();
        String textInput_watermark();
        String passwordTextInput_watermark();
        String glowing_border();
        String button();
    }
}
