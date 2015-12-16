package com.sap.sailing.gwt.home.shared.places.user.confirmation;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class ConfirmationActivity extends AbstractActivity {
    private final ConfirmationPlace place;
    private final ConfirmationClientFactory clientFactory;
    private final StringMessages I18N_SEC = StringMessages.INSTANCE;

    public ConfirmationActivity(ConfirmationPlace place, ConfirmationClientFactory clientFactory) {
        this.place = place;
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        switch (place.getAction()) {
        case ACC_CREATED:
        case CHANGED_EMAIL:
            panel.setWidget(clientFactory.createBusyView());
            clientFactory.getUserManagement().validateEmail(place.getName(), place.getValidationSecret(),
                    new MarkedAsyncCallback<Boolean>(new AsyncCallback<Boolean>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            panel.setWidget(new MessageViewImpl(I18N_SEC.accountConfirmation(), I18N_SEC
                                    .errorValidatingEmail(place.getName(), caught.getMessage())));
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            final MessageViewImpl view;
                            if (result) {
                                view = new MessageViewImpl(I18N_SEC.accountConfirmation(), I18N_SEC
                                        .emailValidatedSuccessfully(place.getName()));
                            } else {
                                view = new MessageViewImpl(I18N_SEC.accountConfirmation(), I18N_SEC
                                        .errorValidatingEmail(place.getName(),
                                                I18N_SEC.emailValidationUnsuccessful(place.getName())));
                            }
                            panel.setWidget(view);
                        }
                    }));
            break;
        case ERROR:
            panel.setWidget(new MessageViewImpl(I18N_SEC.accountConfirmation(), I18N_SEC.error()));
            break;
        }
    }
}
