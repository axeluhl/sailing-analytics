package com.sap.sse.datamining.ui.client.event;

import java.io.Serializable;
import java.util.HashSet;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class ConfigureDimensionParameterEvent extends Event<ConfigureDimensionParameterEvent.Handler> {
    
    public static final Type<Handler> TYPE = new Type<ConfigureDimensionParameterEvent.Handler>();
    
    @FunctionalInterface
    public interface Handler extends EventHandler {
        void onConfigureDimensionParameter(ConfigureDimensionParameterEvent event);
    }
    
    private final DataRetrieverLevelDTO retrieverLevel;
    private final FunctionDTO dimension;
    private final HashSet<? extends Serializable> selectedValues;

    public ConfigureDimensionParameterEvent(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension,
            HashSet<? extends Serializable> selectedValues) {
        this.retrieverLevel = retrieverLevel;
        this.dimension = dimension;
        this.selectedValues = selectedValues;
    }

    public DataRetrieverLevelDTO getRetrieverLevel() {
        return retrieverLevel;
    }

    public FunctionDTO getDimension() {
        return dimension;
    }

    public HashSet<? extends Serializable> getSelectedValues() {
        return selectedValues;
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
