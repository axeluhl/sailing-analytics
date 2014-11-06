package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.QueryDefinitionChangedListener;
import com.sap.sailing.gwt.ui.datamining.QueryDefinitionProvider;
import com.sap.sse.datamining.shared.QueryDefinition;
import com.sap.sse.datamining.shared.dto.FunctionDTO;
import com.sap.sse.gwt.client.ErrorReporter;

public abstract class AbstractQueryDefinitionProvider implements QueryDefinitionProvider {

    private final StringMessages stringMessages;
    private final SailingServiceAsync sailingService;
    private final DataMiningServiceAsync dataMiningService;
    private final ErrorReporter errorReporter;

    private boolean blockChangeNotification;
    private final Set<QueryDefinitionChangedListener> listeners;

    public AbstractQueryDefinitionProvider(StringMessages stringMessages, SailingServiceAsync sailingService,
            DataMiningServiceAsync dataMiningService, ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;

        blockChangeNotification = false;
        listeners = new HashSet<QueryDefinitionChangedListener>();
    }
    
    @Override
    public Iterable<String> validateQueryDefinition(QueryDefinition queryDefinition) {
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

    private String validateGrouper(QueryDefinition queryDefinition) {
        for (FunctionDTO dimension : queryDefinition.getDimensionsToGroupBy()) {
            if (dimension != null) {
                return null;
            }
        }
        return stringMessages.noDimensionToGroupBySelectedError();
    }

    private String validateStatisticAndAggregator(QueryDefinition queryDefinition) {
        return queryDefinition.getStatisticToCalculate() == null || queryDefinition.getAggregatorType() == null ? stringMessages.noStatisticSelectedError() : null;
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
            QueryDefinition queryDefinition = getQueryDefinition();
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
    
    protected DataMiningServiceAsync getDataMiningService() {
        return dataMiningService;
    }

    protected ErrorReporter getErrorReporter() {
        return errorReporter;
    }

}