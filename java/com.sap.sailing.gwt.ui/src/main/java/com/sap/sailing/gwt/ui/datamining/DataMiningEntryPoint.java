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
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    
    private SelectionPanel selectionPanel;
    
    private IntegerBox numberOfQueriesBox;
    private Label benchmarkStatusLabel;

    private FlowPanel resultsPanel;
    private QueryBenchmarkResultsChart resultsChart;
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        FlowPanel dataMiningElementsPanel = new FlowPanel();
        rootPanel.add(dataMiningElementsPanel);
        
        selectionPanel = new SelectionPanel(stringMessages, sailingService, this);
        dataMiningElementsPanel.add(selectionPanel);
        
        dataMiningElementsPanel.add(createFunctionsPanel());
        
        resultsPanel = new FlowPanel();
        dataMiningElementsPanel.add(resultsPanel);
        resultsChart = new QueryBenchmarkResultsChart();
        resultsPanel.add(resultsChart);
    }

    private void runBenchmark() {
        benchmarkStatusLabel.setText(" | Running");
        resultsChart.reset();
        sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                DataMiningEntryPoint.this.reportError("Error fetching the regattas from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(List<RegattaDTO> regattas) {
                final int times = numberOfQueriesBox.getValue() == null ? 1 : numberOfQueriesBox.getValue();
                runQuery(new ClientQueryData(selectionPanel.getSelection(), times, 1));
            }
        });
    }

    private void runQuery(final ClientQueryData queryData) {
        final long startTime = System.currentTimeMillis();
        sailingService.runQueryAsBenchmark(queryData.getSelection(), new AsyncCallback<Pair<Double, Integer>>() {
            @Override
            public void onFailure(Throwable caught) {
                DataMiningEntryPoint.this.reportError("Error running a query: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Pair<Double, Integer> result) {
                long endTime = System.currentTimeMillis();
                double overallTime = (endTime - startTime) / 1000.0;
                resultsChart.addResult(new QueryBenchmarkResult("Run " + queryData.getCurrentRun(), result.getB().intValue(), result.getA(), overallTime));
                
                if (queryData.isFinished()) {
                    benchmarkStatusLabel.setText(" | Done");
                    resultsChart.showResults();
                } else {
                    benchmarkStatusLabel.setText(" | Running (last finished: " + queryData.getCurrentRun() + ")");
                    queryData.incrementCurrentRun();
                    runQuery(queryData);
                }
            }
        });
    }

    private HorizontalPanel createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);
        
        Button runQueryButton = new Button("Run");
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
        functionsPanel.add(new Label("times"));
        
        benchmarkStatusLabel = new Label();
        functionsPanel.add(benchmarkStatusLabel);
        return functionsPanel;
    }

}