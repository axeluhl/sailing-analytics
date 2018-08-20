package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface SailorProfileResources extends ClientBundle {

    public static final SailorProfileResources INSTANCE = GWT.create(SailorProfileResources.class);

    @Source("SailorProfiles.gss")
    SailorProfilesCss css();

    public interface SailorProfilesCss extends CssResource {
        String rotateLeft();

        String eventsTable();

        String eventsTableTitle();

        String eventsTableEmpty();

        String eventsTablePanel();

        String overviewTable();

        String overviewTableFooter();

        String overviewTablePanel();

        String overviewTableEmpty();
    }
}
