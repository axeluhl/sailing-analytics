package com.sap.sailing.gwt.home.shared.usermanagement.signin;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;

public class SignInActivity extends AbstractActivity implements SignInView.Presenter {

    private final ClientFactoryWithUserManagementService clientFactory;
    private final PlaceController placeController;
    
    public SignInActivity(ClientFactoryWithUserManagementService clientFactory, PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        SignInView view = new SignInViewImpl();
        view.setPresenter(this);
        panel.setWidget(view);
    }

    @Override
    public void login(String loginName, String password) {
        // TODO Auto-generated method stub
    }

    @Override
    public void createAccount() {
        // TODO Auto-generated method stub
    }

    @Override
    public void forgotPassword() {
        // TODO Auto-generated method stub
    }

    @Override
    public void loginWithFacebook() {
        // TODO Auto-generated method stub
    }

    @Override
    public void loginWithGoogle() {
        // TODO Auto-generated method stub
    }

}
