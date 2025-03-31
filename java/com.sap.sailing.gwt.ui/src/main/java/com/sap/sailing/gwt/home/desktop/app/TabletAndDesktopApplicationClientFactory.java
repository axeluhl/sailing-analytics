package com.sap.sailing.gwt.home.desktop.app;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.common.client.SharedResources;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystem;
import com.sap.sailing.gwt.home.communication.SailingDispatchSystemImpl;
import com.sap.sailing.gwt.home.desktop.places.error.TabletAndDesktopErrorView;
import com.sap.sailing.gwt.home.desktop.places.events.EventsView;
import com.sap.sailing.gwt.home.desktop.places.events.TabletAndDesktopEventsView;
import com.sap.sailing.gwt.home.desktop.places.searchresult.TabletAndDesktopSearchResultView;
import com.sap.sailing.gwt.home.desktop.places.solutions.SolutionsView;
import com.sap.sailing.gwt.home.desktop.places.solutions.TabletAndDesktopSolutionsView;
import com.sap.sailing.gwt.home.desktop.places.sponsoring.SponsoringView;
import com.sap.sailing.gwt.home.desktop.places.sponsoring.TabletAndDesktopSponsoringView;
import com.sap.sailing.gwt.home.desktop.places.start.StartView;
import com.sap.sailing.gwt.home.desktop.places.start.TabletAndDesktopStartView;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.TabletAndDesktopWhatsNewView;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewPlace.WhatsNewNavigationTabs;
import com.sap.sailing.gwt.home.desktop.places.whatsnew.WhatsNewView;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.partials.busy.BusyViewImpl;
import com.sap.sailing.gwt.home.shared.partials.dialog.whatsnew.WhatsNewDialogFactory;
import com.sap.sailing.gwt.home.shared.places.searchresult.SearchResultView;
import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace.SolutionsNavigationTabs;
import com.sap.sailing.gwt.home.shared.places.subscription.SubscriptionView;
import com.sap.sailing.gwt.home.shared.places.subscription.SubscriptionViewImpl;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationView;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationViewImpl;
import com.sap.sailing.gwt.home.shared.places.user.passwordreset.PasswordResetView;
import com.sap.sailing.gwt.home.shared.places.user.passwordreset.PasswordResetViewImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationCallbackImpl;
import com.sap.sailing.gwt.home.shared.usermanagement.view.AuthenticationViewDesktop;
import com.sap.sailing.gwt.ui.client.refresh.BusyView;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactoryImpl;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.AuthenticationManagerImpl;
import com.sap.sse.security.ui.authentication.AuthenticationPlaceManagementController;
import com.sap.sse.security.ui.authentication.info.LoggedInUserInfoPlace;
import com.sap.sse.security.ui.authentication.view.FlyoutAuthenticationPresenter;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class TabletAndDesktopApplicationClientFactory extends AbstractApplicationClientFactory<DesktopApplicationTopLevelView> {
    private final SailingDispatchSystem dispatch = new SailingDispatchSystemImpl();
    private final AuthenticationPlaceManagementController userManagementWizardController;
    private final AuthenticationManager authenticationManager;

    public TabletAndDesktopApplicationClientFactory(final boolean isStandaloneServer) {
        this(new SimpleEventBus(), isStandaloneServer);
    }

    private TabletAndDesktopApplicationClientFactory(final EventBus eventBus, final boolean isStandaloneServer) {
        this(eventBus, new PlaceController(eventBus), isStandaloneServer);
    }

    private TabletAndDesktopApplicationClientFactory(final EventBus eventBus, final PlaceController placeController, final boolean isStandaloneServer) {
        this(eventBus, placeController, new DesktopPlacesNavigator(placeController, isStandaloneServer));
    }

    private TabletAndDesktopApplicationClientFactory(final EventBus eventBus, final PlaceController placeController, 
            final DesktopPlacesNavigator placesNavigator) {
        super(new TabletAndDesktopApplicationView(placesNavigator, eventBus), eventBus, placeController, placesNavigator);
        getUserService();
        final AuthenticationViewDesktop userManagementDisplay = new AuthenticationViewDesktop();
        WhatsNewDialogFactory.register(getUserService(), placeController);
        final Runnable signInSuccesfulNavigation = new Runnable() {
            @Override
            public void run() {
                userManagementWizardController.goTo(new LoggedInUserInfoPlace());
            }
        };
        this.authenticationManager = new AuthenticationManagerImpl(this, eventBus, getHomePlacesNavigator()
                .getMailVerifiedConfirmationNavigation().getFullQualifiedUrl(), getHomePlacesNavigator()
                .getPasswordResetNavigation().getFullQualifiedUrl());
        this.userManagementWizardController = new AuthenticationPlaceManagementController(
                new AuthenticationClientFactoryImpl(authenticationManager, SharedResources.INSTANCE),
                new AuthenticationCallbackImpl(getHomePlacesNavigator().getUserProfileNavigation(),
                        signInSuccesfulNavigation), userManagementDisplay, getEventBus());
        new FlyoutAuthenticationPresenter(userManagementDisplay, getTopLevelView().getAuthenticationMenuView(),
                userManagementWizardController, eventBus, authenticationManager.getAuthenticationContext());
        new DesktopLoginHintPopup(authenticationManager, placesNavigator);
    }

    @Override
    public DesktopResettableNavigationPathDisplay getNavigationPathDisplay() {
        return getTopLevelView().getNavigationPathDisplay();
    }

    @Override
    public TabletAndDesktopErrorView createErrorView(final String errorMessage, final Throwable errorReason) {
        return new TabletAndDesktopErrorView(errorMessage, errorReason, null);
    }

    @Override
    public EventsView createEventsView() {
        return new TabletAndDesktopEventsView(getHomePlacesNavigator());
    }

    @Override
    public StartView createStartView() {
        return new TabletAndDesktopStartView(getHomePlacesNavigator());
    }

    @Override
    public SponsoringView createSponsoringView() {
        return new TabletAndDesktopSponsoringView();
    }

    @Override
    public SolutionsView createSolutionsView(final SolutionsNavigationTabs navigationTab) {
        return new TabletAndDesktopSolutionsView(navigationTab, getHomePlacesNavigator());
    }

    @Override
    public SubscriptionView createSubscriptionsView() {
        getSubscriptionServiceFactory().initializeProviders();
        return new SubscriptionViewImpl();
    }

    @Override
    public SearchResultView createSearchResultView() {
        return new TabletAndDesktopSearchResultView(getHomePlacesNavigator());
    }

    @Override
    public WhatsNewView createWhatsNewView(final WhatsNewNavigationTabs navigationTab) {
        return new TabletAndDesktopWhatsNewView(navigationTab, getHomePlacesNavigator());
    }

    @Override
    public SailingDispatchSystem getDispatch() {
        return dispatch;
    }

    @Override
    public BusyView createBusyView() {
        return new BusyViewImpl();
    }

    @Override
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    @Override
    public ConfirmationView createConfirmationView() {
        return new ConfirmationViewImpl(SharedResources.INSTANCE, StringMessages.INSTANCE.accountConfirmation());
    }

    @Override
    public PasswordResetView createPasswordResetView() {
        return new PasswordResetViewImpl();
    }

    @Override
    public PlaceNavigation<ConfirmationPlace> getPasswordResettedConfirmationNavigation(final String username) {
        return getHomePlacesNavigator().getPasswordResettedConfirmationNavigation(username);
    }
}
