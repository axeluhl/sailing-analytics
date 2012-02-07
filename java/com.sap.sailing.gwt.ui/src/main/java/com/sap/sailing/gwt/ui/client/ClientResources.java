package com.sap.sailing.gwt.ui.client;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ClientResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/images/settings.png")
    ImageResource settingsIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/play.png")
    ImageResource playIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/pause.png")
    ImageResource pauseIcon();
}