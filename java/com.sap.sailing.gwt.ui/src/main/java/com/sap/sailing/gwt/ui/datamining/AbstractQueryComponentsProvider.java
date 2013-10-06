package com.sap.sailing.gwt.ui.datamining;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractQueryComponentsProvider<DimensionType> extends FlowPanel implements
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
    public boolean areComponentsValid() {
        return isGrouperValid() && isStatisticToCalculateValid() && isAggregatorValid();
    }

    private boolean isAggregatorValid() {
        return getAggregatorType() != null;
    }

    private boolean isStatisticToCalculateValid() {
        return getStatisticToCalculate() != null;
    }

    private boolean isGrouperValid() {
        ValidateGrouper: switch (getGrouperType()) {
        case Custom:
            if (getCustomGrouperScriptText().isEmpty()) {
                return false;
            }
            break;
        case Dimensions:
            for (DimensionType dimension : getDimensionsToGroupBy()) {
                if (dimension != null) {
                    break ValidateGrouper;
                }
            }
            return false;
        default:
            return false;
        }

        return true;
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