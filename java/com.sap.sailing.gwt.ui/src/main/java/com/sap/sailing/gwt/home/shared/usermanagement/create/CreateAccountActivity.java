package com.sap.sailing.gwt.home.shared.usermanagement.create;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;

public class CreateAccountActivity extends AbstractActivity implements CreateAccountView.Presenter {

    private ClientFactoryWithUserManagementService clientFactory;

    public CreateAccountActivity(ClientFactoryWithUserManagementService clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        CreateAccountView view = new CreateAccountViewImpl();
        view.setPresenter(this);
        panel.setWidget(view);
    }
    
    @Override
    public void createAccount(String username, String email, String password, String passwordConfirmation) {
        // TODO Auto-generated method stub
    }

    @Override
    public void signIn() {
        // TODO Auto-generated method stub
    }

}
