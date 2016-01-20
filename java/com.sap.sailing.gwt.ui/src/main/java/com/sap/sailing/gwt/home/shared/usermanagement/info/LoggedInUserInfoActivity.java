package com.sap.sailing.gwt.home.shared.usermanagement.info;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.app.PlaceNavigation;
import com.sap.sailing.gwt.home.shared.places.user.profile.AbstractUserProfilePlace;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementRequestEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInPlace;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class LoggedInUserInfoActivity extends AbstractActivity implements LoggedInUserInfoView.Presenter {

    private final UserManagementClientFactory clientFactory;
    private final PlaceNavigation<? extends AbstractUserProfilePlace> userProfileNavigation;
    private final PlaceController placeController;
    
    public LoggedInUserInfoActivity(LoggedInUserInfoPlace place, UserManagementClientFactory clientFactory,
            PlaceNavigation<? extends AbstractUserProfilePlace> userProfileNavigation, PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.userProfileNavigation = userProfileNavigation;
        this.placeController = placeController;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        final LoggedInUserInfoView view = new LoggedInUserInfoViewImpl();
        view.setPresenter(this);
        panel.setWidget(view);
        view.setUserInfo(clientFactory.getUserManagementContext());
        eventBus.addHandler(UserManagementContextEvent.TYPE, new UserManagementContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(UserManagementContextEvent event) {
                if (event.getCtx().isLoggedIn()) {
                    view.setUserInfo(event.getCtx());
                } else {
                    placeController.goTo(new SignInPlace());
                }
            }
        });
    }

    @Override
    public void gotoProfileUi() {
        clientFactory.getEventBus().fireEvent(new UserManagementRequestEvent());
        userProfileNavigation.goToPlace();
    }

    @Override
    public void signOut() {
        clientFactory.getUserManagement().logout(new AsyncCallback<SuccessInfo>() {
            @Override
            public void onSuccess(SuccessInfo result) {
                clientFactory.didLogout();
            }

            @Override
            public void onFailure(Throwable caught) {
                clientFactory.didLogout();
            }
        });
    }


}
