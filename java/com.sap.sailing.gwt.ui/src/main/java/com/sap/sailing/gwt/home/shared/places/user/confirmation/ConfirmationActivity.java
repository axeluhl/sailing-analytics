package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.shared.usermanagement.app.UserManagementClientFactory;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class ConfirmationActivity extends AbstractActivity implements ConfirmationView.Presenter {
    private final ConfirmationPlace place;
    private final ConfirmationView view;
    private final UserManagementClientFactory clientFactory;
    private final StringMessages i18n_sec = StringMessages.INSTANCE;

    public ConfirmationActivity(ConfirmationPlace place, ConfirmationView view, UserManagementClientFactory clientFactory) {
        this.place = place;
        this.view = view;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(view);
        switch (place.getAction()) {
        case ERROR:
            view.setMessage(i18n_sec.error());
            break;
        case ACCOUNT_CREATED:
            view.setMessage(i18n_sec.signedUpSuccessfully(place.getName()));
            break;
        case RESET_REQUESTED:
            view.setMessage(i18n_sec.passwordResetLinkSent(place.getName()));
            break;
        case RESET_EXECUTED:
            view.setMessage(i18n_sec.successfullyResetPassword(place.getName()));
            break;
        case MAIL_VERIFIED:
            panel.setWidget(clientFactory.createBusyView());
            clientFactory.getUserManagement().validateEmail(place.getName(), place.getValidationSecret(),
                    new MarkedAsyncCallback<Boolean>(new AsyncCallback<Boolean>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            panel.setWidget(view);
                            view.setMessage(i18n_sec.errorValidatingEmail(place.getName(), caught.getMessage()));
                        }
                        
                        @Override
                        public void onSuccess(Boolean result) {
                            panel.setWidget(view);
                            final String message = result ? i18n_sec.emailValidatedSuccessfully(place.getName())
                                    : i18n_sec.emailValidationUnsuccessful(place.getName());
                            view.setMessage(message);
                        }
                    }));
            break;
        default:
            break;
        }
    }
}
