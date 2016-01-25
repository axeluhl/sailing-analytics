package com.sap.sailing.gwt.home.shared.usermanagement.info;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationContextEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.UserManagementRequestEvent;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.signin.SignInPlace;
import com.sap.sse.security.ui.shared.SuccessInfo;

public class LoggedInUserInfoActivity extends AbstractActivity implements LoggedInUserInfoView.Presenter {

    private final AuthenticationClientFactory authenticationClientFactory;
    private final UserManagementClientFactory clientFactory;
    private final PlaceController placeController;
    private final LoggedInUserInfoView view;
    private Callback callback;
    
    public LoggedInUserInfoActivity(LoggedInUserInfoView view, AuthenticationClientFactory authenticationClientFactory,
            UserManagementClientFactory clientFactory, LoggedInUserInfoView.Presenter.Callback callback,
            PlaceController placeController) {
        this.view = view;
        this.authenticationClientFactory = authenticationClientFactory;
        this.clientFactory = clientFactory;
        this.placeController = placeController;
        this.callback = callback;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setPresenter(this);
        panel.setWidget(view);
        view.setUserInfo(authenticationClientFactory.getAuthenticationManager().getAuthenticationContext());
        eventBus.addHandler(AuthenticationContextEvent.TYPE, new AuthenticationContextEvent.Handler() {
            @Override
            public void onUserChangeEvent(AuthenticationContextEvent event) {
                if (event.getCtx().isLoggedIn()) {
                    view.setUserInfo(event.getCtx());
                } else {
                    placeController.goTo(new SignInPlace());
                }
            }
        });
    }

    @Override
    public void gotoProfileUi() {
        clientFactory.getEventBus().fireEvent(new UserManagementRequestEvent());
        callback.handleUserProfileNavigation();
    }

    @Override
    public void signOut() {
        clientFactory.getUserManagement().logout(new AsyncCallback<SuccessInfo>() {
            @Override
            public void onSuccess(SuccessInfo result) {
                authenticationClientFactory.getAuthenticationManager().didLogout();
            }

            @Override
            public void onFailure(Throwable caught) {
                authenticationClientFactory.getAuthenticationManager().didLogout();
            }
        });
    }


}
