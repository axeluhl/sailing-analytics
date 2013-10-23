package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.QueryDefinitionProvider;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class BenchmarkResultsPanel extends FlowPanel {
    
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    private StringMessages stringMessages;
    private QueryDefinitionProvider queryDefinitionProvider;

    private IntegerBox numberOfQueriesBox;
    private Label benchmarkStatusLabel;
    private BenchmarkResultsChart resultsChart;

    public BenchmarkResultsPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryDefinitionProvider queryDefinitionProvider) {
        super();
        this.sailingService = sailingService;
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
        sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the regattas from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(List<RegattaDTO> regattas) {
                final int times = numberOfQueriesBox.getValue() == null ? 1 : numberOfQueriesBox.getValue();
                runQuery(new ClientBenchmarkData(times, 1));
            }
        });
    }

    private void runQuery(final ClientBenchmarkData benchmarkData) {
        final long startTime = System.currentTimeMillis();
        getSailingService().runQuery(queryDefinitionProvider.getQueryDefinition(), new AsyncCallback<QueryResult<Number>>() {
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

    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }
    
    protected QueryDefinitionProvider getQueryDefinitionProvider() {
        return queryDefinitionProvider;
    }

    private void updateResults(final ClientBenchmarkData benchmarkData, double overallTimeInSeconds, QueryResult<? extends Number> result) {
        resultsChart.addResult(new BenchmarkResult(stringMessages.runAsSubstantive() + " " + benchmarkData.getCurrentRun(),
                                                        result.getFilteredDataAmount(), result.getCalculationTimeInSeconds(), overallTimeInSeconds));
        
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
