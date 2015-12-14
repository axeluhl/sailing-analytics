package com.sap.sailing.gwt.home.desktop.places.user.profile;

import java.util.HashMap;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
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
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.EntryPointLinkFactory;
import com.sap.sse.security.ui.client.component.NewAccountValidator;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class UserProfileActivity extends AbstractActivity implements UserProfileView.Presenter {

    private static final ApplicationHistoryMapper historyMapper = GWT.create(ApplicationHistoryMapper.class);
    
    protected final AbstractUserProfilePlace currentPlace;
    protected final UserProfileClientFactory clientFactory;
    protected final DesktopPlacesNavigator homePlacesNavigator;

    private final StringMessages i18n_sec = StringMessages.INSTANCE;
    private final NewAccountValidator validator = new NewAccountValidator(i18n_sec);
    
    private UserProfileView<AbstractUserProfilePlace, UserProfileView.Presenter> currentView = new TabletAndDesktopUserProfileView();
    
    public UserProfileActivity(AbstractUserProfilePlace place, UserProfileClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator, NavigationPathDisplay navigationPathDisplay) {
        this.currentPlace = place;
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;
        
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
        currentView.setUserManagementContext(clientFactory.getUserManagementContext());
        eventBus.addHandler(UserManagementContextEvent.TYPE, new UserManagementContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(UserManagementContextEvent event) {
                currentView.setUserManagementContext(event.getCtx());
            }
        });
        
    }
    
    @Override
    public void handleSaveChangesRequest(final String email) {
        final String username = clientFactory.getUserManagementContext().getCurrentUser().getName();
        clientFactory.getUserManagementService().updateSimpleUserEmail(username, email, 
                EntryPointLinkFactory.createEmailValidationLink(new HashMap<String, String>()), 
                new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Window.alert(i18n_sec.successfullyUpdatedEmail(username, email));
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        Window.alert(i18n_sec.errorUpdatingEmail(caught.getMessage()));
                    }
                });
    }
    
    @Override
    public void handlePasswordChangeRequest(String oldPassword, String newPassword, String newPasswordConfirmation) {
        final String username = clientFactory.getUserManagementContext().getCurrentUser().getName();
        String errorMessage = validator.validateUsernameAndPassword(username, newPassword, newPasswordConfirmation);
        if (errorMessage != null && !errorMessage.isEmpty()) {
            Window.alert(errorMessage);
            return;
        }
        clientFactory.getUserManagementService().updateSimpleUserPassword(username, oldPassword, null, newPassword, 
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof UserManagementException) {
                            String message = ((UserManagementException) caught).getMessage();
                            if (UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS.equals(message)) {
                                Window.alert(i18n_sec.passwordDoesNotMeetRequirements());
                            } else if (UserManagementException.INVALID_CREDENTIALS.equals(message)) {
                                Window.alert(i18n_sec.invalidCredentials());
                            } else {
                                Window.alert(i18n_sec.errorChangingPassword(caught.getMessage()));
                            }
                        } else {
                            Window.alert(i18n_sec.errorChangingPassword(caught.getMessage()));
                        }
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Window.alert(i18n_sec.passwordSuccessfullyChanged());
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
}