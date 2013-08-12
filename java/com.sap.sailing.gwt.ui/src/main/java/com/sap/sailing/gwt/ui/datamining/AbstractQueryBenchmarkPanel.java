package com.sap.sailing.gwt.ui.datamining;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public abstract class AbstractQueryBenchmarkPanel<DimensionType> extends FlowPanel {
    
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    private StringMessages stringMessages;
    private QueryComponentsProvider<DimensionType> queryComponentsProvider;

    private IntegerBox numberOfQueriesBox;
    private Label benchmarkStatusLabel;
    private QueryBenchmarkResultsChart resultsChart;

    public AbstractQueryBenchmarkPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, QueryComponentsProvider<DimensionType> queryComponentsProvider) {
        super();
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.queryComponentsProvider = queryComponentsProvider;
        
        add(createFunctionsPanel());
        resultsChart = new QueryBenchmarkResultsChart(this.stringMessages);
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
                runQuery(new ClientBenchmarkData<DimensionType>(queryComponentsProvider.getSelection(), times, 1));
            }
        });
    }

    private void runQuery(final ClientBenchmarkData<DimensionType> benchmarkData) {
        final long startTime = System.currentTimeMillis();
        sendServerRequest(benchmarkData, new AsyncCallback<Pair<Double, Integer>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error running a query: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Pair<Double, Integer> result) {
                updateResults(benchmarkData, startTime, result);
            }
        });
    }

    protected abstract void sendServerRequest(ClientBenchmarkData<DimensionType> benchmarkData, AsyncCallback<Pair<Double, Integer>> asyncCallback);
    
    protected SailingServiceAsync getSailingService() {
        return sailingService;
    }
    
    protected QueryComponentsProvider<DimensionType> getQueryComponentsProvider() {
        return queryComponentsProvider;
    }

    private void updateResults(final ClientBenchmarkData<DimensionType> benchmarkData, final long startTime,
            Pair<Double, Integer> result) {
        long endTime = System.currentTimeMillis();
        double overallTime = (endTime - startTime) / 1000.0;
        resultsChart.addResult(new QueryBenchmarkResult(stringMessages.runAsSubstantive() + " " + benchmarkData.getCurrentRun(), result.getB().intValue(), result.getA(), overallTime));
        
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
