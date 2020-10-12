package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleActivity;
import com.sap.sse.gwt.adminconsole.AdminConsolePlace;

public class AdminConsoleActivityMapper implements ActivityMapper {

    private final AdminConsoleClientFactory clientFactory;

    public AdminConsoleActivityMapper(AdminConsoleClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        AdminConsoleActivity activity = null;
        if (place instanceof AdminConsolePlace) {
            if (AdminConsoleActivity.instantiated()) {
                activity = AdminConsoleActivity.getInstance(clientFactory); 
                activity.goToMenuAndTab((AdminConsolePlace)place);
            }
            else {
                activity = AdminConsoleActivity.getInstance(clientFactory, (AdminConsolePlace)place); 
            }
        }
      
        return activity;

    }
}
