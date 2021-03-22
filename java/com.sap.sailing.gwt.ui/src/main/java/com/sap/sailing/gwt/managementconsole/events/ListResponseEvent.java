package com.sap.sailing.gwt.managementconsole.events;

import java.util.List;

import com.google.gwt.event.shared.EventHandler;

public abstract class ListResponseEvent<T, H extends ListResponseEvent.Handler<T>> extends TypedGwtEvent<H> {

    public interface Handler<T> extends EventHandler {
        void onListResponse(List<T> list);
    }

    private final List<T> list;

    protected ListResponseEvent(final Type<H> type, final List<T> list) {
        super(type);
        this.list = list;
    }

    @Override
    protected void dispatch(final H handler) {
        handler.onListResponse(list);
    }

}
