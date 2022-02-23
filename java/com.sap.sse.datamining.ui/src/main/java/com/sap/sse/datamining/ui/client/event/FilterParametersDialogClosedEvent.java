package com.sap.sse.datamining.ui.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sap.sse.datamining.shared.dto.DataMiningReportParametersDTO;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class FilterParametersDialogClosedEvent extends Event<FilterParametersDialogClosedEvent.Handler> {
    
    public static final Type<Handler> TYPE = new Type<FilterParametersDialogClosedEvent.Handler>();
    
    @FunctionalInterface
    public interface Handler extends EventHandler {
        void onFilterParametersDialogClosed(FilterParametersDialogClosedEvent event);
    }
    
    private final DataMiningReportParametersDTO parameters;
    private final Integer activeIndex;

    public FilterParametersDialogClosedEvent(DataMiningReportParametersDTO parameters, Integer activeIndex) {
        super();
        this.parameters = parameters;
        this.activeIndex = activeIndex;
    }
    
    public DataMiningReportParametersDTO getParameters() {
        return parameters;
    }
    
    public Integer getActiveIndex() {
        return activeIndex;
    }
    
    public FilterDimensionParameter getParameter(DataRetrieverLevelDTO retrieverLevel, FunctionDTO dimension) {
        return parameters.getUsages(activeIndex).stream()
                .filter(p -> p.getRetrieverLevel().equals(retrieverLevel) && p.getDimension().equals(dimension))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Type<Handler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(Handler handler) {
        handler.onFilterParametersDialogClosed(this);
    }
    
    public static HandlerRegistration register(EventBus eventBus, Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }

}
