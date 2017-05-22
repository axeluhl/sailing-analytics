package com.sap.sse.gwt.client.shared.components;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ComponentResources extends ClientBundle {

    @Source("com/sap/sse/gwt/client/images/open.png")
    ImageResource openIcon();

    @Source("com/sap/sse/gwt/client/images/close.png")
    ImageResource closeIcon();

    @Source("com/sap/sse/gwt/client/images/settings.png")
    ImageResource settingsIcon();

    @Source("com/sap/sse/gwt/client/images/leaderboardsettings.png")
    ImageResource darkSettingsIcon();
}