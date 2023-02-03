package com.sap.sse.datamining.ui.client.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import com.sap.sse.datamining.shared.dto.DataMiningReportParametersDTO;
import com.sap.sse.datamining.shared.dto.FilterDimensionParameter;
import com.sap.sse.datamining.shared.impl.dto.DataRetrieverLevelDTO;
import com.sap.sse.datamining.shared.impl.dto.FunctionDTO;

public class FilterParametersChangedEvent extends Event<FilterParametersChangedEvent.Handler> {
    
    public static final Type<Handler> TYPE = new Type<FilterParametersChangedEvent.Handler>();
    
    @FunctionalInterface
    public interface Handler extends EventHandler {
        void onFilterParameterChange(FilterParametersChangedEvent event);
    }
    
    private final DataMiningReportParametersDTO parameters;
    private final Integer activeIndex;

    public FilterParametersChangedEvent(DataMiningReportParametersDTO parameters, Integer activeIndex) {
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
    
    // FIXME I'm afraid the pair (retrieverLevel, dimension) is not always a unique key. Potential corner cases may occur when @Connector navigation is used.
    // The FunctionDTOs are compared by function name and source type name but not by the path starting at the retriever level's type. If two paths exist to
    // the same source type and then the same function is used through those two separate paths, those dimensions couldn't be discerned here.
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
        handler.onFilterParameterChange(this);
    }
    
    public static HandlerRegistration register(EventBus eventBus, Handler handler) {
        return eventBus.addHandler(TYPE, handler);
    }

}
