package com.sap.sailing.gwt.ui.datamining;

import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IntegerBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.datamining.shared.SelectorType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    
    private IntegerBox numberOfQueriesBox;
    private Label averageOverallTimeLabel;
    private Label averageServerTimeLabel;

    private FlowPanel resultsPanel;
    private ListDataProvider<Pair<String, Double>> resultsDataProvider;
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        FlowPanel dataMiningElementsPanel = new FlowPanel();
        rootPanel.add(dataMiningElementsPanel);
        
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);
        dataMiningElementsPanel.add(functionsPanel);
        
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
        
        resultsPanel = new FlowPanel();
        resultsPanel.setVisible(false);
        dataMiningElementsPanel.add(resultsPanel);
        
        HorizontalPanel overallTimePanel = new HorizontalPanel();
        overallTimePanel.setSpacing(5);
        overallTimePanel.add(new Label("Average overall time: "));
        averageOverallTimeLabel = new Label();
        overallTimePanel.add(averageOverallTimeLabel);
        resultsPanel.add(overallTimePanel);
        
        HorizontalPanel serverTimePanel = new HorizontalPanel();
        serverTimePanel.setSpacing(5);
        serverTimePanel.add(new Label("Average server time: "));
        averageServerTimeLabel = new Label();
        serverTimePanel.add(averageServerTimeLabel);
        resultsPanel.add(serverTimePanel);

        TextColumn<Pair<String, Double>> xValues = new TextColumn<Pair<String,Double>>() {
            @Override
            public String getValue(Pair<String, Double> dateElement) {
                return dateElement.getA();
            }
        };
        TextColumn<Pair<String, Double>> results = new TextColumn<Pair<String,Double>>() {
            @Override
            public String getValue(Pair<String, Double> dateElement) {
                return dateElement.getB().toString();
            }
        };
        
        CellTable<Pair<String,Double>> resultsTable = new CellTable<Pair<String,Double>>();
        resultsTable.addColumn(xValues, "X Values");
        resultsTable.addColumn(results, "Results");
        resultsDataProvider = new ListDataProvider<Pair<String,Double>>();
        resultsDataProvider.addDataDisplay(resultsTable);
        resultsPanel.add(resultsTable);
    }

    private void run() {
        final int times = numberOfQueriesBox.getValue() == null ? 1 : numberOfQueriesBox.getValue();
        String[] selectionIdentifiers = new String[] {"KW 2013 International (STR)", "KW 2013 International (H-Boat)", "KW 2013 International (29ER)",
                "KW 2013 International (505)", "KW 2013 International (F18)", "KW 2013 International (EUR)",
                "KW 2013 International (Folkeboot)", "KW 2013 International (H16)", "KW 2013 International (L4.7)"};
        SelectorType selectorType = SelectorType.Regattas;
        final long startTime = System.currentTimeMillis();
        sailingService.runQueryAsBenchmark(selectorType, selectionIdentifiers, times, new AsyncCallback<Pair<Double, List<Double>>>() {
            @Override
            public void onFailure(Throwable caught) {
                DataMiningEntryPoint.this.reportError("Error running a query: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Pair<Double, List<Double>> result) {
                long endTime = System.currentTimeMillis();
                double averageOverallTime = (endTime - startTime) / (1000.0 * times);
                updateResults(averageOverallTime, result.getA(), result.getB());
            }
        });
    }

    private void updateResults(double averageOverallTime, double averageServerTime, List<Double> results) {
        averageOverallTimeLabel.setText(averageOverallTime + "s");
        averageServerTimeLabel.setText(averageServerTime + "s");
        
        resultsDataProvider.getList().clear();
        int i = 1;
        for (Double gpsFixAmount : results) {
            resultsDataProvider.getList().add(new Pair<String, Double>(i + ". Number of selected and retrieved fixes", gpsFixAmount));
            i++;
        }
        
        if (!resultsPanel.isVisible()) {
            resultsPanel.setVisible(true);
        }
    }

}