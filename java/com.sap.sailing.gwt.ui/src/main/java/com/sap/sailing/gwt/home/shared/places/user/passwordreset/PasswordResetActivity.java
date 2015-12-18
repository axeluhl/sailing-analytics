package com.sap.sailing.gwt.home.shared.places.user.passwordreset;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.security.shared.UserManagementException;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class PasswordResetActivity extends AbstractActivity implements PasswordResetView.Presenter {
    private final PasswordResetPlace place;
    private final PasswordResetClientFactory clientFactory;
    private final StringMessages i18n_sec = StringMessages.INSTANCE;

    public PasswordResetActivity(PasswordResetPlace place, PasswordResetClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        PasswordResetView view = clientFactory.createPasswordResetView();
        view.setPresenter(this);
        panel.setWidget(view.asWidget());
    }

    @Override
    public void resetPassword(String newPassword) {
        clientFactory.getUserManagement().updateSimpleUserPassword(place.getName(), null, place.getResetSecret(),
                newPassword, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (caught instanceof UserManagementException) {
                            String message = ((UserManagementException) caught).getMessage();
                            if (UserManagementException.PASSWORD_DOES_NOT_MEET_REQUIREMENTS.equals(message)) {
                                Window.alert(i18n_sec.passwordDoesNotMeetRequirements());
                            } else if (UserManagementException.INVALID_CREDENTIALS.equals(message)) {
                                Window.alert(i18n_sec.invalidCredentials());
                            } else {
                                Window.alert(i18n_sec.errorChangingPassword(caught.getMessage()));
                            }
                        } else {
                            Window.alert(i18n_sec.errorChangingPassword(caught.getMessage()));
                        }
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Window.alert(i18n_sec.passwordSuccessfullyChanged());
                    }
                });
    }
}
