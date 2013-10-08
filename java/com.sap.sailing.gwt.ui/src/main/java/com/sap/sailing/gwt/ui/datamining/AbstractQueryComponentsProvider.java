package com.sap.sailing.gwt.ui.datamining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.datamining.shared.Components.GrouperType;
import com.sap.sailing.datamining.shared.QueryDefinition;

public abstract class AbstractQueryComponentsProvider<DimensionType> implements
        QueryComponentsProvider<DimensionType> {

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private final Set<QueryComponentsChangedListener<DimensionType>> listeners;

    public AbstractQueryComponentsProvider(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;

        listeners = new HashSet<QueryComponentsChangedListener<DimensionType>>();
    }
    
    @Override
    public Iterable<String> validateQueryDefinition(QueryDefinition<DimensionType> queryDefinition) {
        Collection<String> errorMessages = new ArrayList<String>();
        
        String grouperError = validateGrouper(queryDefinition);
        if (grouperError != null && !grouperError.isEmpty()) {
            errorMessages.add(grouperError);
        }
        
        String statisticError = validateStatisticAndAggregator(queryDefinition);
        if (statisticError != null && !statisticError.isEmpty()) {
            errorMessages.add(statisticError);
        }
        
        return errorMessages;
    }

    private String validateGrouper(QueryDefinition<DimensionType> queryDTO) {
        GrouperType grouper = queryDTO.getGrouperType();
        if (grouper == null) {
            return stringMessages.noGrouperSelectedError();
        }
        
        ValidateGrouper: switch (grouper) {
        case Custom:
            if (queryDTO.getCustomGrouperScriptText().isEmpty()) {
                return stringMessages.noCustomGrouperScriptTextError();
            }
            break;
        case Dimensions:
            for (DimensionType dimension : queryDTO.getDimensionsToGroupBy()) {
                if (dimension != null) {
                    break ValidateGrouper;
                }
            }
            return stringMessages.noDimensionToGroupBySelectedError();
        default:
            return stringMessages.noGrouperSelectedError();
        }

        return null;
    }

    private String validateStatisticAndAggregator(QueryDefinition<DimensionType> queryDTO) {
        return queryDTO.getStatisticType() == null || queryDTO.getAggregatorType() == null ? stringMessages.noStatisticSelectedError() : null;
    }

    @Override
    public void addListener(QueryComponentsChangedListener<DimensionType> listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(QueryComponentsChangedListener<DimensionType> listener) {
        listeners.remove(listener);
    }

    protected void notifyQueryComponentsChanged() {
        for (QueryComponentsChangedListener<DimensionType> listener : listeners) {
            listener.queryComponentsChanged(this);
        }
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }

    protected ErrorReporter getErrorReporter() {
        return errorReporter;
    }

}