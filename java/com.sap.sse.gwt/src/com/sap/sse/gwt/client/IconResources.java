package com.sap.sse.gwt.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface IconResources extends ClientBundle {
    
    public static final IconResources INSTANCE = GWT.create(IconResources.class);

    @Source("images/change-ownership.png")
    ImageResource changeOwnershipIcon();

    @Source("images/remove.png")
    ImageResource removeIcon();
    
    @Source("images/edit.png")
    ImageResource editIcon();
}
