package com.sap.sse.security.ui.authentication.recover;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactory;
import com.sap.sse.security.ui.authentication.AuthenticationManager.SuccessCallback;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoPlace;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoPlace.Action;

public class PasswordRecoveryActivity extends AbstractActivity implements PasswordRecoveryView.Presenter {

    private final AuthenticationClientFactory clientFactory;
    private final PlaceController placeController;
    private final PasswordRecoveryView view;

    public PasswordRecoveryActivity(AuthenticationClientFactory clientFactory, PlaceController placeController) {
        this.clientFactory = clientFactory;
        this.placeController = placeController;
        this.view = clientFactory.createPasswordRecoveryView();
    }
    
    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setPresenter(this);
        panel.setWidget(view);
    }

    @Override
    public void resetPassword(final String email, final String username) {
        clientFactory.getAuthenticationManager().requestPasswordReset(username, email, new SuccessCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                String name = (username == null || username.isEmpty()) ? email : username;
                placeController.goTo(new ConfirmationInfoPlace(Action.RESET_REQUESTED, name));
            }
        });
    }
}
