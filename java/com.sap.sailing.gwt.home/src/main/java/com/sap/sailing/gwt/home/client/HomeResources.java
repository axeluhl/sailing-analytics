package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface HomeResources extends ClientBundle {
    public static final HomeResources INSTANCE = GWT.create(HomeResources.class);

    @Source("com/sap/sailing/gwt/home/images/sap_300_transparent.png")
    ImageResource sapLogo300();

    @Source("com/sap/sailing/gwt/home/images/sap_66_transparent.png")
    ImageResource sapLogo66();
}