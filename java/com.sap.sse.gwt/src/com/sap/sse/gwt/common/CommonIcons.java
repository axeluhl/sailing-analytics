package com.sap.sse.gwt.common;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface CommonIcons extends ClientBundle {

    @Source("facebook-logo.svg")
    @MimeType("image/svg+xml")
    DataResource facebook();
    
    @Source("twitter-logo.svg")
    @MimeType("image/svg+xml")
    DataResource twitter();

    @Source("google.svg")
    @MimeType("image/svg+xml")
    DataResource google();
    
    @Source("search-icon.svg")
    @MimeType("image/svg+xml")
    DataResource search();
}
