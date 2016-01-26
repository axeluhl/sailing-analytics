package com.sap.sailing.gwt.home.shared.usermanagement.recovery;

import static com.sap.sse.security.shared.UserManagementException.CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.AuthenticationClientFactory;
import com.sap.sailing.gwt.home.shared.usermanagement.confirm.ConfirmationInfoPlace;
import com.sap.sailing.gwt.home.shared.usermanagement.confirm.ConfirmationInfoPlace.Action;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.authentication.recover.PasswordRecoveryView;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class PasswordRecoveryActivity extends AbstractActivity implements PasswordRecoveryView.Presenter {

    private final AuthenticationClientFactory clientFactory;
    private final PlaceController placeController;
    private final PasswordRecoveryView view;
    private final Callback callback;

    public PasswordRecoveryActivity(PasswordRecoveryView view, AuthenticationClientFactory clientFactory,
            PasswordRecoveryView.Presenter.Callback callback, PlaceController placeController) {
        this.view = view;
        this.clientFactory = clientFactory;
        this.placeController = placeController;
        this.callback = callback;
    }
    
    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        view.setPresenter(this);
        panel.setWidget(view);
    }

    @Override
    public void resetPassword(final String email, final String username) {
        clientFactory.getUserManagementService().resetPassword(username, email, callback.getPasswordResetUrl(),
                new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        String name = (username == null || username.isEmpty()) ? email : username;
                        placeController.goTo(new ConfirmationInfoPlace(Action.RESET_REQUESTED, name));
                    }
                    
                    @Override
                    public void onFailure(Throwable caught) {
                        StringMessages i18n = StringMessages.INSTANCE;
                        if (caught instanceof UserManagementException) {
                            if (CANNOT_RESET_PASSWORD_WITHOUT_VALIDATED_EMAIL.equals(caught.getMessage())) {
                                view.setErrorMessage(i18n.cannotResetPasswordWithoutValidatedEmail(username));
                            } else {
                                view.setErrorMessage(i18n.errorResettingPassword(username, caught.getMessage()));
                            }
                        } else {
                            view.setErrorMessage(i18n.errorDuringPasswordReset(caught.getMessage()));
                        }
                    }
                });
    }
}
