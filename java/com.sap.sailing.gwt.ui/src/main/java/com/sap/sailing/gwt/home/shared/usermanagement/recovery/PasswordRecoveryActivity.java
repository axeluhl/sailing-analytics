package com.sap.sailing.gwt.home.shared.usermanagement.recovery;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;

public class PasswordRecoveryActivity extends AbstractActivity implements PasswordRecoveryView.Presenter {

    private final ClientFactoryWithUserManagementService clientFactory;
    private final PlaceController placeController;

    public PasswordRecoveryActivity(ClientFactoryWithUserManagementService clientFactory, PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
    }
    
    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        PasswordRecoveryView view = new PasswordRecoveryViewImpl();
        view.setPresenter(this);
        panel.setWidget(view);
    }

    @Override
    public void resetPassword(String loginName) {
        // TODO Auto-generated method stub
    }
    

}
