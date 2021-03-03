package com.sap.sailing.gwt.managementconsole.services;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sailing.gwt.managementconsole.events.EventListResponseEvent;
import com.sap.sailing.gwt.managementconsole.events.EventListUpdateEvent;
import com.sap.sailing.gwt.ui.client.SailingServiceWriteAsync;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.media.ImageDTO;
import com.sap.sse.gwt.client.media.VideoDTO;

public class EventService {
    
    private static final Logger LOG = Logger.getLogger(EventService.class.getName());
    
    private final SailingServiceWriteAsync sailingService;
    private final ErrorReporter errorReporter;
    private final EventBus eventBus;
    
    private Map<String, EventDTO> eventMap;
    
    public EventService(final SailingServiceWriteAsync sailingService,
            final ErrorReporter errorReporter,
            final EventBus eventBus) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.eventBus = eventBus;
        this.eventMap = new HashMap<String, EventDTO>();
    }
    
    public List<EventDTO> getEvents() {
        return new ArrayList<EventDTO>(eventMap.values());
    }
    
    public void updateEvents(final List<EventDTO> events) {
        setEvents(events);
    }
    
    public void requestEventList(boolean forceRequestFromService) {
        if (forceRequestFromService || eventMap.isEmpty()) {
            sailingService.getEvents(new AsyncCallback<List<EventDTO>>() {
                @Override
                public void onFailure(Throwable caught) {
                    LOG.severe("requestEventList :: Cannot load events!");
                    errorReporter.reportError("Error", "Cannot load events!");
                }
    
                @Override
                public void onSuccess(List<EventDTO> result) {
                    LOG.info("requestEventList :: onSuccess");
                    setEvents(result);
                    eventBus.fireEvent(new EventListResponseEvent(result));
                }
            });
        } else {
            eventBus.fireEvent(new EventListUpdateEvent(getEvents()));
        }
    }
    
    private void setEvents(final List<EventDTO> eventList) {
        this.eventMap = eventList.stream().collect(Collectors.toMap(event -> event.id.toString(),  Function.identity()));
    }
    
    public void createEvent(String name, String venue, Date date, List<String> courseAreaNames, AsyncCallback<EventDTO> callback) {
        sailingService.createEvent(name, null, date, null, venue, false, courseAreaNames, null, null, new HashMap<String, String>(), new ArrayList<ImageDTO>(), new ArrayList<VideoDTO>(), new ArrayList<UUID>(), callback); 
    }
}
