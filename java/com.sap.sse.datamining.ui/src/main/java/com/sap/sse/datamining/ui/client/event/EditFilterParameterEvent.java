package com.sap.sse.datamining.ui.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;

public class EditFilterParameterEvent extends Event<EditFilterParameterEvent.Handler> {
    
    public static final Type<Handler> TYPE = new Type<EditFilterParameterEvent.Handler>();
    
    @FunctionalInterface
    public interface Handler extends EventHandler {
        void onConfigureDimensionParameter(EditFilterParameterEvent event);
    }
    
    private final FilterDimensionParameter parameter;

    public EditFilterParameterEvent(FilterDimensionParameter parameter) {
        this.parameter = parameter;
    }

    public FilterDimensionParameter getParameter() {
        return parameter;
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onConfigureDimensionParameter(this);
    }
    
    public static HandlerRegistration register(EventBus eventBus, Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }

}
