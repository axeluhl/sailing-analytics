package com.sap.sailing.gwt.ui.datamining;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.sap.sailing.datamining.shared.SelectorType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    
    private IntegerBox numberOfQueriesBox;
    private Label benchmarkStatusLabel;

    private FlowPanel resultsPanel;
    private QueryBenchmarkResultsChart resultsChart;
    
    private int executedBenchmarks;
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        FlowPanel dataMiningElementsPanel = new FlowPanel();
        rootPanel.add(dataMiningElementsPanel);
        
        dataMiningElementsPanel.add(createFunctionsPanel());
        
        resultsPanel = new FlowPanel();
        resultsPanel.setVisible(false);
        dataMiningElementsPanel.add(resultsPanel);
        resultsChart = new QueryBenchmarkResultsChart();
        resultsPanel.add(resultsChart);
    }

    private void run() {
        benchmarkStatusLabel.setText(" | Running");
        resultsChart.reset();
        resetResults();
        final int times = numberOfQueriesBox.getValue() == null ? 1 : numberOfQueriesBox.getValue();
        String[] selectionIdentifiers = new String[] {"KW 2013 International (STR)", "KW 2013 International (H-Boat)", "KW 2013 International (29ER)",
                "KW 2013 International (505)", "KW 2013 International (F18)", "KW 2013 International (EUR)",
                "KW 2013 International (Folkeboot)", "KW 2013 International (H16)", "KW 2013 International (L4.7)"};
        SelectorType selectorType = SelectorType.Regattas;
        final Map<Integer, Long> startTimesMap = new HashMap<Integer, Long>();
        for (int i = 0; i < times; i++) {
            final int number = i + 1;
            
            startTimesMap.put(number, System.currentTimeMillis());
            sailingService.runQueryAsBenchmark(selectorType, selectionIdentifiers,
                    new AsyncCallback<Pair<Double, Double>>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            DataMiningEntryPoint.this.reportError("Error running a query: " + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Pair<Double, Double> result) {
                            long endTime = System.currentTimeMillis();
                            double overallTime = (endTime - startTimesMap.get(number)) / 1000.0;
                            resultsChart.addResult(new QueryBenchmarkResult("Run " + number, result.getB().intValue(), result.getA(), overallTime));
                            executedBenchmarks++;

                            if (!resultsPanel.isVisible()) {
                                resultsPanel.setVisible(true);
                            }
                            if (executedBenchmarks == times) {
                                benchmarkStatusLabel.setText(" | Done");
                                resultsChart.showResults();
                            } else {
                                benchmarkStatusLabel.setText(" | Running (last finished: " + number + ")");
                            }
                        }
                    });
        }
    }

    private void resetResults() {
        executedBenchmarks = 0;
    }

    private HorizontalPanel createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);
        
        Button runQueryButton = new Button("Run");
        runQueryButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                run();
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