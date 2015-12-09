package com.sap.sailing.gwt.home.desktop.places.user.profile;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.desktop.app.WithHeader;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.ProvidesNavigationPath;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;
import com.sap.sse.security.ui.shared.UserDTO;

public class UserProfileActivityProxy extends AbstractActivityProxy implements ProvidesNavigationPath, WithHeader {

    private AbstractUserProfilePlace place;
    private UserProfileClientFactory clientFactory;
    private final DesktopPlacesNavigator homePlacesNavigator;
    private NavigationPathDisplay navigationPathDisplay;

    public UserProfileActivityProxy(AbstractUserProfilePlace place, UserProfileClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator) {
        this.place = place;
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;
    }
    
    @Override
    public void setNavigationPathDisplay(NavigationPathDisplay navigationPathDisplay) {
        this.navigationPathDisplay = navigationPathDisplay;
    }

    @Override
    protected void startAsync() {
        // TODO implement
//        final UUID seriesUUID = UUID.fromString(ctx.getSeriesId());
//        clientFactory.getDispatch().execute(new GetEventSeriesViewAction(seriesUUID), 
//                new ActivityProxyCallback<EventSeriesViewDTO>(clientFactory, place) {
//            @Override
//            public void onSuccess(EventSeriesViewDTO series) {
//                afterLoad(series);
//            }
//        });
        // TODO remove
        afterLoad(null);
    }

    private void afterLoad(final UserDTO user) {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                super.onSuccess(new UserProfileActivity(place, user, clientFactory,
                        homePlacesNavigator, navigationPathDisplay));
            }
        });
    }
}
