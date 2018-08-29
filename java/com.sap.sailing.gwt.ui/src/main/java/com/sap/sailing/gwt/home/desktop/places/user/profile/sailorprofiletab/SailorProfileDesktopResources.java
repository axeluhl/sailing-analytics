package com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SailorProfileDesktopResources extends ClientBundle {

    public static final SailorProfileDesktopResources INSTANCE = GWT.create(SailorProfileDesktopResources.class);

    @Source("SailorProfiles.gss")
    SailorProfilesCss css();

    public interface SailorProfilesCss extends CssResource {
        String showAndEditHeader();
    }
}
