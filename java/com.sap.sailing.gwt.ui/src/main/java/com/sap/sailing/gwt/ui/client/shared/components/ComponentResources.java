package com.sap.sailing.gwt.ui.client.shared.components;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface ComponentResources extends ClientBundle {

    @Source("com/sap/sailing/gwt/ui/client/images/open.png")
    ImageResource openIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/close.png")
    ImageResource closeIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/settings.png")
    ImageResource settingsIcon();

    @Source("com/sap/sailing/gwt/ui/client/images/leaderboardsettings.png")
    ImageResource darkSettingsIcon();
}