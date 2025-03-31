package com.sap.sailing.gwt.home.shared.partials.busy;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface BusyViewResources extends ClientBundle {
    public static final BusyViewResources INSTANCE = GWT.create(BusyViewResources.class);

    @Source("BusyView.gss")
    LocalCss css();

    public interface LocalCss extends CssResource {
        String busy();
    }
}
