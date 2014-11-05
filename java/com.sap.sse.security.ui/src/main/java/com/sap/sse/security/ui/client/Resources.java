package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sse.security.ui.loginpanel.Css;

public interface Resources extends ClientBundle {

    public static final Resources INSTANCE =  GWT.create(Resources.class);

    @Source("css/LoginPanel.css")
    public Css css();

    @Source("com/sap/sse/security/ui/images/user.png")
    ImageResource user();
    
    @Source("com/sap/sse/security/ui/images/user_small.png")
    ImageResource userSmall();
    
    @Source("com/sap/sse/security/ui/images/user_icon.png")
    ImageResource userIcon();
}
