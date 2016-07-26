package com.sap.sailing.gwt.home.shared.places.user.profile.preferences;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;

public interface UserPreferencesView extends IsWidget {
    
    public void setEdgeToEdge(boolean edgeToEdge);
    
    public interface Presenter {
        void loadPreferences();
        SuggestedMultiSelectionBoatClassDataProvider getFavoriteBoatClassesDataProvider();
        SuggestedMultiSelectionCompetitorDataProvider getFavoriteCompetitorsDataProvider();
    }

}
