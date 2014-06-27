package com.sap.sailing.gwt.home.client.shared.sponsoring;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.sap.sailing.gwt.home.client.HomeResources;

public interface SponsoringResources extends ClientBundle {
    public static final SponsoringResources INSTANCE = GWT.create(SponsoringResources.class);

    @Source({"com/sap/sailing/gwt/home/client/shared/sponsoring/Sponsoring.css", "com/sap/sailing/gwt/home/main.css"})
    LocalCss css();

    public interface LocalCss extends HomeResources.MainCss {
    }
}
