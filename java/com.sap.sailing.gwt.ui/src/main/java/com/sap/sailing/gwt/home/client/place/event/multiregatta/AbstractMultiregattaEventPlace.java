package com.sap.sailing.gwt.home.client.place.event.multiregatta;

import com.sap.sailing.gwt.home.client.place.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event.EventContext;

public abstract class AbstractMultiregattaEventPlace extends AbstractEventPlace {

    public AbstractMultiregattaEventPlace(EventContext ctx) {
        super(ctx);
    }

    public AbstractMultiregattaEventPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }
}
