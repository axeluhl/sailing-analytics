package com.sap.sailing.gwt.ui.datamining;

import java.util.Collection;
import java.util.Map;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.SharedDimensions;
import com.sap.sailing.datamining.shared.SharedDimensions.GPSFix;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class GPSFixQueryBenchmarkPanel extends AbstractQueryBenchmarkPanel<SharedDimensions.GPSFix> {

    public GPSFixQueryBenchmarkPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryComponentsProvider<SharedDimensions.GPSFix> queryComponentsProvider) {
        super(stringMessages, sailingService, errorReporter, queryComponentsProvider);
    }

    @Override
    protected void sendServerRequest(ClientBenchmarkData<SharedDimensions.GPSFix> benchmarkData,
            AsyncCallback<Pair<Double, Integer>> asyncCallback) {
        QueryComponentsProvider<GPSFix> queryComponentsProvider = getQueryComponentsProvider();
        Map<GPSFix, Collection<?>> selection = benchmarkData.getSelection();
        Collection<GPSFix> dimensionsToGroupBy = queryComponentsProvider.getDimensionsToGroupBy();
        StatisticType statisticToCalculate = queryComponentsProvider.getStatisticToCalculate();
        AggregatorType aggregationType = queryComponentsProvider.getAggregationType();
        getSailingService().runGPSFixQueryAsBenchmark(selection, dimensionsToGroupBy, statisticToCalculate,
                aggregationType, asyncCallback);
    }

}
