package com.sap.sailing.gwt.home.shared.usermanagement.signin;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;
import com.sap.sailing.gwt.home.shared.usermanagement.AsyncLoginCallback;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.create.CreateAccountPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.info.LoggedInUserInfoPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.recovery.PasswordRecoveryPlace;

public class SignInActivity extends AbstractActivity implements SignInView.Presenter {

    private final ClientFactoryWithUserManagementService clientFactory;
    private final PlaceController placeController;
    private final SignInPlace place;
    private final SignInView view = new SignInViewImpl();
    
    public SignInActivity(SignInPlace place, ClientFactoryWithUserManagementService clientFactory,
            PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
        this.place = place;
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
                new AsyncLoginCallback(clientFactory, placeController, place.getNextTarget(), view));
    }

    @Override
    public void createAccount() {
        placeController.goTo(new CreateAccountPlace(place.getNextTarget()));
    }

    @Override
    public void forgotPassword() {
        placeController.goTo(new PasswordRecoveryPlace(place.getNextTarget()));
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
