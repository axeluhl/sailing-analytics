package com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile;

import java.util.UUID;

import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.sailorprofiletab.details.SailorProfileDetailsView;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionDataProvider;
import com.sap.sailing.gwt.home.shared.places.user.profile.sailorprofile.dataprovider.SailorProfileDataProvider;

/**
 * Interface for the user preferences UI. To support desktop as well as mobile version, an
 * {@link #setEdgeToEdge(boolean) edge-to-edge} flag can be provided, in order to meet the respective layout
 * requirements.
 */
public interface SharedSailorProfileView extends IsWidget {

    /**
     * Defines whether or not the {@link SharedSailorProfileView} should be optimized to fill the whole display width.
     * 
     * @param edgeToEdge
     *            <code>true</code> if this view is used in an edge-to-edge layout (usually in mobile version),
     *            <code>false</code> otherwise
     */
    public void setEdgeToEdge(boolean edgeToEdge);

    /**
     * Presenter interface for the user preferences UI, providing methods to load preferences and to access the required
     * {@link SuggestedMultiSelectionDataProvider}s.
     */
    public interface Presenter {

        SailorProfileDataProvider getDataProvider();

        void loadPreferences();

        SuggestedMultiSelectionBoatClassDataProvider getFavoriteBoatClassesDataProvider();

        SuggestedMultiSelectionCompetitorDataProvider getFavoriteCompetitorsDataProvider();

        SuggestedMultiSelectionCompetitorDataProvider getCompetitorsDataProvider();

        PlaceController getPlaceController();

        void refreshSailorProfileEntry(UUID uuid, SailorProfileDetailsView sailorView);
    }

}
