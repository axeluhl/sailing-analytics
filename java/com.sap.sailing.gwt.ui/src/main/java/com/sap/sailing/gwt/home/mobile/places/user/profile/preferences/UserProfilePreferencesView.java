package com.sap.sailing.gwt.home.mobile.places.user.profile.preferences;

import com.sap.sailing.gwt.home.mobile.places.user.profile.UserProfileViewBase;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.shared.partials.multiselection.SuggestedMultiSelectionCompetitorDataProvider;

public interface UserProfilePreferencesView extends UserProfileViewBase {
    
    public interface Presenter extends UserProfileViewBase.Presenter {
        SuggestedMultiSelectionBoatClassDataProvider getFavoriteBoatClassesDataProvider();
        SuggestedMultiSelectionCompetitorDataProvider getFavoriteCompetitorsDataProvider();
    }
}

