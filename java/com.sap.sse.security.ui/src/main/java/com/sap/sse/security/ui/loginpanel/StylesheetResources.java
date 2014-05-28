package com.sap.sse.security.ui.loginpanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;

public interface StylesheetResources extends ClientBundle {

    public static final StylesheetResources INSTANCE =  GWT.create(StylesheetResources.class);

    @Source("css/LoginPanel.css")
    public Css css();
}
