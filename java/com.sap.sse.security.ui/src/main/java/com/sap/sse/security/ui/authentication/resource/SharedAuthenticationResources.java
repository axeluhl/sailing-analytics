package com.sap.sse.security.ui.authentication.resource;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface SharedAuthenticationResources extends ClientBundle {
    
    public static SharedAuthenticationResources INSTANCE = GWT.create(SharedAuthenticationResources.class);
    
    @Source("edit.svg")
    @MimeType("image/svg+xml")
    DataResource edit();
    
    @Source("userdefault.svg")
    @MimeType("image/svg+xml")
    DataResource userdefault();
}
