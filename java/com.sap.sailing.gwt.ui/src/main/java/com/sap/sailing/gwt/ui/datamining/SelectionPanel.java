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
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.sap.sailing.datamining.shared.Dimension;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class SelectionPanel extends FlowPanel implements QuerySelectionProvider {
    
    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    
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
        
        Button clearSelectionButton = new Button(this.stringMessages.clearSelection());
        clearSelectionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                clearSelection();
            }
        });
        add(clearSelectionButton);
        
        add(createSelectionTables());
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
