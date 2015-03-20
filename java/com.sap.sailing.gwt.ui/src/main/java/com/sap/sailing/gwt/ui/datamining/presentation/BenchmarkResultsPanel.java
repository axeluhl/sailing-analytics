package com.sap.sailing.gwt.ui.datamining.presentation;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.DataMiningServiceAsync;
import com.sap.sailing.gwt.ui.datamining.QueryDefinitionProvider;
import com.sap.sse.datamining.shared.DataMiningSession;
import com.sap.sse.datamining.shared.QueryResult;
import com.sap.sse.gwt.client.ErrorReporter;

public class BenchmarkResultsPanel extends FlowPanel {

    private final DataMiningSession session;
    private DataMiningServiceAsync dataMiningService;
    private ErrorReporter errorReporter;
    private StringMessages stringMessages;
    private QueryDefinitionProvider queryDefinitionProvider;

    private IntegerBox numberOfQueriesBox;
    private Label benchmarkStatusLabel;
    private BenchmarkResultsChart resultsChart;

    public BenchmarkResultsPanel(DataMiningSession session, StringMessages stringMessages, DataMiningServiceAsync dataMiningService,
            ErrorReporter errorReporter, QueryDefinitionProvider queryDefinitionProvider) {
        super();
        this.session = session;
        this.dataMiningService = dataMiningService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.queryDefinitionProvider = queryDefinitionProvider;
        
        add(createFunctionsPanel());
        resultsChart = new BenchmarkResultsChart(this.stringMessages);
        add(resultsChart);
    }

    private void runBenchmark() {
        benchmarkStatusLabel.setText(" | " + stringMessages.running());
        resultsChart.reset();
        
        int times = numberOfQueriesBox.getValue() == null ? 1 : numberOfQueriesBox.getValue();
        runQuery(new ClientBenchmarkData(times, 1));
    }

    private void runQuery(final ClientBenchmarkData benchmarkData) {
        final long startTime = System.currentTimeMillis();
        dataMiningService.runQuery(session, queryDefinitionProvider.getQueryDefinition(), new AsyncCallback<QueryResult<Number>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error running a query: " + caught.getMessage());
            }
            @Override
            public void onSuccess(QueryResult<Number> result) {
                long endTime = System.currentTimeMillis();
                double overallTime = (endTime - startTime) / 1000.0;
                updateResults(benchmarkData, overallTime, result);
            }
        });
    }
    
    protected QueryDefinitionProvider getQueryDefinitionProvider() {
        return queryDefinitionProvider;
    }

    private void updateResults(final ClientBenchmarkData benchmarkData, double overallTimeInSeconds, QueryResult<? extends Number> result) {
        resultsChart.addResult(new BenchmarkResult(stringMessages.runAsSubstantive() + " " + benchmarkData.getCurrentRun(),
                                                        result.getRetrievedDataAmount(), result.getCalculationTimeInSeconds(), overallTimeInSeconds));
        
        if (benchmarkData.isFinished()) {
            benchmarkStatusLabel.setText(" | " + stringMessages.done());
            resultsChart.showResults();
        } else {
            benchmarkStatusLabel.setText(" | " + stringMessages.running() + " (" + stringMessages.lastFinished() + ": " + benchmarkData.getCurrentRun() + ")");
            benchmarkData.incrementCurrentRun();
            runQuery(benchmarkData);
        }
    }

    private HorizontalPanel createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);
        
        Button runQueryButton = new Button(stringMessages.run());
        runQueryButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                runBenchmark();
            }
        });
        functionsPanel.add(runQueryButton);
        
        numberOfQueriesBox = new IntegerBox();
        numberOfQueriesBox.setValue(1);
        functionsPanel.add(numberOfQueriesBox);
        functionsPanel.add(new Label(stringMessages.times()));
        
        benchmarkStatusLabel = new Label();
        functionsPanel.add(benchmarkStatusLabel);
        return functionsPanel;
    }

}
