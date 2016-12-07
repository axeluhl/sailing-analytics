package com.sap.sailing.gwt.home.desktop.places.event.regatta;

import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

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
