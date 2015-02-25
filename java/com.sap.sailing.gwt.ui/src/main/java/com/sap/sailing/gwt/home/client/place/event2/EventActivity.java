package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabActivity;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.EventView.PlaceCallback;
import com.sap.sailing.gwt.home.client.place.event2.model.EventDTO;
import com.sap.sailing.gwt.home.client.place.event2.model.EventReferenceDTO;
import com.sap.sailing.gwt.home.client.place.event2.model.EventType;
import com.sap.sailing.gwt.home.client.place.event2.model.RegattaDTO;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;
import com.sap.sailing.gwt.home.client.place.event2.tabs.overview.EventRegattaOverviewPlace;

public abstract class EventActivity<PLACE extends AbstractEventPlace> extends AbstractActivity implements EventView.Presenter {

    protected final PLACE currentPlace;

    protected final EventContext ctx;

    protected final EventClientFactory clientFactory;

    public EventActivity(PLACE place, EventClientFactory clientFactory) {
        this.currentPlace = place;
        this.ctx = new EventContext(clientFactory, place.getCtx());

        this.clientFactory = clientFactory;
    }

    @Override
    public EventContext getCtx() {
        return ctx;
    }

    @Override
    public void handleTabPlaceSelection(TabActivity<?, EventContext, ? extends EventView.Presenter> selectedActivity) {
        Place tabPlaceToGo = selectedActivity.placeToFire(ctx);
        clientFactory.getPlaceController().goTo(tabPlaceToGo);
    }
    
    public void navigateTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    @Override
    public void forPlaceSelection(PlaceCallback callback) {
        EventDTO event = ctx.getEventDTO();
        if(event.getType() == EventType.SERIES_EVENT) {
            for(EventReferenceDTO seriesEvent : event.getEventsOfSeries()) {
                EventRegattaOverviewPlace place = new EventRegattaOverviewPlace(seriesEvent.getId().toString(), seriesEvent.getRegattaName());
                callback.forPlace(place, seriesEvent.getName());
            }
        } else {
            for(RegattaDTO regatta : event.getRegattas()) {
                EventRegattaOverviewPlace place = new EventRegattaOverviewPlace(new EventContext(ctx.getEventDTO()).withRegattaId(regatta.getName()));
                callback.forPlace(place, regatta.getName());
            }
        }
    }
    
    @Override
    public String getUrl(Place place) {
        // TODO implement
        return "TODO URL";
    }
    
    @Override
    public String getEventName() {
        return ctx.getEventDTO().getName();
    }
}
