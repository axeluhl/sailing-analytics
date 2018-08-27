package com.sap.sailing.gwt.home.mobile.places.user.profile.sailorprofiles.details;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SailorProfileMobileResources extends ClientBundle {

    public static final SailorProfileMobileResources INSTANCE = GWT.create(SailorProfileMobileResources.class);

    @Source("SailorProfileMobile.gss")
    SailorProfileMobileCss css();

    public interface SailorProfileMobileCss extends CssResource {

    }

}
