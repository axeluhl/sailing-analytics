package com.sap.sailing.gwt.home.client.place.solutions;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.client.shared.placeholder.Placeholder;

public class SolutionsActivity extends AbstractActivity {
    private final SolutionsClientFactory clientFactory;
    
    public SolutionsActivity(SolutionsPlace place, SolutionsClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new Placeholder());
        final SolutionsView view = clientFactory.createSolutionsView();
        panel.setWidget(view.asWidget());
    }
}
