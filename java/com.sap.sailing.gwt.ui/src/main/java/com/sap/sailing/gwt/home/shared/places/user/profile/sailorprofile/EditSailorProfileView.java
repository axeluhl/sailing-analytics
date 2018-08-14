package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.StatefulSailorProfileDataProvider;

public interface EditSailorProfileView extends IsWidget {

    public interface Presenter {

        StatefulSailorProfileDataProvider getDataProvider();

        SharedSailorProfileCompetitorDataProvider getCompetitorsDataProvider();

        PlaceController getPlaceController();
    }

    void setEntry(SailorProfileDTO entry);

}
