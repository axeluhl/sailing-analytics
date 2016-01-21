package com.sap.sailing.gwt.home.shared.usermanagement.signin;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.AsyncLoginCallback;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementPlaceManagementController;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryPlace;

public class SignInActivity extends AbstractActivity implements SignInView.Presenter {

    private final UserManagementClientFactory clientFactory;
    private final PlaceController placeController;
    private final SignInView view = new SignInViewImpl();
    private final UserManagementPlaceManagementController.Callback callback;
    
    public SignInActivity(SignInPlace place, UserManagementClientFactory clientFactory,
            UserManagementPlaceManagementController.Callback callback, PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
        this.callback = callback;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setPresenter(this);
        panel.setWidget(view);
        eventBus.addHandler(UserManagementContextEvent.TYPE, new UserManagementContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(UserManagementContextEvent event) {
                if (event.getCtx().isLoggedIn()) {
                    placeController.goTo(new LoggedInUserInfoPlace());
                }
            }
        });
    }

    @Override
    public void login(String loginName, String password) {
        clientFactory.getUserManagement().login(loginName, password,
                new AsyncLoginCallback(clientFactory, view, callback, true));
    }

    @Override
    public void createAccount() {
        placeController.goTo(new CreateAccountPlace());
    }

    @Override
    public void forgotPassword() {
        placeController.goTo(new PasswordRecoveryPlace());
    }

    @Override
    public void loginWithFacebook() {
        // TODO not supported yet
    }

    @Override
    public void loginWithGoogle() {
        // TODO not supported yet
    }

}
