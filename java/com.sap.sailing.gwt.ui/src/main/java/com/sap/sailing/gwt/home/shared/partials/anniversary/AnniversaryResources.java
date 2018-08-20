package com.sap.sailing.gwt.home.shared.partials.anniversary;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface AnniversaryResources extends ClientBundle {
    
    public static final AnniversaryResources INSTANCE = GWT.create(AnniversaryResources.class);
    
    @Source("icon-bottle-white.svg")
    @MimeType("image/svg+xml")
    DataResource bottle();
}
