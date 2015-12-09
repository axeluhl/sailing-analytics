package com.sap.sailing.gwt.home.shared.usermanagement.info;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.desktop.app.TabletAndDesktopApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementRequestEvent;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class LoggedInUserInfoActivity extends AbstractActivity implements LoggedInUserInfoView.Presenter {

    private TabletAndDesktopApplicationClientFactory clientFactory;
    
    public LoggedInUserInfoActivity(TabletAndDesktopApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        final LoggedInUserInfoView view = new LoggedInUserInfoViewImpl();
        view.setPresenter(this);
        panel.setWidget(view);
        view.setUserInfo(clientFactory.getUserManagementContext().getCurrentUser());
        eventBus.addHandler(UserManagementContextEvent.TYPE, new UserManagementContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(UserManagementContextEvent event) {
                view.setUserInfo(event.getCtx().getCurrentUser());
            }
        });
    }

    @Override
    public void gotoProfileUi() {
        clientFactory.getHomePlacesNavigator().getUserProfileNavigation().goToPlace();
    }

    @Override
    public void signOut() {
        clientFactory.getUserManagement().logout(new AsyncCallback<SuccessInfo>() {
            @Override
            public void onSuccess(SuccessInfo result) {
                clientFactory.getEventBus().fireEvent(new UserManagementRequestEvent());
                clientFactory.resetUserManagementContext();
            }

            @Override
            public void onFailure(Throwable caught) {
                clientFactory.getEventBus().fireEvent(new UserManagementRequestEvent());
                clientFactory.resetUserManagementContext();
            }
        });
    }


}
