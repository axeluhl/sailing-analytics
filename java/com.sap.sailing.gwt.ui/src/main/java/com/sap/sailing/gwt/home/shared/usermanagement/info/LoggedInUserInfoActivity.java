package com.sap.sailing.gwt.home.shared.usermanagement.info;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.desktop.app.TabletAndDesktopApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementContextEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementRequestEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SigInPlace;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class LoggedInUserInfoActivity extends AbstractActivity implements LoggedInUserInfoView.Presenter {

    private TabletAndDesktopApplicationClientFactory clientFactory;
    private PlaceController placeController;
    
    public LoggedInUserInfoActivity(TabletAndDesktopApplicationClientFactory clientFactory,
            PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
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
                if (event.getCtx().isLoggedIn()) {
                    view.setUserInfo(event.getCtx().getCurrentUser());
                } else {
                    placeController.goTo(new SigInPlace());
                }
            }
        });
    }

    @Override
    public void gotoProfileUi() {
        clientFactory.getEventBus().fireEvent(new UserManagementRequestEvent());
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
