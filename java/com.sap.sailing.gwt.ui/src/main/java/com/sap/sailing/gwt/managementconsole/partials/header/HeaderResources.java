package com.sap.sailing.gwt.managementconsole.partials.header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sse.security.ui.authentication.generic.resource.AuthenticationResources;

public interface HeaderResources extends AuthenticationResources {

    HeaderResources INSTANCE = GWT.create(HeaderResources.class);

    @Source("Header.gss")
    Style style();

    @Source("sap-logo.png")
    ImageResource sapLogo();

    public interface Style extends CssResource {
        String header();

        String logo();

        String menu();

        String account();

        String navigation();
    }
}
