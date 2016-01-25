package com.sap.sailing.gwt.home.shared.usermanagement.confirm;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.security.ui.client.i18n.StringMessages;

public class ConfirmationInfoActivity extends AbstractActivity implements ConfirmationInfoView.Presenter {
    
    private final ConfirmationInfoPlace place;
    private final ConfirmationInfoView view;
    private final StringMessages i18n_sec = StringMessages.INSTANCE;
    
    public ConfirmationInfoActivity(ConfirmationInfoPlace place, ConfirmationInfoView view) {
        this.place = place;
        this.view = view;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(view);
        switch (place.getAction()) {
        case ACCOUNT_CREATED:
            view.setMessage(i18n_sec.signedUpSuccessfully(place.getName()));
            break;
        case RESET_REQUESTED:
            view.setMessage(i18n_sec.passwordResetLinkSent(place.getName()));
            break;
        case ERROR:
        default:
            view.setMessage(i18n_sec.error());
            break;
        }
    }

}
