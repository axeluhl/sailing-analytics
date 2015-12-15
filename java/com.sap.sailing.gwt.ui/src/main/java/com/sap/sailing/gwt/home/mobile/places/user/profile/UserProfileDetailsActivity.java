package com.sap.sailing.gwt.home.mobile.places.user.profile;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;

public class UserProfileDetailsActivity extends AbstractActivity implements UserProfileDetailsView.Presenter {

    private final MobileApplicationClientFactory clientFactory;
    
    private final UserProfileDetailsView currentView = new UserProfileDetailsViewImpl(this);
    
    public UserProfileDetailsActivity(AbstractUserProfilePlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        panel.setWidget(currentView);
        currentView.setUserManagementContext(clientFactory.getUserManagementContext());
        eventBus.addHandler(UserManagementContextEvent.TYPE, new UserManagementContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(UserManagementContextEvent event) {
                currentView.setUserManagementContext(event.getCtx());
            }
        });
    }
    
    @Override
    public void handleSaveChangesRequest(String email) {
        Window.alert("Saving changes ...");
    }
    
    @Override
    public void handlePasswordChangeRequest(String oldPassword, String newPassword, String newPasswordConfirmation) {
        Window.alert("Changing password ...");
    }
}
