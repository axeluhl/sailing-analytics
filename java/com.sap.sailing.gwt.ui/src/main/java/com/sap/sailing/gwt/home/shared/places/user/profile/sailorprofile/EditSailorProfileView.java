package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.UUID;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.SailorProfileDetailsView;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.SailorProfileDataProvider;

public interface EditSailorProfileView extends IsWidget {

    public interface Presenter {

        SailorProfileDataProvider getDataProvider();

        SharedSailorProfileCompetitorDataProvider getCompetitorsDataProvider();

        PlaceController getPlaceController();

        void refreshSailorProfile(UUID uuid, SailorProfileDetailsView sailorView);
    }

}
