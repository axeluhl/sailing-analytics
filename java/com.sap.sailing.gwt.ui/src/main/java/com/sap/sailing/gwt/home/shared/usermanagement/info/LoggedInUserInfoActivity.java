package com.sap.sailing.gwt.home.shared.usermanagement.info;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.app.ClientFactoryWithUserManagementService;

public class LoggedInUserInfoActivity extends AbstractActivity implements LoggedInUserInfoView.Presenter {

    private ClientFactoryWithUserManagementService clientFactory;
    
    public LoggedInUserInfoActivity(ClientFactoryWithUserManagementService clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        LoggedInUserInfoView view = new LoggedInUserInfoViewImpl();
        view.setPresenter(this);
        panel.setWidget(view);
    }

    @Override
    public void gotoProfileUi() {
        // TODO Auto-generated method stub
    }

    @Override
    public void signOut() {
        // TODO Auto-generated method stub
    }


}
