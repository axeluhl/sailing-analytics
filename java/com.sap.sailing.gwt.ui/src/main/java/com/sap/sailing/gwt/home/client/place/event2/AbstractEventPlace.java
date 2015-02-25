package com.sap.sailing.gwt.home.client.place.event2;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public abstract class AbstractEventPlace extends Place {
    private final EventContext ctx;

    protected AbstractEventPlace(EventContext ctx) {
        this.ctx = ctx;
    }

    public EventContext getCtx() {
        return ctx;
    }

    public AbstractEventPlace(String eventUuidAsString) {
        this.ctx = new EventContext();
        ctx.withId(eventUuidAsString);
    }

    public String getTitle(String eventName) {
        return TextMessages.INSTANCE.sapSailing() + " - " + eventName;
    }

    public String getEventUuidAsString() {
        return ctx.getEventId();
    }
    
    public String getRegattaId() {
        return ctx.getRegattaId();
    }
}
