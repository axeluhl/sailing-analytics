package com.sap.sse.gwt.client.mvp;

import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.google.web.bindery.event.shared.EventBus;

public class CustomActivityManager extends DelegatingActivityManager {

    private EventBus eventBus;

    public CustomActivityManager(ActivityMapper mapper, EventBus eventBus) {
        super(mapper, eventBus);
        this.eventBus = eventBus;
    }
    
    @Override
    protected void afterPlaceActivation(Place place) {
        super.afterPlaceActivation(place);
        eventBus.fireEvent(new PlaceChangedEvent(place));
    }
}
