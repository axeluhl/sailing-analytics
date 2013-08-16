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
import com.sap.sailing.gwt.ui.datamining.QueryComponentsProvider.GrouperType;

public class GPSFixQueryBenchmarkPanel extends AbstractQueryBenchmarkPanel<SharedDimensions.GPSFix> {

    public GPSFixQueryBenchmarkPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryComponentsProvider<SharedDimensions.GPSFix> queryComponentsProvider) {
        super(stringMessages, sailingService, errorReporter, queryComponentsProvider);
    }

    @Override
    protected void sendServerRequest(ClientBenchmarkData<SharedDimensions.GPSFix> benchmarkData, AsyncCallback<QueryResult<Integer>> asyncCallback) {
        Map<GPSFix, Collection<?>> selection = benchmarkData.getSelection();
        StatisticType statisticToCalculate = getQueryComponentsProvider().getStatisticToCalculate();
        AggregatorType aggregatedAs = getQueryComponentsProvider().getAggregatorType();
        
        GrouperType grouperType = getQueryComponentsProvider().getGrouperType();
        switch (grouperType) {
        case Custom:
            String grouperScriptText = getQueryComponentsProvider().getCustomGrouperScriptText();
            getSailingService().runGPSFixQuery(selection, grouperScriptText, statisticToCalculate, aggregatedAs, asyncCallback);
            return;
        case Dimensions:
            Collection<GPSFix> dimensionsToGroupBy = getQueryComponentsProvider().getDimensionsToGroupBy();
            getSailingService().runGPSFixQuery(selection, dimensionsToGroupBy, statisticToCalculate, aggregatedAs, asyncCallback);
            return;
        }
        throw new IllegalArgumentException("Not yet implemented for the given grouper type: " + grouperType);
    }

}
