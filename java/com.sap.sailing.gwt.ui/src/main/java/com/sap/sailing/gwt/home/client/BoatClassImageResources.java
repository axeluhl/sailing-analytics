package com.sap.sailing.gwt.home.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface BoatClassImageResources extends ClientBundle {
    public static final BoatClassImageResources INSTANCE = GWT.create(BoatClassImageResources.class);

    @Source("com/sap/sailing/gwt/ui/client/svg/boatclass/49er.svg")
    ImageResource _49erIcon();
}
