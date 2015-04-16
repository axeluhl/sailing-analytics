package com.sap.sailing.gwt.regattaoverview.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.CssResource.NotStrict;

public interface RegattaOverviewResources extends ClientBundle {
    public static final RegattaOverviewResources INSTANCE = GWT.create(RegattaOverviewResources.class);

    @Source("RegattaOverview.gss")
    @NotStrict
    LocalCss css();

    public interface LocalCss extends CssResource {
    }
}
