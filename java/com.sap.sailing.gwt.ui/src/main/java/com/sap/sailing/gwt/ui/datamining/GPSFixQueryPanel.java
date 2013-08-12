package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.datamining.shared.SharedDimensions;
import com.sap.sailing.datamining.shared.SharedDimensions.GPSFix;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class GPSFixQueryPanel extends AbstractQueryPanel<SharedDimensions.GPSFix> {

    public GPSFixQueryPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryComponentsProvider<SharedDimensions.GPSFix> queryComponentsProvider) {
        super(stringMessages, sailingService, errorReporter, queryComponentsProvider);
    }

    @Override
    protected void sendServerRequest(AsyncCallback<QueryResult<Integer>> asyncCallback) {
        Map<SharedDimensions.GPSFix, Collection<?>> selection = getQueryComponentsProvider().getSelection();
        Collection<GPSFix> dimensionsToGroupBy = getQueryComponentsProvider().getDimensionsToGroupBy();
        StatisticType statisticToCalculate = getQueryComponentsProvider().getStatisticToCalculate();
        AggregatorType aggregatedAs = getQueryComponentsProvider().getAggregationType();
        getSailingService().runGPSFixQuery(selection, dimensionsToGroupBy, statisticToCalculate, aggregatedAs, asyncCallback);
    }

}
