package com.sap.sailing.gwt.home.shared.usermanagement.create;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;

public class CreateAccountActivity extends AbstractActivity implements CreateAccountView.Presenter {

    private final ClientFactoryWithUserManagementService clientFactory;
    private final PlaceController placeController;

    public CreateAccountActivity(ClientFactoryWithUserManagementService clientFactory, PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
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
