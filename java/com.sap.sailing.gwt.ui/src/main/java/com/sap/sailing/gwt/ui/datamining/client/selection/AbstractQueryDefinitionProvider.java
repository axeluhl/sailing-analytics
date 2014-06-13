package com.sap.sailing.gwt.ui.datamining.client.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.datamining.shared.QueryDefinitionDeprecated;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.client.QueryDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.client.QueryDefinitionProvider;
import com.sap.sse.datamining.shared.components.GrouperType;

public abstract class AbstractQueryDefinitionProvider implements QueryDefinitionProvider {

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;

    private boolean blockChangeNotification;
    private final Set<QueryDefinitionChangedListener> listeners;

    public AbstractQueryDefinitionProvider(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;

        blockChangeNotification = false;
        listeners = new HashSet<QueryDefinitionChangedListener>();
    }
    
    @Override
    public Iterable<String> validateQueryDefinition(QueryDefinitionDeprecated queryDefinition) {
        Collection<String> errorMessages = new ArrayList<String>();
        
        if (queryDefinition != null) {
            String grouperError = validateGrouper(queryDefinition);
            if (grouperError != null && !grouperError.isEmpty()) {
                errorMessages.add(grouperError);
            }
            String statisticError = validateStatisticAndAggregator(queryDefinition);
            if (statisticError != null && !statisticError.isEmpty()) {
                errorMessages.add(statisticError);
            }
        }
        
        return errorMessages;
    }

    private String validateGrouper(QueryDefinitionDeprecated queryDefinition) {
        GrouperType grouper = queryDefinition.getGrouperType();
        if (grouper == null) {
            return stringMessages.noGrouperSelectedError();
        }
        
        ValidateGrouper: switch (grouper) {
        case Dimensions:
            for (DimensionIdentifier dimension : queryDefinition.getDimensionsToGroupBy()) {
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

    private String validateStatisticAndAggregator(QueryDefinitionDeprecated queryDefinition) {
        return queryDefinition.getStatisticType() == null || queryDefinition.getAggregatorType() == null ? stringMessages.noStatisticSelectedError() : null;
    }

    @Override
    public void addQueryDefinitionChangedListener(QueryDefinitionChangedListener listener) {
        listeners.add(listener);
    }

    @Override
    public void removeQueryDefinitionChangedListener(QueryDefinitionChangedListener listener) {
        listeners.remove(listener);
    }

    protected void setBlockChangeNotification(boolean block) {
        blockChangeNotification = block;
    }

    protected void notifyQueryDefinitionChanged() {
        if (!blockChangeNotification) {
            QueryDefinitionDeprecated queryDefinition = getQueryDefinition();
            for (QueryDefinitionChangedListener listener : listeners) {
                listener.queryDefinitionChanged(queryDefinition);
            }
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