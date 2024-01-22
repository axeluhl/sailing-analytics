package com.sap.sailing.gwt.home.desktop.resources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;
import com.sap.sailing.gwt.home.shared.SharedHomeResources;

public interface SharedDesktopResources extends SharedHomeResources {

    public static final SharedDesktopResources INSTANCE = GWT.create(SharedDesktopResources.class);

    @Source("liveraces.svg")
    @MimeType("image/svg+xml")
    DataResource liveraces();
}
