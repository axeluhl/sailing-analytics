package com.sap.sse.security.ui.authentication.generic.resource;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.DataResource;
import com.google.gwt.resources.client.DataResource.MimeType;

public interface AuthenticationResources extends ClientBundle {
    
    @Source("loggedin.svg")
    @MimeType("image/svg+xml")
    DataResource loggedin();
    
    @Source("loggedin-open.svg")
    @MimeType("image/svg+xml")
    DataResource loggedinOpen();
    
    @Source("loggedout.svg")
    @MimeType("image/svg+xml")
    DataResource loggedout();
    
    @Source("loggedout-open.svg")
    @MimeType("image/svg+xml")
    DataResource loggedoutOpen();
}
