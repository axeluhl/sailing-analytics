package com.sap.sailing.gwt.home.mobile.places.morelogininformation;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.morelogininformation.AbstractMoreLoginInformationActivity;

public class MoreLoginInformationActivity extends AbstractMoreLoginInformationActivity {

    public MoreLoginInformationActivity(Place place, MobileApplicationClientFactory clientFactory) {
        super(clientFactory, new MoreLoginInformationMobile(
                () -> clientFactory.getNavigator().getCreateAccountNavigation().goToPlace()));
    }

}
