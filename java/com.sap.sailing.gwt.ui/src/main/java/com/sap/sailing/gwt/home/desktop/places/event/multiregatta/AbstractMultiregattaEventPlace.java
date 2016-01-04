package com.sap.sailing.gwt.home.desktop.places.event.multiregatta;

import com.sap.sailing.gwt.home.shared.places.event.AbstractEventPlace;
import com.sap.sailing.gwt.home.shared.places.event.EventContext;

public abstract class AbstractMultiregattaEventPlace extends AbstractEventPlace {

    public AbstractMultiregattaEventPlace(EventContext ctx) {
        super(ctx);
    }

    public AbstractMultiregattaEventPlace(String eventUuidAsString) {
        super(eventUuidAsString);
    }
}
