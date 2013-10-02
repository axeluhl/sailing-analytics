package com.sap.sailing.gwt.ui.datamining;

import com.google.gwt.user.client.ui.FlowPanel;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class DataMiningPanel extends FlowPanel {

    public DataMiningPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter, boolean showBenchmark) {
        GPSFixSelectionPanel selectionPanel = new GPSFixSelectionPanel(stringMessages, sailingService, errorReporter);
        this.add(selectionPanel);
        
        GPSFixQueryPanel queryPanel = new GPSFixQueryPanel(stringMessages, sailingService, errorReporter, selectionPanel);
        this.add(queryPanel);
        
        if (showBenchmark) {
            GPSFixQueryBenchmarkPanel benchmarkPanel = new GPSFixQueryBenchmarkPanel(stringMessages, sailingService, errorReporter, selectionPanel);
            this.add(benchmarkPanel);
        }
    }

}
