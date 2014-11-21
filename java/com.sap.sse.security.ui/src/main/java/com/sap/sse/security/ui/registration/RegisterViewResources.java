package com.sap.sse.security.ui.registration;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface RegisterViewResources extends ClientBundle {
    public static final RegisterViewResources INSTANCE = GWT.create(RegisterViewResources.class);

    @Source("com/sap/sse/security/ui/registration/RegisterViewResources.css")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String registrationform();
        String registrationform_header();
        String registrationform_header_appname();
        String registrationform_content();
        String textInput_watermark();
        String passwordTextInput_watermark();
        String glowing_border();
        String button();
        String buttoninactive();
        String errormessage();
    }
}
