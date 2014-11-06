package com.sap.sse.security.ui.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.sap.sse.security.ui.loginpanel.Css;

public interface Resources extends ClientBundle {

    public static final Resources INSTANCE =  GWT.create(Resources.class);

    @Source("css/LoginPanel.css")
    public Css css();
}
