package com.sap.sailing.gwt.home.client.place.fakeseries;

import com.google.gwt.place.shared.Place;
import com.sap.sailing.gwt.common.client.i18n.TextMessages;

public abstract class AbstractSeriesPlace extends Place {
    private final SeriesContext ctx;

    protected AbstractSeriesPlace(SeriesContext ctx) {
        this.ctx = ctx;
    }

    public SeriesContext getCtx() {
        return ctx;
    }

    public AbstractSeriesPlace(String eventUuidAsString) {
        this.ctx = new SeriesContext();
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
