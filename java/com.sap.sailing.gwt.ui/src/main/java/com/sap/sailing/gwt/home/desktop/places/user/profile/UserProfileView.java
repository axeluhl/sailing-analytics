package com.sap.sailing.gwt.home.desktop.places.user.profile;

import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.user.client.ui.IsWidget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sse.security.ui.authentication.app.NeedsAuthenticationContext;
import com.sap.sse.security.ui.authentication.decorator.NotLoggedInPresenter;

public interface UserProfileView<PLACE extends AbstractUserProfilePlace, PRES extends UserProfileView.Presenter> extends IsWidget, NeedsAuthenticationContext {

    public interface Presenter extends NotLoggedInPresenter {
        void handleTabPlaceSelection(TabView<?, ? extends Presenter> selectedActivity);

        SafeUri getUrl(AbstractSeriesPlace place);
        void navigateTo(Place place);
        
        PlaceNavigation<StartPlace> getHomeNavigation();
        PlaceNavigation<? extends AbstractUserProfilePlace> getUserProfileNavigation();

        UserProfileClientFactory getClientFactory();
        String getMailVerifiedUrl();
    }
    
    /**
     * This is the presenter the view can talk to.
     * 
     * @param currentPresenter
     */
    void registerPresenter(PRES currentPresenter);

    /**
     * Tell the view to process tabbar place navigation
     * 
     * @param place
     */
    void navigateTabsTo(PLACE place);
    
    void showErrorInCurrentTab(IsWidget errorView);
}
