package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.datamining.shared.SharedDimensions;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class GPSFixBenchmarkResultsPanel extends AbstractBenchmarkResultsPanel<SharedDimensions.GPSFix> {

    public GPSFixBenchmarkResultsPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryDefinitionProvider<SharedDimensions.GPSFix> queryComponentsProvider) {
        super(stringMessages, sailingService, errorReporter, queryComponentsProvider);
    }

    @Override
    protected void sendServerRequest(QueryDefinition<SharedDimensions.GPSFix> queryDefinition, AsyncCallback<QueryResult<Number>> asyncCallback) {
        getSailingService().runGPSFixQuery(queryDefinition, asyncCallback);
    }

}
