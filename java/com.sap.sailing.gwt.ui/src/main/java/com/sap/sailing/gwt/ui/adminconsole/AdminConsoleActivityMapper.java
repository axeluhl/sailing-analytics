package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleActivity;
import com.sap.sailing.gwt.ui.adminconsole.places.DefaultPlace;
import com.sap.sailing.gwt.ui.adminconsole.places.events.EventsPlace;

public class AdminConsoleActivityMapper implements ActivityMapper {

    private final AdminConsoleClientFactory clientFactory;
    private AdminConsoleActivity activity;

    public AdminConsoleActivityMapper(AdminConsoleClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        final AbstractAdminConsolePlace defaultPlaceToSet;
        if (place instanceof AbstractAdminConsolePlace) {
            final AbstractAdminConsolePlace adminConsolePlace = (AbstractAdminConsolePlace) place;
            if (activity != null) {
                defaultPlaceToSet = null;
                activity.goToMenuAndTab(adminConsolePlace);
            } else {
                defaultPlaceToSet = adminConsolePlace;
                activity = new AdminConsoleActivity(clientFactory);
            }
        } else if (place instanceof DefaultPlace) {
            defaultPlaceToSet = new EventsPlace((String) null /* no place token */);
            if (activity == null) {
                activity = new AdminConsoleActivity(clientFactory);
            }
        } else {
            defaultPlaceToSet = null;
        }
        if (defaultPlaceToSet != null) {
            activity.setRedirectToPlace(defaultPlaceToSet);
        }
        return activity;
    }
}
