package com.sap.sailing.gwt.managementconsole.places.eventseries.overview;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.events.EventSeriesListResponseEvent;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;

public class EventSeriesOverviewActivity extends AbstractManagementConsoleActivity<EventSeriesOverviewPlace>
        implements EventSeriesOverviewView.Presenter {

    private final EventSeriesOverviewView view;

    public EventSeriesOverviewActivity(final ManagementConsoleClientFactory clientFactory, final EventSeriesOverviewPlace place) {
        super(clientFactory, place);
        this.view = getClientFactory().getViewFactory().getEventSeriesOverviewView();
        this.view.setPresenter(this);
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        eventBus.addHandler(EventSeriesListResponseEvent.TYPE, list -> {
            view.renderEventSeries(list);
            container.setWidget(view);
        });
        getClientFactory().getEventService().requestEventSeriesList(/* forceRequestFromService */ false);
    }

}
