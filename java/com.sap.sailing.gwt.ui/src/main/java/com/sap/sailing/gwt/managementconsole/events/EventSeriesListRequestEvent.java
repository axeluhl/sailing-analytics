package com.sap.sailing.gwt.managementconsole.events;

public class EventSeriesListRequestEvent extends ListRequestEvent<ListRequestEvent.Handler> {

    public static final Type<Handler> TYPE = new Type<>();

    public EventSeriesListRequestEvent() {
        super(TYPE);
    }

}
