package com.sap.sailing.gwt.ui.pwa.mobile.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sse.security.ui.authentication.generic.resource.AuthenticationResources;

public interface HeaderResources extends AuthenticationResources {
    public static final HeaderResources INSTANCE = GWT.create(HeaderResources.class);

    @Source("Header.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {

    }
}
