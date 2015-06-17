package com.sap.sailing.gwt.home.mobile;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.web.bindery.event.shared.EventBus;
import com.sap.sse.gwt.client.mvp.PlaceChangedEvent;

// FIXME: Copy of com.sap.sse.gwt.client.mvp.impl.CustomActivityManager -> remove duplication
public class CustomActivityManager extends ActivityManager {

    private EventBus eventBus;

    public CustomActivityManager(ActivityMapper mapper, EventBus eventBus) {
        super(mapper, eventBus);
        this.eventBus = eventBus;
    }
    
    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        super.onPlaceChange(event);
        eventBus.fireEvent(new PlaceChangedEvent(event.getNewPlace()));
    }

}
