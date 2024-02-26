package com.sap.sailing.gwt.managementconsole.places.eventseries.events;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.common.communication.event.EventMetadataDTO;
import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.places.regatta.overview.RegattaOverviewPlace;

public class EventSeriesEventsActivity extends AbstractManagementConsoleActivity<EventSeriesEventsPlace>
        implements EventSeriesEventsView.Presenter {

    private final EventSeriesEventsView view;

    public EventSeriesEventsActivity(final ManagementConsoleClientFactory clientFactory, final EventSeriesEventsPlace place) {
        super(clientFactory, place);
        this.view = getClientFactory().getViewFactory().getEventSeriesEventsView();
        view.setPresenter(this);
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        final EventSeriesEventsView view = getClientFactory().getViewFactory().getEventSeriesEventsView();
        container.setWidget(view);
    }

    @Override
    public void requestContextMenu(EventMetadataDTO event) {
        view.showContextMenu(event);
    }

    @Override
    public void requestContextMenu(final EventSeriesMetadataDTO eventSeries) {
        view.showContextMenu(eventSeries);
    }

    @Override
    public void navigateToEvent(final EventMetadataDTO event) {
        getClientFactory().getPlaceController().goTo(new RegattaOverviewPlace(event.getId()));
    }

}
