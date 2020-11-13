package com.sap.sailing.gwt.managementconsole.places.event.overview;

import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.sap.sailing.gwt.managementconsole.app.ManagementConsoleClientFactory;
import com.sap.sailing.gwt.managementconsole.events.EventUpdateEvent;
import com.sap.sailing.gwt.managementconsole.events.ManagementConsoleEventHandler;
import com.sap.sailing.gwt.managementconsole.places.AbstractManagementConsoleActivity;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.EventDTO;

public class EventOverviewActivity extends AbstractManagementConsoleActivity<EventOverviewPlace> {
    
    private final Logger logger = Logger.getLogger(EventOverviewActivity.class.getName());

    public EventOverviewActivity(final ManagementConsoleClientFactory clientFactory, final EventOverviewPlace place) {
        super(clientFactory, place);
    }

    @Override
    public void start(final AcceptsOneWidget container, final EventBus eventBus) {
        logger.info("start");
        EventOverviewView view = new EventOverviewView();
        container.setWidget(view);
        eventBus.addHandler(EventUpdateEvent.TYPE, new ManagementConsoleEventHandler() {
            
            @Override
            public void onEvent(GwtEvent<? extends EventHandler> event) {
                logger.info("onEvent");
                if (event.getSource() instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<EventDTO> events = (List<EventDTO>) event.getSource();
                    view.renderEvents(events);
                    for (EventDTO singleEvent: events) {
                        logger.info(singleEvent.getName() 
                                + " - " + singleEvent.venue.getName() 
                                + " - " + DateAndTimeFormatterUtil.formatDateRange(singleEvent.startDate, singleEvent.endDate));
                    }
                }
            }
        });
        getClientFactory().getSailingService().getEvents(new AsyncCallback<List<EventDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                logger.severe("Cannot load events!");
                getClientFactory().getErrorReporter().reportError("Error", "Cannot load events!");
            }

            @Override
            public void onSuccess(List<EventDTO> result) {
                logger.info("onSuccess");
                eventBus.fireEventFromSource(new EventUpdateEvent(), result);
            }
        });
    }

}
