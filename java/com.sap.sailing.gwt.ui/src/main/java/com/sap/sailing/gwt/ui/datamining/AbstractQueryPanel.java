package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public abstract class AbstractQueryPanel<DimensionType> extends FlowPanel {

    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    private QueryComponentsProvider<DimensionType> queryComponentsProvider;

    private Label queryStatusLabel;
    private QueryResultsChart resultChart;

    public AbstractQueryPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryComponentsProvider<DimensionType> queryComponentsProvider) {
        super();
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.queryComponentsProvider = queryComponentsProvider;
        
        add(createFunctionsPanel());
        resultChart = new QueryResultsChart(this.stringMessages);
        add(resultChart);
    }

    private void runQuery() {
        queryStatusLabel.setText(" | " + stringMessages.running());
        sendServerRequest(new AsyncCallback<QueryResult<Number>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error running the query: " + caught.getMessage());
            }
            @Override
            public void onSuccess(QueryResult<Number> result) {
                queryStatusLabel.setText(" | " + stringMessages.done());
                resultChart.showResult(result);
            }
        });
    }

    protected abstract void sendServerRequest(AsyncCallback<QueryResult<Number>> asyncCallback);
    
    protected QueryComponentsProvider<DimensionType> getQueryComponentsProvider() {
        return queryComponentsProvider;
    }
    
    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }

    private HorizontalPanel createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);
        
        Button runQueryButton = new Button(stringMessages.run());
        runQueryButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                runQuery();
            }
        });
        functionsPanel.add(runQueryButton);
        
        queryStatusLabel = new Label();
        functionsPanel.add(queryStatusLabel);
        return functionsPanel;
    }

}
