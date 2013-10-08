package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.datamining.shared.SharedDimensions;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class GPSFixResultsPanel extends AbstractResultsPanel<SharedDimensions.GPSFix, Number> {

    public GPSFixResultsPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryComponentsProvider<SharedDimensions.GPSFix> queryComponentsProvider, ResultsPresenter<Number> presenter) {
        super(stringMessages, sailingService, errorReporter, queryComponentsProvider, presenter);
    }

    @Override
    protected void sendServerRequest(QueryDefinition<SharedDimensions.GPSFix> queryDefinition, AsyncCallback<QueryResult<Number>> asyncCallback) {
        getSailingService().runGPSFixQuery(queryDefinition, asyncCallback);
    }

}
