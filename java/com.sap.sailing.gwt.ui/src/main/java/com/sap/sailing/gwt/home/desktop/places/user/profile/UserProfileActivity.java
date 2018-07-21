package com.sap.sailing.gwt.home.desktop.places.user.profile;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.shared.app.ApplicationHistoryMapper;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay;
import com.sap.sailing.gwt.home.shared.app.NavigationPathDisplay.NavigationItem;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.fakeseries.AbstractSeriesPlace;
import com.sap.sailing.gwt.home.shared.places.start.StartPlace;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sailing.gwt.ui.client.FlagImageResolver;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.AuthenticationPlaces;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class UserProfileActivity extends AbstractActivity implements UserProfileView.Presenter {

    private static final ApplicationHistoryMapper historyMapper = GWT.create(ApplicationHistoryMapper.class);
    
    protected final AbstractUserProfilePlace currentPlace;
    protected final UserProfileClientFactory clientFactory;
    protected final DesktopPlacesNavigator homePlacesNavigator;

    private final StringMessages i18n_sec = StringMessages.INSTANCE;
    
    private UserProfileView<AbstractUserProfilePlace, UserProfileView.Presenter> currentView;

    public UserProfileActivity(AbstractUserProfilePlace place, UserProfileClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator, NavigationPathDisplay navigationPathDisplay, FlagImageResolver flagImageResolver) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;
        currentView = new TabletAndDesktopUserProfileView(flagImageResolver);

        initNavigationPath(navigationPathDisplay);
    }
    
    private void initNavigationPath(NavigationPathDisplay navigationPathDisplay) {
        com.sap.sailing.gwt.ui.client.StringMessages i18n = com.sap.sailing.gwt.ui.client.StringMessages.INSTANCE;
        navigationPathDisplay.showNavigationPath(new NavigationItem(i18n.home(), getHomeNavigation()),
                new NavigationItem(i18n_sec.userDetails(), getUserProfileNavigation()));
    }
    
    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        currentView.registerPresenter(this);
        panel.setWidget(currentView);
        currentView.navigateTabsTo(currentPlace);
        currentView.setAuthenticationContext(clientFactory.getAuthenticationManager().getAuthenticationContext());

        eventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                currentView.setAuthenticationContext(event.getCtx());
            }
        });
    }
    
    @Override
    public void handleTabPlaceSelection(TabView<?, ? extends UserProfileView.Presenter> selectedActivity) {
        Place tabPlaceToGo = selectedActivity.placeToFire();
        clientFactory.getPlaceController().goTo(tabPlaceToGo);
    }
    
    public void navigateTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }
    
    @Override
    public SafeUri getUrl(AbstractSeriesPlace place) {
        String token = historyMapper.getToken(place);
        return UriUtils.fromString("#" + token);
    }
    
    @Override
    public PlaceNavigation<StartPlace> getHomeNavigation() {
        return homePlacesNavigator.getHomeNavigation();
    }

    @Override
    public PlaceNavigation<? extends AbstractUserProfilePlace> getUserProfileNavigation() {
        return homePlacesNavigator.getUserProfileNavigation();

    }
    
    @Override
    public void doTriggerLoginForm() {
        clientFactory.getEventBus().fireEvent(new AuthenticationRequestEvent(AuthenticationPlaces.SIGN_IN));
    }
    
    @Override
    public UserProfileClientFactory getClientFactory() {
        return clientFactory;
    }
    
    @Override
    public String getMailVerifiedUrl() {
        return homePlacesNavigator
                .getMailVerifiedConfirmationNavigation().getTargetUrl();
    }
    
}