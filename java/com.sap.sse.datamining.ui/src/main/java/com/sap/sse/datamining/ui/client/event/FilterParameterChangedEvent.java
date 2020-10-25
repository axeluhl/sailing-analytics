package com.sap.sse.datamining.ui.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sap.sse.datamining.ui.client.parameterization.ParameterizedFilterDimension;

public class FilterParameterChangedEvent extends Event<FilterParameterChangedEvent.Handler> {
    
    public static final Type<Handler> TYPE = new Type<FilterParameterChangedEvent.Handler>();
    
    @FunctionalInterface
    public interface Handler extends EventHandler {
        void onFilterParameterChenge(FilterParameterChangedEvent event);
    }
    
    private final ParameterizedFilterDimension parameter;

    public FilterParameterChangedEvent(ParameterizedFilterDimension parameter) {
        this.parameter = parameter;
    }

    public ParameterizedFilterDimension getParameter() {
        return parameter;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onFilterParameterChenge(this);
    }
    
    public static HandlerRegistration register(EventBus eventBus, Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }

}
