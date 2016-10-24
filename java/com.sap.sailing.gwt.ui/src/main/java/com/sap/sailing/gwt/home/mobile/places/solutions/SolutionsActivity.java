package com.sap.sailing.gwt.home.mobile.places.solutions;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.home.mobile.app.MobileApplicationClientFactory;
import com.sap.sailing.gwt.home.shared.places.solutions.SolutionsPlace;

public class SolutionsActivity extends AbstractActivity implements SolutionsView.Presenter {
    
    private final MobileApplicationClientFactory clientFactory;

    public SolutionsActivity(SolutionsPlace place, MobileApplicationClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        panel.setWidget(new SolutionsViewImpl(this, clientFactory.getNavigator()));
    }
}
