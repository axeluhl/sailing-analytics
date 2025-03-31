package com.sap.sailing.gwt.home.mobile.partials.quickfinder;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface QuickfinderResources extends ClientBundle {

    public static final QuickfinderResources INSTANCE = GWT.create(QuickfinderResources.class);

    @Source("Quickfinder.gss")
    LocalCss css();
    
    @Source("quickfinder-arrow.svg")
    @MimeType("image/svg+xml")
    DataResource arrow();

    public interface LocalCss extends CssResource {
        String quickfinder();
        String quickfinder_select();
    }
}
