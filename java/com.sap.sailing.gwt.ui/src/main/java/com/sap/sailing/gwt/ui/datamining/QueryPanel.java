package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class QueryPanel extends FlowPanel {

    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    private QuerySelectionProvider selectionProvider;
    
    private QueryResultsChart resultChart;

    public QueryPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QuerySelectionProvider selectionProvider) {
        super();
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.selectionProvider = selectionProvider;
        
        add(createFunctionsPanel());
        resultChart = new QueryResultsChart(this.stringMessages);
        add(resultChart);
    }

    private void runQuery() {
        sailingService.runQuery(selectionProvider.getSelection(), new AsyncCallback<QueryResult>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error running the query: " + caught.getMessage());
            }
            @Override
            public void onSuccess(QueryResult result) {
                resultChart.showResult(result);
            }
        });
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
        
        return functionsPanel;
    }

}
