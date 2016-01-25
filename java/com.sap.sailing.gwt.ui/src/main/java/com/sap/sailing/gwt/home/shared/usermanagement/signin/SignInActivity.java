package com.sap.sailing.gwt.home.shared.usermanagement.signin;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.AsyncLoginCallback;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationContextEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementPlaceManagementController;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryPlace;

public class SignInActivity extends AbstractActivity implements SignInView.Presenter {

    private final AuthenticationClientFactory clientFactory;
    private final PlaceController placeController;
    private final SignInView view;
    private final UserManagementPlaceManagementController.Callback callback;
    
    public SignInActivity(SignInView view, AuthenticationClientFactory clientFactory,
            UserManagementPlaceManagementController.Callback callback, PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
        this.view = view;
        this.callback = callback;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setPresenter(this);
        panel.setWidget(view);
        eventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                if (event.getCtx().isLoggedIn()) {
                    placeController.goTo(new LoggedInUserInfoPlace());
                }
            }
        });
    }

    @Override
    public void login(String loginName, String password) {
        clientFactory.getUserManagementService().login(loginName, password,
                new AsyncLoginCallback(clientFactory.getAuthenticationManager(), view, callback, true));
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
