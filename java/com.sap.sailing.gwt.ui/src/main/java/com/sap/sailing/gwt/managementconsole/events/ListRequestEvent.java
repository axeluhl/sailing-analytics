package com.sap.sailing.gwt.managementconsole.events;

import com.google.gwt.event.shared.EventHandler;

abstract class ListRequestEvent<H extends ListRequestEvent.Handler> extends TypedGwtEvent<H> {

    interface Handler extends EventHandler {
        void onListRequest();
    }

    protected ListRequestEvent(final Type<H> type) {
        super(type);
    }

    @Override
    protected void dispatch(final H handler) {
        handler.onListRequest();
    }

}
