package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class ConfirmationActivity extends AbstractActivity implements ConfirmationView.Presenter {
    private final ConfirmationPlace place;
    private final ConfirmationClientFactory clientFactory;
    private final StringMessages i18n_sec = StringMessages.INSTANCE;

    public ConfirmationActivity(ConfirmationPlace place, ConfirmationClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        switch (place.getAction()) {
        case MAIL_VERIFIED:
        case CHANGED_EMAIL:
            panel.setWidget(clientFactory.createBusyView());
            clientFactory.getUserManagement().validateEmail(place.getName(), place.getValidationSecret(),
                    new MarkedAsyncCallback<Boolean>(new AsyncCallback<Boolean>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            panel.setWidget(new ConfirmationViewImpl(i18n_sec.accountConfirmation(), i18n_sec
                                    .errorValidatingEmail(place.getName(), caught.getMessage())));
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            final ConfirmationView view;
                            if (result) {
                                view = new ConfirmationViewImpl(i18n_sec.accountConfirmation(), i18n_sec
                                        .emailValidatedSuccessfully(place.getName()));
                            } else {
                                view = new ConfirmationViewImpl(i18n_sec.accountConfirmation(), i18n_sec
                                        .errorValidatingEmail(place.getName(),
                                                i18n_sec.emailValidationUnsuccessful(place.getName())));
                            }
                            panel.setWidget(view.asWidget());
                        }
                    }));
            break;
        case ERROR:
            panel.setWidget(new ConfirmationViewImpl(i18n_sec.accountConfirmation(), i18n_sec.error()));
            break;
        case ACC_CREATED:
            panel.setWidget(new ConfirmationViewImpl(i18n_sec.accountConfirmation(), i18n_sec
                    .signedUpSuccessfully(place.getName())));
            break;
        case RESET_SEND:
            panel.setWidget(new ConfirmationViewImpl(i18n_sec.accountConfirmation(), i18n_sec
                    .successfullyResetPassword(place.getName())));
            break;
        default:
            break;
        }
    }
}
