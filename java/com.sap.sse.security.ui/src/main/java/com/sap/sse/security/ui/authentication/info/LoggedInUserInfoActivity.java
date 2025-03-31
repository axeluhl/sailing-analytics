package com.sap.sse.security.ui.authentication.info;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.security.ui.authentication.AuthenticationCallback;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactory;
import com.sap.sse.security.ui.authentication.AuthenticationContextEvent;
import com.sap.sse.security.ui.authentication.AuthenticationPlaces;
import com.sap.sse.security.ui.authentication.AuthenticationRequestEvent;
import com.sap.sse.security.ui.authentication.signin.SignInPlace;

public class LoggedInUserInfoActivity extends AbstractActivity implements LoggedInUserInfoView.Presenter {

    private final AuthenticationClientFactory clientFactory;
    private final PlaceController placeController;
    private final AuthenticationCallback callback;
    private EventBus eventBus;
    
    public LoggedInUserInfoActivity(AuthenticationClientFactory clientFactory, AuthenticationCallback callback,
            PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
        this.callback = callback;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        final LoggedInUserInfoView view = clientFactory.createLoggedInUserInfoView();
        this.eventBus = eventBus;
        view.setPresenter(this);
        panel.setWidget(view);
        view.setUserInfo(clientFactory.getAuthenticationManager().getAuthenticationContext());
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
        eventBus.fireEvent(new AuthenticationRequestEvent(AuthenticationPlaces.SIGN_IN));
        callback.handleUserProfileNavigation();
    }

    @Override
    public void signOut() {
        clientFactory.getAuthenticationManager().logout();
    }

}
