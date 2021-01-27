package com.sap.sailing.gwt.managementconsole.partials.authentication;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

public interface AuthenticationResources extends ClientBundle {

    AuthenticationResources INSTANCE = GWT.create(AuthenticationResources.class);

    @Source({ ManagementConsoleResources.COLORS, "Authentication.gss" })
    Style style();

    @Source("../../resources/sap-logo.png")
    ImageResource sapLogo();

    public interface Style extends CssResource {

        String signIn();

        String content();

        String logo();

        String link();
    }
}
