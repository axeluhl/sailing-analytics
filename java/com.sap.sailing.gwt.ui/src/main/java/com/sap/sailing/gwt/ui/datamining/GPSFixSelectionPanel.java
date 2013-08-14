package com.sap.sailing.gwt.ui.datamining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.datamining.shared.AggregatorType;
import com.sap.sailing.datamining.shared.SharedDimensions;
import com.sap.sailing.datamining.shared.SharedDimensions.GPSFix;
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

public class GPSFixSelectionPanel extends FlowPanel implements QueryComponentsProvider<SharedDimensions.GPSFix> {
    
    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;

    private HorizontalPanel dimensionsToGroupByPanel;
    private List<ValueListBox<SharedDimensions.GPSFix>> dimensionsToGroupByBoxes;
    private ValueListBox<StatisticAndAggregatorType> statisticsListBox;
    
    private Map<SharedDimensions.GPSFix, SelectionTable<SharedDimensions.GPSFix, ?, ?>> tablesMappedByDimension;
    private SelectionTable<SharedDimensions.GPSFix, RegattaDTO, String> regattaNameTable;
    private SelectionTable<SharedDimensions.GPSFix, BoatClassDTO, String> boatClassTable;
    private SelectionTable<SharedDimensions.GPSFix, RaceDTO, String> raceNameTable;
    private SelectionTable<SharedDimensions.GPSFix, Integer, Integer> legNumberTable;
    private SelectionTable<SharedDimensions.GPSFix, LegType, LegType> legTypeTable;
    private SelectionTable<SharedDimensions.GPSFix, CompetitorDTO, String> competitorNameTable;
    private SelectionTable<SharedDimensions.GPSFix, CompetitorDTO, String> competitorSailIDTable;
    private SelectionTable<SharedDimensions.GPSFix, String, String> nationalityTable;
    
    public GPSFixSelectionPanel(StringMessages stringMessages, SailingServiceAsync sailingService, ErrorReporter errorReporter) {
        super();
        
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        dimensionsToGroupByBoxes = new ArrayList<ValueListBox<GPSFix>>();
        
        add(createSelectionTables());
        
        add(createFunctionsPanel());
        
        fillSelectionTables();
    }

    @Override
    public Map<SharedDimensions.GPSFix, Collection<?>> getSelection() {
        Map<SharedDimensions.GPSFix, Collection<?>> selection = new HashMap<SharedDimensions.GPSFix, Collection<?>>();
        for (SelectionTable<SharedDimensions.GPSFix,?, ?> table : tablesMappedByDimension.values()) {
            Collection<?> specificSelection = table.getSelection();
            if (!specificSelection.isEmpty()) {
                selection.put(table.getDimension(), specificSelection);
            }
        }
        return selection;
    }

    private void clearSelection() {
        for (SelectionTable<SharedDimensions.GPSFix, ?, ?> table : tablesMappedByDimension.values()) {
            table.clearSelection();
        }
    }

