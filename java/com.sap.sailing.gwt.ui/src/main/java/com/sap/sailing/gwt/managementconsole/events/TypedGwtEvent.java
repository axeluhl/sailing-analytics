package com.sap.sailing.gwt.managementconsole.events;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

abstract class TypedGwtEvent<H extends EventHandler> extends GwtEvent<H> {

    private final Type<H> type;

    protected TypedGwtEvent(final Type<H> type) {
        this.type = type;
    }

    @Override
    public final Type<H> getAssociatedType() {
        return type;
    }

}
