package com.sap.sailing.gwt.home.desktop.places.morelogininformation;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.desktop.app.DesktopClientFactory;
import com.sap.sailing.gwt.home.shared.places.morelogininformation.AbstractMoreLoginInformationActivity;
import com.sap.sse.security.ui.authentication.AuthenticationPlaces;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;

public class MoreLoginInformationActivity extends AbstractMoreLoginInformationActivity {

    public MoreLoginInformationActivity(Place place, DesktopClientFactory clientFactory) {
        super(clientFactory, new MoreLoginInformationDesktop(() -> clientFactory.getEventBus()
                .fireEvent(new AuthenticationRequestEvent(AuthenticationPlaces.CREATE_ACCOUNT))));
    }

}
