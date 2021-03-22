package com.sap.sailing.gwt.managementconsole.events;

public class EventListRequestEvent extends ListRequestEvent<ListRequestEvent.Handler> {

    public static final Type<Handler> TYPE = new Type<>();

    public EventListRequestEvent() {
        super(TYPE);
    }

}
