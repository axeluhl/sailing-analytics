package com.sap.sse.security.ui.authentication.confirm;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.security.ui.authentication.AuthenticationClientFactory;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class ConfirmationInfoActivity extends AbstractActivity implements ConfirmationInfoView.Presenter {
    
    private final AuthenticationClientFactory clientFactory;
    private final ConfirmationInfoPlace place;
    private final StringMessages i18n_sec = StringMessages.INSTANCE;
    
    public ConfirmationInfoActivity(AuthenticationClientFactory clientFactory, ConfirmationInfoPlace place) {
        this.clientFactory = clientFactory;
        this.place = place;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        ConfirmationInfoView view = clientFactory.createConfirmationInfoView();
        panel.setWidget(view);
        view.setMessage(place.getAction().getMessage(i18n_sec, place.getName()));
    }

}
