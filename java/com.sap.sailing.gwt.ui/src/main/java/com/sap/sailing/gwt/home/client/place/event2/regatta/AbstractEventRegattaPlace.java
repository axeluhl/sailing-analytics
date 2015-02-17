package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public abstract class AbstractEventRegattaPlace extends AbstractEventPlace {

    public AbstractEventRegattaPlace(EventContext ctx) {
        super(ctx);
    }

    public AbstractEventRegattaPlace(String eventUuidAsString, String regattaUuidAsString) {
        super(eventUuidAsString);
        getCtx().withRegattaId(regattaUuidAsString);
    }

}
