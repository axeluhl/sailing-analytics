package com.sap.sse.gwt.client.mvp.example.goodbye;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

public class GoodbyeActivity extends AbstractActivity {
    private GoodbyeViewFactory clientFactory;
    // Name that will be appended to "Good-bye, "
    private String name;

    public GoodbyeActivity(GoodbyePlace place, GoodbyeViewFactory clientFactory) {
        this.name = place.getGoodbyeName();
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget containerWidget, EventBus eventBus) {
        GoodbyeView goodbyeView = clientFactory.getGoodbyeView();
        goodbyeView.setName(name);
        containerWidget.setWidget(goodbyeView.asWidget());
    }
}