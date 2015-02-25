package com.sap.sailing.gwt.home.client.place.event2;

import java.util.Date;
import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.model.EventMetadataDTO;
import com.sap.sailing.gwt.home.client.place.event2.model.EventReferenceDTO;
import com.sap.sailing.gwt.home.client.place.event2.model.EventType;
import com.sap.sailing.gwt.home.client.place.event2.model.RegattaReferenceDTO;
import com.sap.sailing.gwt.home.client.place.event2.model.EventState;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.EventMultiregattaActivity;
import com.sap.sailing.gwt.home.client.place.event2.multiregatta.tabs.MultiregattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event2.regatta.EventRegattaActivity;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaOverviewPlace;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventActivityProxy extends AbstractActivityProxy {

    private final AbstractEventPlace place;
    private EventContext ctx;
    private EventClientFactory clientFactory;

    public EventActivityProxy(AbstractEventPlace place, EventClientFactory clientFactory) {
        this.place = place;
        ctx = this.place.getCtx();
        this.clientFactory = clientFactory;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void startAsync() {
        if (ctx.getEventDTO() != null) {
            afterLoad();
        } else {
            final UUID eventUUID = UUID.fromString(ctx.getEventId());
            
            EventMetadataDTO event = new EventMetadataDTO();
            event.setId(eventUUID);
            event.setName("Kieler Woche");
            event.setVenue("Kiel");
            event.setVenueCountry("Germany");
            event.setStartDate(new Date(115, 6, 10));
            event.setEndDate(new Date(115, 6, 12));
            event.setOfficialWebsiteURL("http://sapsailing.com");
            event.setState(EventState.UPCOMMING);

            // event.setType(EventType.SINGLE_REGATTA);
            event.setType(EventType.MULTI_REGATTA);
//            event.setType(EventType.SERIES_EVENT);

            switch (event.getType()) {
            case SINGLE_REGATTA:
                event.getRegattas().add(new RegattaReferenceDTO("Regatta"));
                break;
            case MULTI_REGATTA:
                event.getRegattas().add(new RegattaReferenceDTO("Regatta 1"));
                event.getRegattas().add(new RegattaReferenceDTO("Regatta 2"));
                event.getRegattas().add(new RegattaReferenceDTO("Regatta 3"));
                break;
            case SERIES_EVENT:
                event.getRegattas().add(new RegattaReferenceDTO("Regatta"));
                event.getEventsOfSeries().add(new EventReferenceDTO(eventUUID, "Series Event 1", "Regatta 1"));
                event.getEventsOfSeries().add(
                        new EventReferenceDTO(UUID.fromString("212385a0-fff7-432a-a63f-df2420faefc4"),
                                "Series Event 2", "Regatta 2"));
                event.getEventsOfSeries().add(
                        new EventReferenceDTO(UUID.fromString("312385a0-fff7-432a-a63f-df2420faefc4"),
                                "Series Event 3", "Regatta 3"));
                break;

            }
            ctx.updateContext(event);
            afterLoad();
            
            
//            clientFactory.getSailingService().getEventById(eventUUID, true, new AsyncCallback<EventDTO>() {
//                @Override
//                public void onSuccess(final EventDTO event) {
//                    if(event != null) {
//                        ctx.updateContext(event);
//                        afterLoad();
//                    } else {
//                        // TODO
////                        createErrorView("No such event with UUID " + eventUUID, null, panel);
//                    }
//                }
//
//                @Override
//                public void onFailure(Throwable caught) {
//                    // TODO
////                    createErrorView("Error while loading the event with service getEventById()", caught, panel);
//                }
//            });

        }
        
        
        
    }

    private void afterLoad() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                final AbstractEventPlace placeToStart;
                if(place instanceof EventDefaultPlace) {
                    placeToStart = getRealPlace();
                } else {
                    placeToStart = place;
                }
                
                if(placeToStart instanceof AbstractEventRegattaPlace) {
                    super.onSuccess(new EventRegattaActivity((AbstractEventRegattaPlace) placeToStart, clientFactory));
                }
                if(placeToStart instanceof AbstractMultiregattaEventPlace) {
                    super.onSuccess(new EventMultiregattaActivity((AbstractMultiregattaEventPlace) placeToStart, clientFactory));
                }
            }

        });
    }
    
    private AbstractEventPlace getRealPlace() {
        EventMetadataDTO event = ctx.getEventDTO();
        if(event.getType() == EventType.SERIES_EVENT) {
            return new RegattaOverviewPlace(ctx);
        }
        if(event.getType() == EventType.SINGLE_REGATTA) {
            return new RegattaOverviewPlace(ctx.withRegattaId(event.getRegattas().get(0).getName()));
        }
        return new MultiregattaOverviewPlace(place.getCtx());
    }
}
