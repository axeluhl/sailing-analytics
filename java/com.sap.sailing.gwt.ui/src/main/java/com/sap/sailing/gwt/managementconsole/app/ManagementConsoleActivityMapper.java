package com.sap.sailing.gwt.managementconsole.app;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewActivityProxy;
import com.sap.sailing.gwt.managementconsole.places.event.overview.EventOverviewPlace;
import com.sap.sailing.gwt.managementconsole.places.showcase.ShowcaseActivityProxy;
import com.sap.sailing.gwt.managementconsole.places.showcase.ShowcasePlace;

public class ManagementConsoleActivityMapper implements ActivityMapper {

    private final ManagementConsoleClientFactory clientFactory;

    public ManagementConsoleActivityMapper(final ManagementConsoleClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(final Place place) {
        if (place instanceof ShowcasePlace) {
            return new ShowcaseActivityProxy(clientFactory, (ShowcasePlace) place);
        } else if (place instanceof EventOverviewPlace) {
            return new EventOverviewActivityProxy(clientFactory, (EventOverviewPlace) place);
        }
        return null;
    }
}
