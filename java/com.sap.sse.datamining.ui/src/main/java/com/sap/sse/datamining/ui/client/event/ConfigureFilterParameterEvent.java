package com.sap.sse.datamining.ui.client.event;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class ConfigureFilterParameterEvent extends Event<ConfigureFilterParameterEvent.Handler> {
    
    public static final Type<Handler> TYPE = new Type<ConfigureFilterParameterEvent.Handler>();
    
    @FunctionalInterface
    public interface Handler extends EventHandler {
        void onConfigureDimensionParameter(ConfigureFilterParameterEvent event);
    }
    
    private final DataRetrieverLevelDTO retrieverLevel;
    private final FunctionDTO dimension;
    private final Set<? extends Serializable> selectedValues;

    public ConfigureFilterParameterEvent(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension, Set<? extends Serializable> selectedValues) {
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

    public Set<? extends Serializable> getSelectedValues() {
        return selectedValues != null ? selectedValues : Collections.emptySet();
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
