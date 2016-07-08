package com.sap.sailing.gwt.home.mobile.places.user.profile.preferences;

import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionBoatClassDataProvider;
import com.sap.sailing.gwt.home.desktop.places.user.profile.selection.SuggestedMultiSelectionCompetitorDataProvider;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public interface UserProfilePreferencesView extends IsWidget, NeedsAuthenticationContext {
    
    public interface Presenter extends NotLoggedInPresenter {
        SuggestedMultiSelectionBoatClassDataProvider getFavoriteBoatClassesDataProvider();
        SuggestedMultiSelectionCompetitorDataProvider getFavoriteCompetitorsDataProvider();
    }
}

