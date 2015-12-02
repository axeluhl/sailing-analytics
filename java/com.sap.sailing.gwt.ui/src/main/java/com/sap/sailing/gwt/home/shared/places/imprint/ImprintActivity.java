package com.sap.sailing.gwt.home.shared.places.imprint;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class ImprintActivity extends AbstractActivity {

    public ImprintActivity(ImprintPlace place) {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new ImprintViewImpl());
    }

}
