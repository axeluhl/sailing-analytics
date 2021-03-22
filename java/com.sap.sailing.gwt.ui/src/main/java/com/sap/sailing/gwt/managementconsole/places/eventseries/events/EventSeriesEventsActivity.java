package com.sap.sailing.gwt.managementconsole.places.eventseries.events;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;

public class EventSeriesEventsActivity extends AbstractManagementConsoleActivity<EventSeriesEventsPlace>
        implements EventSeriesEventsView.Presenter {


    public EventSeriesEventsActivity(final ManagementConsoleClientFactory clientFactory, final EventSeriesEventsPlace place) {
        super(clientFactory, place);
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        final EventSeriesEventsView view = getClientFactory().getViewFactory().getEventSeriesEventsView();
        view.setPresenter(this);
        container.setWidget(view);
    }

}
