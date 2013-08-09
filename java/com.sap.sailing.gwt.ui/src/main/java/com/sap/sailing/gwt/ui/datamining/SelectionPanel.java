package com.sap.sailing.gwt.ui.datamining;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueListBox;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.Dimension;
import com.sap.sailing.datamining.shared.StatisticType;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class SelectionPanel extends FlowPanel implements QueryComponentsProvider {
    
    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;

    private ValueListBox<Dimension> groupByListBox;
    private ValueListBox<StatisticAndAggregatorType> statisticsListBox;
    
    private Map<Dimension, SelectionTable<?, ?>> tablesMappedByDimension;
    private SelectionTable<RegattaDTO, String> regattaNameTable;
    private SelectionTable<BoatClassDTO, String> boatClassTable;
    private SelectionTable<RaceDTO, String> raceNameTable;
    private SelectionTable<Integer, Integer> legNumberTable;
    private SelectionTable<LegType, LegType> legTypeTable;
    private SelectionTable<CompetitorDTO, String> competitorNameTable;
    private SelectionTable<CompetitorDTO, String> competitorSailIDTable;
    private SelectionTable<String, String> nationalityTable;
    
    public SelectionPanel(StringMessages stringMessages, SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        super();
        
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        
        add(createSelectionTables());
        
        add(createFunctionsPanel());
        
        fillSelectionTables();
    }

    @Override
    public Map<Dimension, Collection<?>> getSelection() {
        Map<Dimension, Collection<?>> selection = new HashMap<Dimension, Collection<?>>();
        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            Collection<?> specificSelection = table.getSelection();
            if (!specificSelection.isEmpty()) {
                selection.put(table.getDimension(), specificSelection);
            }
        }
        return selection;
    }

    private void clearSelection() {
        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            table.clearSelection();
        }
    }

    @Override
    public Dimension getDimensionToGroupBy() {
        return groupByListBox.getValue();
    }

    @Override
    public StatisticType getStatisticToCalculate() {
        return statisticsListBox.getValue().getStatisticType();
    }

    @Override
    public AggregatorType getAggregationType() {
        return statisticsListBox.getValue().getAggregatorType();
    }

    private void fillSelectionTables() {
        sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the regattas from the server: " + caught.getMessage());
            }
            @Override
            public void onSuccess(List<RegattaDTO> regattas) {
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

                regattaNameTable.setContent(regattas);
                boatClassTable.setContent(boatClasses);
                raceNameTable.setContent(races);
                legNumberTable.setContent(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
                legTypeTable.setContent(Arrays.asList(LegType.values()));
                competitorNameTable.setContent(competitors);
                competitorSailIDTable.setContent(competitors);
                nationalityTable.setContent(nationalities);
            }
        });
    }

    private HorizontalPanel createFunctionsPanel() {
        HorizontalPanel functionsPanel = new HorizontalPanel();
        functionsPanel.setSpacing(5);
        
        Button clearSelectionButton = new Button(this.stringMessages.clearSelection());
        clearSelectionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearSelection();
            }
        });
        functionsPanel.add(clearSelectionButton);
        
        functionsPanel.add(new Label(stringMessages.groupBy() + ": "));
        groupByListBox = new ValueListBox<Dimension>(new Renderer<Dimension>() {
            @Override
            public String render(Dimension dimension) {
                if (dimension == null) {
                  return "";
                }
                return dimension.toString();
            }
            @Override
            public void render(Dimension dimension, Appendable appendable) throws IOException {
                appendable.append(render(dimension));
            }
        });
        groupByListBox.setAcceptableValues(Arrays.asList(Dimension.values()));
        functionsPanel.add(groupByListBox);
        
        functionsPanel.add(new Label(stringMessages.statisticToCalculate() + ": "));
        statisticsListBox = new ValueListBox<StatisticAndAggregatorType>(new Renderer<StatisticAndAggregatorType>() {
            @Override
            public String render(StatisticAndAggregatorType statisticAndAggregatorType) {
                if (statisticAndAggregatorType == null) {
                  return "";
                }
                return statisticAndAggregatorType.toString();
            }
            @Override
            public void render(StatisticAndAggregatorType statisticAndAggregatorType, Appendable appendable) throws IOException {
                appendable.append(render(statisticAndAggregatorType));
            }
        });
        statisticsListBox.setAcceptableValues(Arrays.asList(new StatisticAndAggregatorType(StatisticType.DataAmount, AggregatorType.Average)));
        functionsPanel.add(statisticsListBox);
        
        return functionsPanel;
    }

    private HorizontalPanel createSelectionTables() {
        HorizontalPanel tablesPanel = new HorizontalPanel();
        tablesPanel.setSpacing(5);
        tablesMappedByDimension = new HashMap<Dimension, SelectionTable<?,?>>();
        
        regattaNameTable = new SelectionTable<RegattaDTO, String>(stringMessages.regatta(), Dimension.RegattaName) {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.getName();
            }
        };
        tablesPanel.add(regattaNameTable);
        tablesMappedByDimension.put(Dimension.RegattaName, regattaNameTable);
        
        boatClassTable = new SelectionTable<BoatClassDTO, String>(stringMessages.boatClass(), Dimension.BoatClassName) {
            @Override
            public String getValue(BoatClassDTO boatClass) {
                return boatClass.getName();
            }
        };
        tablesPanel.add(boatClassTable);
        tablesMappedByDimension.put(Dimension.BoatClassName, boatClassTable);
        
        raceNameTable = new SelectionTable<RaceDTO, String>(stringMessages.race(), Dimension.RaceName) {
            @Override
            public String getValue(RaceDTO race) {
                return race.getName();
            }
        };
        tablesPanel.add(raceNameTable);
        tablesMappedByDimension.put(Dimension.RaceName, raceNameTable);
        
        legNumberTable = new SelectionTable<Integer, Integer>(stringMessages.legLabel(), Dimension.LegNumber) {
            @Override
            public Integer getValue(Integer legNumber) {
                return legNumber;
            }
        };
        tablesPanel.add(legNumberTable);
        tablesMappedByDimension.put(Dimension.LegNumber, legNumberTable);
        
        legTypeTable = new SelectionTable<LegType, LegType>(stringMessages.legType(), Dimension.LegType) {
            @Override
            public LegType getValue(LegType legType) {
                return legType;
            }
        };
        tablesPanel.add(legTypeTable);
        tablesMappedByDimension.put(Dimension.LegType, legTypeTable);
        
        competitorNameTable = new SelectionTable<CompetitorDTO, String>(stringMessages.competitor(), Dimension.CompetitorName) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getName();
            }
        };
        tablesPanel.add(competitorNameTable);
        tablesMappedByDimension.put(Dimension.CompetitorName, competitorNameTable);
        
        competitorSailIDTable = new SelectionTable<CompetitorDTO, String>(stringMessages.sailID(), Dimension.SailID) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getSailID();
            }
        };
        tablesPanel.add(competitorSailIDTable);
        tablesMappedByDimension.put(Dimension.SailID, competitorSailIDTable);
        
        nationalityTable = new SelectionTable<String, String>(stringMessages.nationality(), Dimension.Nationality) {
            @Override
            public String getValue(String nationality) {
                return nationality;
            }
        };
        tablesPanel.add(nationalityTable);
        tablesMappedByDimension.put(Dimension.Nationality, nationalityTable);
        
        return tablesPanel;
    }

}
