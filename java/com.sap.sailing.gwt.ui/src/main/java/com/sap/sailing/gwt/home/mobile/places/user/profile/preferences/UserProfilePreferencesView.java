package com.sap.sailing.gwt.home.mobile.places.user.profile.preferences;

import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sailing.gwt.home.mobile.places.user.profile.UserProfileViewBase;

public interface UserProfilePreferencesView extends UserProfileViewBase {
    
    public interface Presenter extends UserProfileViewBase.Presenter {
        SuggestedMultiSelectionBoatClassDataProvider getFavoriteBoatClassesDataProvider();
        SuggestedMultiSelectionCompetitorDataProvider getFavoriteCompetitorsDataProvider();
    }
}

