package com.sap.sailing.gwt.home.client.place.event2;

import com.sap.sailing.gwt.common.client.AbstractBasePlace;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;
import com.sap.sailing.gwt.home.client.place.event2.tabs.EventContext;

public abstract class EventPlace extends AbstractBasePlace {
    private final EventContext ctx;

    protected EventPlace(EventContext ctx) {
        this.ctx = ctx;
    }

    public EventContext getCtx() {
        return ctx;
    }

    public EventPlace(String eventUuidAsString) {
        this.ctx = new EventContext();
        ctx.withId(eventUuidAsString);
    }

    public String getTitle(String eventName) {
        return TextMessages.INSTANCE.sapSailing() + " - " + eventName;
    }

    public String getEventUuidAsString() {
        return ctx.getEventId();
    }

    public String getLeaderboardIdAsNameString() {
        return ctx.getLeaderboardIdAsNameString();
    }

}
