package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.communication.user.profile.domain.SailorProfileDTO;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.SailorProfileDataProvider;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.SailorProfilesCompetitorSelectionPresenter;

/** abstract interface over Sailor Profile Details on mobile and desktop */
public interface EditSailorProfileDetailsView extends IsWidget {

    public interface Presenter {

        SailorProfileDataProvider getDataProvider();

        SailorProfilesCompetitorSelectionPresenter getCompetitorPresenter();

        PlaceController getPlaceController();
    }

    void setEntry(SailorProfileDTO entry);

}
