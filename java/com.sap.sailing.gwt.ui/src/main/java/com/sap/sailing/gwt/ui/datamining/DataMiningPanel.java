package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.datamining.presentation.BenchmarkResultsPanel;
import com.sap.sailing.gwt.ui.datamining.presentation.QueryResultsPanel;
import com.sap.sailing.gwt.ui.datamining.presentation.ResultsChart;
import com.sap.sailing.gwt.ui.datamining.selection.SimpleQueryDefinitionProvider;

public class DataMiningPanel extends FlowPanel {

    public DataMiningPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, boolean showBenchmark) {
        this.addStyleName("dataMiningPanel");
        
        QueryDefinitionProvider selectionPanel = new SimpleQueryDefinitionProvider(stringMessages, sailingService, errorReporter);
        this.add(selectionPanel.getWidget());
        
        ResultsPresenter<Number> resultsChart = new ResultsChart(stringMessages);
        QueryResultsPanel queryPanel = new QueryResultsPanel(stringMessages, sailingService, errorReporter, selectionPanel, resultsChart);
        this.add(queryPanel);
        
        if (showBenchmark) {
            BenchmarkResultsPanel benchmarkPanel = new BenchmarkResultsPanel(stringMessages, sailingService, errorReporter, selectionPanel);
            this.add(benchmarkPanel);
        }
    }

}
