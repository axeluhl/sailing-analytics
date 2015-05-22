package com.sap.sailing.gwt.regattaoverview.client;

import java.util.Date;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.i18n.client.DateTimeFormat;

public class EventTimeUpdateEvent extends GwtEvent<EventTimeUpdateEvent.Handler> {

    public static final Type<Handler> TYPE = new Type<EventTimeUpdateEvent.Handler>();
    private final DateTimeFormat timeFormatter = DateTimeFormat.getFormat("HH:mm:ss");

    public interface Handler extends EventHandler {
        void onEventDTOLoaded(EventTimeUpdateEvent e);
    }

    private final Date updatedTime;

    public EventTimeUpdateEvent(Date updatedTime) {
        super();
        this.updatedTime = updatedTime;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public String getFormattedUpdatedTime() {
        return timeFormatter.format(updatedTime);

    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onEventDTOLoaded(this);
    }

}
