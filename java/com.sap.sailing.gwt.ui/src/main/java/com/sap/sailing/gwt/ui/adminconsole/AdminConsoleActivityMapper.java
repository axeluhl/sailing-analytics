package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsoleActivity;
import com.sap.sailing.gwt.ui.adminconsole.places.AdminConsolePlace;
import com.sap.sailing.gwt.ui.client.MediaServiceWriteAsync;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;

public class AdminConsoleActivityMapper implements ActivityMapper {
    
    private final AdminConsoleClientFactory clientFactory;
    private final MediaServiceWriteAsync mediaServiceWrite;
    private final SailingServiceWriteAsync sailingService;

    public AdminConsoleActivityMapper(AdminConsoleClientFactory clientFactory, 
            final MediaServiceWriteAsync mediaServiceWrite, final SailingServiceWriteAsync sailingService) {
        super();
        this.clientFactory = clientFactory;
        this.mediaServiceWrite = mediaServiceWrite;
        this.sailingService = sailingService;
    }

    @Override
    public Activity getActivity(Place place) {
        if (place instanceof AdminConsolePlace) {
            return new AdminConsoleActivity((AdminConsolePlace) place, clientFactory, mediaServiceWrite, sailingService);
        }
        return null;
    }
}
