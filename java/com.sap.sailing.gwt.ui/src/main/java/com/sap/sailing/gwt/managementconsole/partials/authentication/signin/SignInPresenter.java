package com.sap.sailing.gwt.managementconsole.partials.authentication.signin;

import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sse.security.ui.authentication.AuthenticationManager;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;

public class SignInPresenter implements SignInView.Presenter, AuthenticationRequestEvent.Handler {

    private final AuthenticationManager authenticationManager;
    private final SignInView view;

    public SignInPresenter(final ManagementConsoleClientFactory clientFactory) {
        this.authenticationManager = clientFactory.getAuthenticationManager();
        this.view = clientFactory.getViewFactory().getSignInView();
        this.view.setPresenter(this);
    }

    @Override
    public void login(final String loginName, final String password) {
        authenticationManager.login(loginName, password, info -> view.hide());
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
    public void onUserManagementRequestEvent(final AuthenticationRequestEvent event) {
        view.clearInputs();
        view.show();
    }

}
