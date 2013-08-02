package com.sap.sailing.gwt.ui.datamining;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.google.gwt.view.client.MultiSelectionModel;
import com.sap.sailing.datamining.shared.SelectionType;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.AbstractEntryPoint;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class DataMiningEntryPoint extends AbstractEntryPoint {
    
    private IntegerBox numberOfQueriesBox;
    private Label benchmarkStatusLabel;

    private FlowPanel resultsPanel;
    private QueryBenchmarkResultsChart resultsChart;

    private ListDataProvider<RegattaDTO> regattaNameDataProvider;
    private ListDataProvider<BoatClassDTO> boatClassDataProvider;
    private ListDataProvider<RaceDTO> raceNameDataProvider;
    private ListDataProvider<Integer> legNumberDataProvider;
    private ListDataProvider<LegType> legTypeDataProvider;
    private ListDataProvider<CompetitorDTO> competitorNameDataProvider;
    private ListDataProvider<CompetitorDTO> competitorSailIDDataProvider;
    private ListDataProvider<String> nationalityDataProvider;
    
    private MultiSelectionModel<RegattaDTO> regattaNameSelectionModel;
    private MultiSelectionModel<BoatClassDTO> boatClassSelectionModel;
    private MultiSelectionModel<RaceDTO> raceNameSelectionModel;
    private MultiSelectionModel<Integer> legNumberSelectionModel;
    private MultiSelectionModel<LegType> legTypeSelectionModel;
    private MultiSelectionModel<CompetitorDTO> competitorNameSelectionModel;
    private MultiSelectionModel<CompetitorDTO> competitorSailIDSelectionModel;
    private MultiSelectionModel<String> nationalitySelectionModel;
    
    @Override
    protected void doOnModuleLoad() {
        super.doOnModuleLoad();
        RootPanel rootPanel = RootPanel.get();
        FlowPanel dataMiningElementsPanel = new FlowPanel();
        rootPanel.add(dataMiningElementsPanel);
        
        dataMiningElementsPanel.add(createSelectionPanel());
        
        dataMiningElementsPanel.add(createFunctionsPanel());
        
        resultsPanel = new FlowPanel();
        dataMiningElementsPanel.add(resultsPanel);
        resultsChart = new QueryBenchmarkResultsChart();
        resultsPanel.add(resultsChart);
        
        fillSelectionPanel();
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
                runQuery(new ClientQueryData(getSelection(), times, 1));
            }
        });
    }

    private void runQuery(final ClientQueryData queryData) {
        final long startTime = System.currentTimeMillis();
        sailingService.runQueryAsBenchmark(queryData.getSelection(), new AsyncCallback<Pair<Double,Double>>() {
            @Override
            public void onFailure(Throwable caught) {
                DataMiningEntryPoint.this.reportError("Error running a query: " + caught.getMessage());
            }
            @Override
            public void onSuccess(Pair<Double, Double> result) {
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
    
    private Map<SelectionType, Collection<?>> getSelection() {
        Map<SelectionType, Collection<?>> selection = new HashMap<SelectionType, Collection<?>>();
        
        if (!raceNameSelectionModel.getSelectedSet().isEmpty()) {
            Collection<String> regattaNames = new HashSet<String>();
            for (RegattaDTO regatta : regattaNameSelectionModel.getSelectedSet()) {
                regattaNames.add(regatta.getName());
            }
            selection.put(SelectionType.RegattaName, regattaNames);
        }
        
        if (!boatClassSelectionModel.getSelectedSet().isEmpty()) {
            Collection<String> boatClassNames = new HashSet<String>();
            for (BoatClassDTO boatClass : boatClassSelectionModel.getSelectedSet()) {
                boatClassNames.add(boatClass.getName());
            }
            selection.put(SelectionType.BoatClass, boatClassNames);
        }
        
        if (!raceNameSelectionModel.getSelectedSet().isEmpty()) {
            Collection<String> raceNames = new HashSet<String>();
            for (RaceDTO race : raceNameSelectionModel.getSelectedSet()) {
                raceNames.add(race.getName());
            }
            selection.put(SelectionType.RaceName, raceNames);
        }
        
        if (!legNumberSelectionModel.getSelectedSet().isEmpty()) {
            Collection<Integer> raceNames = new HashSet<Integer>(legNumberSelectionModel.getSelectedSet());
            selection.put(SelectionType.LegNumber, raceNames);
        }
        
        if (!legTypeSelectionModel.getSelectedSet().isEmpty()) {
            Collection<LegType> legTypes = new HashSet<LegType>(legTypeSelectionModel.getSelectedSet());
            selection.put(SelectionType.LegType, legTypes);
        }
        
        if (!competitorNameSelectionModel.getSelectedSet().isEmpty()) {
            Collection<String> competitorNames = new HashSet<String>();
            for (CompetitorDTO competitor : competitorNameSelectionModel.getSelectedSet()) {
                competitorNames.add(competitor.getName());
            }
            selection.put(SelectionType.CompetitorName, competitorNames);
        }
        
        if (!competitorSailIDSelectionModel.getSelectedSet().isEmpty()) {
            Collection<String> sailIDs = new HashSet<String>();
            for (CompetitorDTO competitor : competitorSailIDSelectionModel.getSelectedSet()) {
                sailIDs.add(competitor.getSailID());
            }
            selection.put(SelectionType.SailID, sailIDs);
        }
        
        if (!nationalitySelectionModel.getSelectedSet().isEmpty()) {
            Collection<String> nationalities = new HashSet<String>(nationalitySelectionModel.getSelectedSet());
            selection.put(SelectionType.LegNumber, nationalities);
        }
        
        return selection;
    }

    private void fillSelectionPanel() {
        sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                DataMiningEntryPoint.this.reportError("Error fetching the regattas from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(List<RegattaDTO> regattas) {
                regattaNameDataProvider.getList().clear();
                regattaNameDataProvider.getList().addAll(regattas);

                Set<BoatClassDTO> boatClasses = new HashSet<BoatClassDTO>();
                Set<RaceDTO> races = new HashSet<RaceDTO>();
                Set<CompetitorDTO> competitors = new HashSet<CompetitorDTO>();
                Set<String> nationalities = new HashSet<String>();
                for (RegattaDTO regatta : regattas) {
                    if (regatta != null) {
                        boatClasses.add(regatta.boatClass);
                        for (RaceWithCompetitorsDTO race : regatta.races) {
                            if (race != null) {
                                races.add(race);
                                for (CompetitorDTO competitor : race.competitors) {
                                    if (competitor != null) {
                                        competitors.add(competitor);
                                        nationalities.add(competitor.getThreeLetterIocCountryCode());
                                    }
                                }
                            }
                        }
                    }
                }
                
                boatClassDataProvider.getList().clear();
                boatClassDataProvider.getList().addAll(boatClasses);
                
                raceNameDataProvider.getList().clear();
                raceNameDataProvider.getList().addAll(races);
                
                competitorNameDataProvider.getList().clear();
                competitorNameDataProvider.getList().addAll(competitors);
                
                competitorSailIDDataProvider.getList().clear();
                competitorSailIDDataProvider.getList().addAll(competitors);
                
                nationalityDataProvider.getList().clear();
                nationalityDataProvider.getList().addAll(nationalities);
            }
        });
        
        legNumberDataProvider.getList().clear();
        legNumberDataProvider.getList().addAll(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        
        legTypeDataProvider.getList().clear();
        legTypeDataProvider.getList().addAll(Arrays.asList(LegType.values()));
    }

    private HorizontalPanel createSelectionPanel() {
        HorizontalPanel selectionPanel = new HorizontalPanel();
        selectionPanel.setSpacing(5);
        
        CellTable<RegattaDTO> regattaNameTable = new CellTable<RegattaDTO>();
        regattaNameTable.addColumn(new TextColumn<RegattaDTO>() {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.getName();
            }
        }, "Regatta");
        regattaNameSelectionModel = new MultiSelectionModel<RegattaDTO>();
        regattaNameTable.setSelectionModel(regattaNameSelectionModel);
        regattaNameDataProvider = new ListDataProvider<RegattaDTO>();
        regattaNameDataProvider.addDataDisplay(regattaNameTable);
        selectionPanel.add(regattaNameTable);
        
        CellTable<BoatClassDTO> boatClassTable = new CellTable<BoatClassDTO>();
        boatClassTable.addColumn(new TextColumn<BoatClassDTO>() {
            @Override
            public String getValue(BoatClassDTO boatClass) {
                return boatClass.getName();
            }
        }, "Boat Class");
        boatClassSelectionModel = new MultiSelectionModel<BoatClassDTO>();
        boatClassTable.setSelectionModel(boatClassSelectionModel);
        boatClassDataProvider = new ListDataProvider<BoatClassDTO>();
        boatClassDataProvider.addDataDisplay(boatClassTable);
        selectionPanel.add(boatClassTable);
        
        CellTable<RaceDTO> raceNameTable = new CellTable<RaceDTO>();
        raceNameTable.addColumn(new TextColumn<RaceDTO>() {
            @Override
            public String getValue(RaceDTO race) {
                return race.getName();
            }
        }, "Race Name");
        raceNameSelectionModel = new MultiSelectionModel<RaceDTO>();
        raceNameTable.setSelectionModel(raceNameSelectionModel);
        raceNameDataProvider = new ListDataProvider<RaceDTO>();
        raceNameDataProvider.addDataDisplay(raceNameTable);
        selectionPanel.add(raceNameTable);
        
        CellTable<Integer> legNumberTable = new CellTable<Integer>();
        legNumberTable.addColumn(new TextColumn<Integer>() {
            @Override
            public String getValue(Integer legNumber) {
                return legNumber + "";
            }
        }, "Leg Number");
        legNumberSelectionModel = new MultiSelectionModel<Integer>();
        legNumberTable.setSelectionModel(legNumberSelectionModel);
        legNumberDataProvider = new ListDataProvider<Integer>();
        legNumberDataProvider.addDataDisplay(legNumberTable);
        selectionPanel.add(legNumberTable);
        
        CellTable<LegType> legTypeTable = new CellTable<LegType>();
        legTypeTable.addColumn(new TextColumn<LegType>() {
            @Override
            public String getValue(LegType legType) {
                return legType.name();
            }
        }, "Leg Type");
        legTypeSelectionModel = new MultiSelectionModel<LegType>();
        legTypeTable.setSelectionModel(legTypeSelectionModel);
        legTypeDataProvider = new ListDataProvider<LegType>();
        legTypeDataProvider.addDataDisplay(legTypeTable);
        selectionPanel.add(legTypeTable);
        
        CellTable<CompetitorDTO> competitorNameTable = new CellTable<CompetitorDTO>();
        competitorNameTable.addColumn(new TextColumn<CompetitorDTO>() {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getName();
            }
        }, "Competitor Name");
        competitorNameSelectionModel = new MultiSelectionModel<CompetitorDTO>();
        competitorNameTable.setSelectionModel(competitorNameSelectionModel);
        competitorNameDataProvider = new ListDataProvider<CompetitorDTO>();
        competitorNameDataProvider.addDataDisplay(competitorNameTable);
        selectionPanel.add(competitorNameTable);
        
        CellTable<CompetitorDTO> competitorSailIDTable = new CellTable<CompetitorDTO>();
        competitorSailIDTable.addColumn(new TextColumn<CompetitorDTO>() {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getSailID();
            }
        }, "Competitor Sail ID");
        competitorSailIDSelectionModel = new MultiSelectionModel<CompetitorDTO>();
        competitorSailIDTable.setSelectionModel(competitorSailIDSelectionModel);
        competitorSailIDDataProvider = new ListDataProvider<CompetitorDTO>();
        competitorSailIDDataProvider.addDataDisplay(competitorSailIDTable);
        selectionPanel.add(competitorSailIDTable);
        
        CellTable<String> nationalityTable = new CellTable<String>();
        nationalityTable.addColumn(new TextColumn<String>() {
            @Override
            public String getValue(String nationality) {
                return nationality;
            }
        }, "Nationality");
        nationalitySelectionModel = new MultiSelectionModel<String>();
        nationalityTable.setSelectionModel(nationalitySelectionModel);
        nationalityDataProvider = new ListDataProvider<String>();
        nationalityDataProvider.addDataDisplay(nationalityTable);
        selectionPanel.add(nationalityTable);
        
        return selectionPanel;
    }

}