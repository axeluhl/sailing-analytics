package com.sap.sailing.gwt.home.client.place.solutions;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;

public class SolutionsActivity extends AbstractActivity {

    public SolutionsActivity(SolutionsPlace place, SolutionsClientFactory clientFactory) {
    }

    @Override
    public void start(AcceptsOneWidget panel, EventBus eventBus) {
        panel.setWidget(new TabletAndDesktopSolutionsView());
        Window.setTitle(TextMessages.INSTANCE.sapSailing() + " - " + TextMessages.INSTANCE.solutions());
    }

}
