package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.ui.adminconsole.places.AbstractAdminConsolePlace;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleActivity;

public class AdminConsoleActivityMapper implements ActivityMapper {

    private final AdminConsoleClientFactory clientFactory;

    public AdminConsoleActivityMapper(AdminConsoleClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        AdminConsoleActivity activity = null;
        if (place instanceof AbstractAdminConsolePlace) {
            if (AdminConsoleActivity.instantiated()) {
                activity = AdminConsoleActivity.getInstance(clientFactory); 
                activity.goToMenuAndTab((AbstractAdminConsolePlace)place);
            }
            else {
                activity = AdminConsoleActivity.getInstance(clientFactory, (AbstractAdminConsolePlace)place); 
            }
        }
      
        return activity;

    }
}
