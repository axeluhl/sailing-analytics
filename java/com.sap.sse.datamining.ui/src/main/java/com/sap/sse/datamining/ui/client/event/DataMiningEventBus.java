package com.sap.sse.datamining.ui.client.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.Event.Type;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

public final class DataMiningEventBus {
    
    private final static EventBus eventBus = GWT.create(SimpleEventBus.class);
    
    private DataMiningEventBus() {}
    
    public static EventBus get() {
        return eventBus;
    }
    
    public static <H extends EventHandler> HandlerRegistration addHandler(Type<H> type, H handler) {
        return eventBus.addHandler(type, handler);
    }
    
    public static void fire(Event<?> event) {
        eventBus.fireEvent(event);
    }
    
    public static void fire(Event<?> event, Object source) {
        eventBus.fireEventFromSource(event, source);
    }

}
