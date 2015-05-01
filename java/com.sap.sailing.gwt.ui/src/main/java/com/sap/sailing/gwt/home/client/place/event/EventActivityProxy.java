package com.sap.sailing.gwt.home.client.place.event;

import java.util.UUID;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.gwt.home.client.app.HomePlacesNavigator;
import com.sap.sailing.gwt.home.client.place.error.ErrorPlace;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.AbstractMultiregattaEventPlace;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.EventMultiregattaActivity;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs.MultiregattaMediaPlace;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs.MultiregattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event.multiregatta.tabs.MultiregattaRegattasPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.AbstractEventRegattaPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.EventRegattaActivity;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaLeaderboardPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaMediaPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaOverviewPlace;
import com.sap.sailing.gwt.home.client.place.event.regatta.tabs.RegattaRacesPlace;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO;
import com.sap.sailing.gwt.ui.shared.eventview.EventViewDTO.EventType;
import com.sap.sse.gwt.client.mvp.AbstractActivityProxy;

public class EventActivityProxy extends AbstractActivityProxy {

    private AbstractEventPlace place;
    private EventContext ctx;
    private EventClientFactory clientFactory;
    private HomePlacesNavigator homePlacesNavigator;

    public EventActivityProxy(AbstractEventPlace place, EventClientFactory clientFactory,
            HomePlacesNavigator homePlacesNavigator) {
        this.place = place;
        this.homePlacesNavigator = homePlacesNavigator;
        this.ctx = this.place.getCtx();
        this.clientFactory = clientFactory;
    }

    @Override
    protected void startAsync() {
        if (ctx.getEventDTO() != null) {
            afterEventLoad();
        } else {
            final UUID eventUUID = UUID.fromString(ctx.getEventId());
            
            clientFactory.getHomeService().getEventViewById(eventUUID, new AsyncCallback<EventViewDTO>() {
                @Override
                public void onSuccess(final EventViewDTO event) {
                    ctx.updateContext(event);
                    afterEventLoad();
                }

                @Override
                public void onFailure(Throwable caught) {
                    // TODO @FM: extract text?
                    ErrorPlace errorPlace = new ErrorPlace("Error while loading the event with service getEventViewById()");
                    // TODO @FM: reload sinnvoll hier?
                    errorPlace.setComingFrom(place);
                    clientFactory.getPlaceController().goTo(errorPlace);
                }
            });

        }
    }

    private void afterEventLoad() {
        if(place instanceof EventDefaultPlace) {
            place = getRealPlace();
        }
        place = verifyAndAdjustPlace();
        afterLoad();
    }

    private void afterLoad() {
        GWT.runAsync(new AbstractRunAsyncCallback() {
            @Override
            public void onSuccess() {
                if(place instanceof AbstractEventRegattaPlace) {
                    super.onSuccess(new EventRegattaActivity((AbstractEventRegattaPlace) place, clientFactory,
                            homePlacesNavigator));
                }
                if(place instanceof AbstractMultiregattaEventPlace) {
                    super.onSuccess(new EventMultiregattaActivity((AbstractMultiregattaEventPlace) place,
                            clientFactory, homePlacesNavigator));
                }
            }
        });
    }

    private AbstractEventPlace getRealPlace() {
        EventViewDTO event = ctx.getEventDTO();

        if(event.getType() == EventType.SERIES_EVENT || event.getType() == EventType.SINGLE_REGATTA) {
            if (event.isFinished()) {
                return new RegattaLeaderboardPlace(new EventContext(ctx).withRegattaId(null));
            } else {
                return new RegattaOverviewPlace(new EventContext(ctx).withRegattaId(null));
            }
        }
        return new MultiregattaOverviewPlace(place.getCtx());
    }
    
    /**
     * Checks if the place is valid for the given event.
     * If not, the place is automatically being adjusted.
     */
    private AbstractEventPlace verifyAndAdjustPlace() {
        EventContext contextWithoutRegatta = new EventContext(ctx).withRegattaId(null);
        // TODO check if regatta ID is valid
        if(place instanceof AbstractMultiregattaEventPlace && ctx.getEventDTO().getType() != EventType.MULTI_REGATTA) {
            // Events with a type other than multi regatta only have regatta level pages
            if(place instanceof MultiregattaRegattasPlace) {
                return new RegattaRacesPlace(contextWithoutRegatta);
            }
            if(place instanceof MultiregattaMediaPlace) {
                return new RegattaMediaPlace(contextWithoutRegatta);
            }
            return new RegattaOverviewPlace(contextWithoutRegatta);
        }
        
        if(place instanceof AbstractEventRegattaPlace) {
            boolean regattaKnown = ctx.getEventDTO().isRegattaIDKnown(ctx.getRegattaId());
            if(ctx.getEventDTO().getType() != EventType.MULTI_REGATTA && ctx.getRegattaId() != null && !regattaKnown) {
                // Regatta ID unknown but unnecessary ...
                ctx.withRegattaId(null);
            } else if(ctx.getEventDTO().getType() == EventType.MULTI_REGATTA && !regattaKnown) {
                return new MultiregattaRegattasPlace(contextWithoutRegatta);
            }
        }
        
        if(place instanceof RegattaMediaPlace && ctx.getEventDTO().getType() == EventType.MULTI_REGATTA) {
            // The media page for multi regatta events is on event level only but not on regatta level
            return new MultiregattaMediaPlace(new EventContext(ctx).withRegattaId(null));
        }
        // no adjustment necessary
        return place;
    }
}
