package com.sap.sailing.gwt.managementconsole.partials.mainframe;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.CssResource;
import com.sap.sse.security.ui.authentication.generic.resource.AuthenticationResources;

public interface MainFrameResources extends AuthenticationResources {

    MainFrameResources INSTANCE = GWT.create(MainFrameResources.class);

    @Source("MainFrame.gss")
    Style style();

    public interface Style extends CssResource {

        String header();

        String content();
    }
}
