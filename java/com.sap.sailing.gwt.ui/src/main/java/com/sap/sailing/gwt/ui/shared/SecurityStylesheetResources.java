package com.sap.sailing.gwt.ui.shared;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.sap.sse.security.ui.loginpanel.Css;

public interface SecurityStylesheetResources extends ClientBundle {
    public static final SecurityStylesheetResources INSTANCE = GWT.create(SecurityStylesheetResources.class);

    @Source("LoginPanel.css")
    public Css css();
}
