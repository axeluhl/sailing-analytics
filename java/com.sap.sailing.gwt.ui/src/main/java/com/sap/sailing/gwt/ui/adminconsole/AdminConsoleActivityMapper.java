package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleActivity;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsolePlace;
import com.sap.sse.gwt.client.DOMUtils;

public class AdminConsoleActivityMapper implements ActivityMapper {

    private final AdminConsoleClientFactory clientFactory;

    public AdminConsoleActivityMapper(AdminConsoleClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    @Override
    public Activity getActivity(Place place) {
        //DOMUtils.scrollToTop(clientFactory.getRoot());

        if (place instanceof AdminConsolePlace) {
            return new AdminConsoleActivity((AdminConsolePlace) place, clientFactory);
        }
        return null;
    }
}
