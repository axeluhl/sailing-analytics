package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.UUID;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.SailorProfileDetailsView;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.SailorProfileDataProvider;

public interface SharedSailorProfileView extends IsWidget {

    /**
     * Defines whether or not the {@link SharedSailorProfileView} should be optimized to fill the whole display width.
     * 
     * @param edgeToEdge
     *            <code>true</code> if this view is used in an edge-to-edge layout (usually in mobile version),
     *            <code>false</code> otherwise
     */
    public void setEdgeToEdge(boolean edgeToEdge);

    public interface Presenter {

        SailorProfileDataProvider getDataProvider();

        void loadPreferences();

        SuggestedMultiSelectionBoatClassDataProvider getFavoriteBoatClassesDataProvider();

        SuggestedMultiSelectionCompetitorDataProvider getFavoriteCompetitorsDataProvider();

        SuggestedMultiSelectionCompetitorDataProvider getCompetitorsDataProvider();

        PlaceController getPlaceController();

        void refreshSailorProfile(UUID uuid, SailorProfileDetailsView sailorView);
    }

}
