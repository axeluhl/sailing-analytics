package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.datamining.shared.SharedDimensions;
import com.sap.sailing.datamining.shared.SharedDimensions.GPSFix;
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
        getSailingService().runGPSFixQueryAsBenchmark(benchmarkData.getSelection(),
                queryComponentsProvider.getDimensionToGroupBy(), queryComponentsProvider.getStatisticToCalculate(),
                queryComponentsProvider.getAggregationType(), asyncCallback);
    }

}
