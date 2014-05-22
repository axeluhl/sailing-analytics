package com.sap.sse.gwt.client.mvp;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

public interface ClientFactory {
    EventBus getEventBus();

    PlaceController getPlaceController();
}
