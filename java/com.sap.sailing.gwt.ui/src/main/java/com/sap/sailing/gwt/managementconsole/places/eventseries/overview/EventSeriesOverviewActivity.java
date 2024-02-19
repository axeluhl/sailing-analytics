package com.sap.sailing.gwt.managementconsole.places.eventseries.overview;

import java.util.UUID;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.common.communication.event.EventSeriesMetadataDTO;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.events.EventSeriesListResponseEvent;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.managementconsole.resources.ManagementConsoleResources;

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
            if (list.size() == 0) {
                EventSeriesMetadataDTO es = new EventSeriesMetadataDTO("Test Event Servies", UUID.randomUUID());
                list.add(es);
            }
            view.renderEventSeries(list);
            container.setWidget(view);
        });
        getClientFactory().getEventService().requestEventSeriesList(/* forceRequestFromService */ false);
    }

    @Override
    public void reloadEventSeriesList() {
        getClientFactory().getEventService().requestEventSeriesList(/* forceRequestFromService */ true);
    }

    @Override
    public void navigateToCreateEventSeries() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void navigateToEventSeries(EventSeriesMetadataDTO eventSeries) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void advancedSettings(ManagementConsoleResources app_res, EventSeriesMetadataDTO eventSeries) {
        view.showContextMenu(eventSeries);
    }

    @Override
    public void deleteEventSeries(EventSeriesMetadataDTO eventSeries) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void requestContextMenu(EventSeriesMetadataDTO item) {
        // TODO Auto-generated method stub
        
    }

}
