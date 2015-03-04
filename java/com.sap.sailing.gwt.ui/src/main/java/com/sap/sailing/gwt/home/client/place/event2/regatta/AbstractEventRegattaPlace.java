package com.sap.sailing.gwt.home.client.place.event2.regatta;

import com.sap.sailing.gwt.home.client.place.event2.AbstractEventPlace;
import com.sap.sailing.gwt.home.client.place.event2.EventContext;

public abstract class AbstractEventRegattaPlace extends AbstractEventPlace {

    public AbstractEventRegattaPlace(EventContext ctx) {
        super(ctx);
    }

    public AbstractEventRegattaPlace(String eventUuidAsString, String regattaId) {
        super(eventUuidAsString);
        getCtx().withRegattaId(regattaId);
    }
    
    public abstract AbstractEventRegattaPlace newInstanceWithContext(EventContext ctx);
}
