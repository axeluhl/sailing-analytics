package com.sap.sailing.gwt.managementconsole.places.showcase;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;

public class ShowcaseActivity extends AbstractManagementConsoleActivity<ShowcasePlace> {

    public ShowcaseActivity(final ManagementConsoleClientFactory clientFactory, final ShowcasePlace place) {
        super(clientFactory, place);
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        container.setWidget(new ShowcaseView());
    }

}
