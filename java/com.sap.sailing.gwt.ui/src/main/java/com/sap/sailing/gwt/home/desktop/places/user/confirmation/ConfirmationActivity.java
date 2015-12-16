package com.sap.sailing.gwt.home.desktop.places.user.confirmation;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.desktop.app.DesktopPlacesNavigator;
import com.sap.sailing.gwt.home.shared.places.user.confirmation.ConfirmationPlace;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;

public class ConfirmationActivity extends AbstractActivity {
    private final ConfirmationPlace place;
    private final ConfirmationClientFactory clientFactory;
    private final DesktopPlacesNavigator homePlacesNavigator;
    private final com.sap.sailing.gwt.ui.client.StringMessages I18N = com.sap.sailing.gwt.ui.client.StringMessages.INSTANCE;
    private final com.sap.sse.security.ui.client.i18n.StringMessages I18N_SEC = com.sap.sse.security.ui.client.i18n.StringMessages.INSTANCE;

    public ConfirmationActivity(ConfirmationPlace place, ConfirmationClientFactory clientFactory,
            DesktopPlacesNavigator homePlacesNavigator) {
        this.place = place;
        this.clientFactory = clientFactory;
        this.homePlacesNavigator = homePlacesNavigator;
    }

    @Override
    public void start(final AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(clientFactory.createBusyView());
        clientFactory.getUserManagement().validateEmail(place.getName(), place.getValidationSecret(),
                new MarkedAsyncCallback<Boolean>(new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        panel.setWidget(new MessageViewImpl("TODO: Account Confirmation", I18N_SEC
                                .errorValidatingEmail(place.getName(), caught.getMessage()), I18N.start(),
                                new Command() {
                                    @Override
                                    public void execute() {
                                        homePlacesNavigator.getHomeNavigation().goToPlace();
                                    }
                                }));
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        final MessageViewImpl view;
                        if (result) {
                            view = new MessageViewImpl("TODO: Account Confirmation", I18N_SEC
                                    .emailValidatedSuccessfully(place.getName()), I18N.start(), new Command() {
                                @Override
                                public void execute() {
                                    homePlacesNavigator.getHomeNavigation().goToPlace();
                                }
                            });
                            panel.setWidget(view);
                        } else {
                            view = new MessageViewImpl("TODO: Account Confirmation", I18N_SEC.errorValidatingEmail(
                                    place.getName(), I18N_SEC.emailValidationUnsuccessful(place.getName())), I18N
                                    .start(), new Command() {
                                @Override
                                public void execute() {
                                    homePlacesNavigator.getHomeNavigation().goToPlace();
                                }
                            });
                        }
                        panel.setWidget(view);
                    }
                }));
    }
}
