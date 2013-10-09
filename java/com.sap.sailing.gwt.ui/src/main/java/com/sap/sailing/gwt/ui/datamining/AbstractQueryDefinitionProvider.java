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
import com.sap.sailing.datamining.shared.SharedDimensions;

public abstract class AbstractQueryDefinitionProvider implements QueryDefinitionProvider {

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private final Set<QueryDefinitionChangedListener> listeners;

    public AbstractQueryDefinitionProvider(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;

        listeners = new HashSet<QueryDefinitionChangedListener>();
    }
    
    @Override
    public Iterable<String> validateQueryDefinition(QueryDefinition queryDefinition) {
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

    private String validateGrouper(QueryDefinition queryDefinition) {
        GrouperType grouper = queryDefinition.getGrouperType();
        if (grouper == null) {
            return stringMessages.noGrouperSelectedError();
        }
        
        ValidateGrouper: switch (grouper) {
        case Custom:
            if (queryDefinition.getCustomGrouperScriptText().isEmpty()) {
                return stringMessages.noCustomGrouperScriptTextError();
            }
            break;
        case Dimensions:
            for (SharedDimensions dimension : queryDefinition.getDimensionsToGroupBy()) {
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

    private String validateStatisticAndAggregator(QueryDefinition queryDefinition) {
        return queryDefinition.getStatisticType() == null || queryDefinition.getAggregatorType() == null ? stringMessages.noStatisticSelectedError() : null;
    }

    @Override
    public void addListener(QueryDefinitionChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeListener(QueryDefinitionChangedListener listener) {
        listeners.remove(listener);
    }

    protected void notifyQueryDefinitionChanged() {
        for (QueryDefinitionChangedListener listener : listeners) {
            listener.queryDefinitionChanged(getQueryDefinition());
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