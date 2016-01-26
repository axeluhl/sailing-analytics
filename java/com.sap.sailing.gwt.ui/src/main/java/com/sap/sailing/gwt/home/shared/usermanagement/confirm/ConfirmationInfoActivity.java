package com.sap.sailing.gwt.home.shared.usermanagement.confirm;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sse.security.ui.authentication.confirm.ConfirmationInfoView;
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
        view.setMessage(place.getAction().getMessage(i18n_sec, place.getName()));
    }

}
