package com.sap.sailing.gwt.ui.datamining;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.datamining.Aggregator;
import com.sap.sailing.datamining.AggregatorFactory;
import com.sap.sailing.datamining.Extractor;
import com.sap.sailing.datamining.ExtractorFactory;
import com.sap.sailing.datamining.Query;
import com.sap.sailing.datamining.QueryFactory;
import com.sap.sailing.datamining.Selector;
import com.sap.sailing.datamining.SelectorFactory;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.adminconsole.AdminConsoleTableResources;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    
    private Label overallTimeLabel;
    private Label serverTimeLabel;

    private FlowPanel resultsPanel;
    private ListDataProvider<Pair<String, Double>> resultsDataProvider;
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        FlowPanel dataMiningElementsPanel = new FlowPanel();
        rootPanel.add(dataMiningElementsPanel);
        
        Button runQueryButton = new Button("Run");
        runQueryButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                createAndRunQuery();
            }
        });
        dataMiningElementsPanel.add(runQueryButton);
        
        resultsPanel = new FlowPanel();
//        resultsPanel.setVisible(false);
        rootPanel.add(resultsPanel);
        
        HorizontalPanel overallTimePanel = new HorizontalPanel();
        overallTimePanel.add(new Label("Overall time: "));
        overallTimeLabel = new Label();
        overallTimePanel.add(overallTimeLabel);
        resultsPanel.add(overallTimePanel);
        
        HorizontalPanel serverTimePanel = new HorizontalPanel();
        serverTimePanel.add(new Label("Server time: "));
        serverTimeLabel = new Label();
        serverTimePanel.add(serverTimeLabel);
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
        
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        CellTable<Pair<String,Double>> resultsTable = new CellTable<Pair<String,Double>>(1000, tableRes);
        resultsTable.addColumn(xValues, "X Values");
        resultsTable.addColumn(results, "Results");
        resultsDataProvider.addDataDisplay(resultsTable);
        resultsPanel.add(resultsTable);
    }

    private void createAndRunQuery() {
        final long startTime = System.currentTimeMillis();
        Selector selector = SelectorFactory.createEventSelector("Kieler Woche 2013");
        Extractor extractor = ExtractorFactory.createDistanceInMetersExtractor();
        Aggregator aggregator = AggregatorFactory.createSumAggregator();
        Query query = QueryFactory.createQuery(selector, extractor, aggregator);
//        sailingService.runQuery(query, new AsyncCallback<Pair<Double, List<Pair<String, Double>>>>() {
//            @Override
//            public void onFailure(Throwable caught) {
//                DataMiningEntryPoint.this.reportError("Error running a query: " + caught.getMessage());
//            }
//            @Override
//            public void onSuccess(Pair<Double, List<Pair<String, Double>>> result) {
//                long endTime = System.currentTimeMillis();
//                double overallTime = (endTime - startTime) / 1000.0;
//                updateResults(overallTime, result.getA(), result.getB());
//            }
//        });
    }
    
    private void updateResults(double overallTime, double serverTime, List<Pair<String, Double>> results) {
        overallTimeLabel.setText(overallTime + "s");
        serverTimeLabel.setText(serverTime + "s");
        
        resultsDataProvider.getList().clear();
        resultsDataProvider.getList().addAll(results);
        
        if (!resultsPanel.isVisible()) {
            resultsPanel.setVisible(true);
        }
    }

}