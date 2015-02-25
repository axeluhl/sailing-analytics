package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.controls.tabbar.TabView;
import com.sap.sailing.gwt.home.client.place.event.EventClientFactory;
import com.sap.sailing.gwt.home.client.place.event2.EventView.PlaceCallback;
import com.sap.sailing.gwt.home.client.place.event2.model.EventMetadataDTO;
import com.sap.sailing.gwt.home.client.place.event2.model.EventReferenceDTO;
import com.sap.sailing.gwt.home.client.place.event2.model.EventType;
import com.sap.sailing.gwt.home.client.place.event2.model.RegattaReferenceDTO;
import com.sap.sailing.gwt.home.client.place.event2.regatta.tabs.RegattaOverviewPlace;

public abstract class EventActivity<PLACE extends AbstractEventPlace> extends AbstractActivity implements EventView.Presenter {

    protected final PLACE currentPlace;

    protected final EventContext ctx;

    protected final EventClientFactory clientFactory;

    public EventActivity(PLACE place, EventClientFactory clientFactory) {
        this.currentPlace = place;
        this.ctx = new EventContext(place.getCtx());

        this.clientFactory = clientFactory;
    }

    @Override
    public EventContext getCtx() {
        return ctx;
    }

    @Override
    public void handleTabPlaceSelection(TabView<?, ? extends EventView.Presenter> selectedActivity) {
        Place tabPlaceToGo = selectedActivity.placeToFire();
        clientFactory.getPlaceController().goTo(tabPlaceToGo);
    }
    
    public void navigateTo(Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

    @Override
    public void forPlaceSelection(PlaceCallback callback) {
        EventMetadataDTO event = ctx.getEventDTO();
        if(event.getType() == EventType.SERIES_EVENT) {
            for(EventReferenceDTO seriesEvent : event.getEventsOfSeries()) {
                RegattaOverviewPlace place = new RegattaOverviewPlace(new EventContext().withId(seriesEvent.getId().toString()));
                callback.forPlace(place, seriesEvent.getDisplayName());
            }
        } else {
            for(RegattaReferenceDTO regatta : event.getRegattas()) {
                RegattaOverviewPlace place = new RegattaOverviewPlace(
                        new EventContext(ctx.getEventDTO()).withRegattaId(regatta.getName()));
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