    @Override
    public Collection<SharedDimensions.GPSFix> getDimensionsToGroupBy() {
        Collection<SharedDimensions.GPSFix> dimensionsToGroupBy = new ArrayList<SharedDimensions.GPSFix>();
        for (ValueListBox<GPSFix> dimensionToGroupByBox : dimensionsToGroupByBoxes) {
            if (dimensionToGroupByBox.getValue() != null) {
                dimensionsToGroupBy.add(dimensionToGroupByBox.getValue());
            }
        }
        return dimensionsToGroupBy;
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

                List<RegattaDTO> sortedRegattas = new ArrayList<RegattaDTO>(regattas);
                Collections.sort(sortedRegattas, new Comparator<RegattaDTO>() {
                    @Override
                    public int compare(RegattaDTO o1, RegattaDTO o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                List<BoatClassDTO> sortedBoatClasses = new ArrayList<BoatClassDTO>(boatClasses);
                Collections.sort(sortedBoatClasses, new Comparator<BoatClassDTO>() {
                    @Override
                    public int compare(BoatClassDTO o1, BoatClassDTO o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });

                List<RaceDTO> sortedRaces = new ArrayList<RaceDTO>(races);
                Collections.sort(sortedRaces, new Comparator<RaceDTO>() {
                    @Override
                    public int compare(RaceDTO o1, RaceDTO o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                
                regattaNameTable.setContent(sortedRegattas);
                boatClassTable.setContent(sortedBoatClasses);
                raceNameTable.setContent(sortedRaces);
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
        
        dimensionsToGroupByPanel = new HorizontalPanel();
        dimensionsToGroupByPanel.setSpacing(5);
        dimensionsToGroupByPanel.add(new Label(stringMessages.groupBy() + ": "));
        ValueListBox<SharedDimensions.GPSFix> dimensionToGroupByBox = createDimensionToGroupByBox();
        dimensionsToGroupByPanel.add(dimensionToGroupByBox);
        dimensionsToGroupByBoxes.add(dimensionToGroupByBox);
        functionsPanel.add(dimensionsToGroupByPanel);
        
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
        List<StatisticAndAggregatorType> statistics = Arrays.asList(new StatisticAndAggregatorType(StatisticType.DataAmount, AggregatorType.Average));
        statisticsListBox.setAcceptableValues(statistics);
        statisticsListBox.setValue(statistics.get(0), false);
        functionsPanel.add(statisticsListBox);
        
        return functionsPanel;
    }

    private ValueListBox<SharedDimensions.GPSFix> createDimensionToGroupByBox() {
        ValueListBox<SharedDimensions.GPSFix> dimensionToGroupByBox = new ValueListBox<SharedDimensions.GPSFix>(new Renderer<SharedDimensions.GPSFix>() {
            @Override
            public String render(SharedDimensions.GPSFix gpsFixDimension) {
                if (gpsFixDimension == null) {
                    return "";
                  }
                  return gpsFixDimension.toString();
            }
            @Override
            public void render(SharedDimensions.GPSFix gpsFixDimension, Appendable appendable) throws IOException {
                appendable.append(render(gpsFixDimension));
                
            }
        });
        dimensionToGroupByBox.addValueChangeHandler(new ValueChangeHandler<SharedDimensions.GPSFix>() {
            private boolean firstChange = true;
            @Override
            public void onValueChange(ValueChangeEvent<GPSFix> event) {
                if (firstChange && event.getValue() != null) {
                    ValueListBox<GPSFix> newBox = createDimensionToGroupByBox();
                    dimensionsToGroupByPanel.add(newBox);
                    dimensionsToGroupByBoxes.add(newBox);
                    firstChange = false;
                } else if (event.getValue() == null) {
                    dimensionsToGroupByPanel.remove((Widget) event.getSource());
                    dimensionsToGroupByBoxes.remove(event.getSource());
                }
            }
        });
        dimensionToGroupByBox.setAcceptableValues(Arrays.asList(SharedDimensions.GPSFix.values()));
        return dimensionToGroupByBox;
    }

    private Panel createSelectionTables() {
        HorizontalPanel tablesPanel = new HorizontalPanel();
        tablesPanel.setSpacing(5);
        ScrollPanel tablesScrollPanel = new ScrollPanel(tablesPanel);
        tablesScrollPanel.setHeight("30em");
        tablesMappedByDimension = new HashMap<SharedDimensions.GPSFix, SelectionTable<SharedDimensions.GPSFix, ?,?>>();
        
        regattaNameTable = new SelectionTable<SharedDimensions.GPSFix, RegattaDTO, String>(stringMessages.regatta(), SharedDimensions.GPSFix.RegattaName) {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.getName();
            }
        };
        tablesPanel.add(regattaNameTable);
        tablesMappedByDimension.put(regattaNameTable.getDimension(), regattaNameTable);
        
        boatClassTable = new SelectionTable<SharedDimensions.GPSFix, BoatClassDTO, String>(stringMessages.boatClass(), SharedDimensions.GPSFix.BoatClassName) {
            @Override
            public String getValue(BoatClassDTO boatClass) {
                return boatClass.getName();
            }
        };
        tablesPanel.add(boatClassTable);
        tablesMappedByDimension.put(boatClassTable.getDimension(), boatClassTable);
        
        raceNameTable = new SelectionTable<SharedDimensions.GPSFix, RaceDTO, String>(stringMessages.race(), SharedDimensions.GPSFix.RaceName) {
            @Override
            public String getValue(RaceDTO race) {
                return race.getName();
            }
        };
        tablesPanel.add(raceNameTable);
        tablesMappedByDimension.put(raceNameTable.getDimension(), raceNameTable);
        
        legNumberTable = new SelectionTable<SharedDimensions.GPSFix, Integer, Integer>(stringMessages.legLabel(), SharedDimensions.GPSFix.LegNumber) {
            @Override
            public Integer getValue(Integer legNumber) {
                return legNumber;
            }
        };
        tablesPanel.add(legNumberTable);
        tablesMappedByDimension.put(legNumberTable.getDimension(), legNumberTable);
        
        legTypeTable = new SelectionTable<SharedDimensions.GPSFix, LegType, LegType>(stringMessages.legType(), SharedDimensions.GPSFix.LegType) {
            @Override
            public LegType getValue(LegType legType) {
                return legType;
            }
        };
        tablesPanel.add(legTypeTable);
        tablesMappedByDimension.put(legTypeTable.getDimension(), legTypeTable);
        
        competitorNameTable = new SelectionTable<SharedDimensions.GPSFix, CompetitorDTO, String>(stringMessages.competitor(), SharedDimensions.GPSFix.CompetitorName) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getName();
            }
        };
        tablesPanel.add(competitorNameTable);
        tablesMappedByDimension.put(competitorNameTable.getDimension(), competitorNameTable);
        
        competitorSailIDTable = new SelectionTable<SharedDimensions.GPSFix, CompetitorDTO, String>(stringMessages.sailID(), SharedDimensions.GPSFix.SailID) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getSailID();
            }
        };
        tablesPanel.add(competitorSailIDTable);
        tablesMappedByDimension.put(competitorSailIDTable.getDimension(), competitorSailIDTable);
        
        nationalityTable = new SelectionTable<SharedDimensions.GPSFix, String, String>(stringMessages.nationality(), SharedDimensions.GPSFix.Nationality) {
            @Override
            public String getValue(String nationality) {
                return nationality;
            }
        };
        tablesPanel.add(nationalityTable);
        tablesMappedByDimension.put(nationalityTable.getDimension(), nationalityTable);
        
        return tablesScrollPanel;
    }

}
