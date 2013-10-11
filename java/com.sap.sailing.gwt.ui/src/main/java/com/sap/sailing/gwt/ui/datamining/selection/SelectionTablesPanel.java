package com.sap.sailing.gwt.ui.datamining.selection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.SharedDimension;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class SelectionTablesPanel extends SimplePanel {
    
    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;

    private Map<SharedDimension, SelectionTable<?, ?>> tablesMappedByDimension;
    
    public SelectionTablesPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        
        tablesMappedByDimension = new HashMap<SharedDimension, SelectionTable<?,?>>();
        
        setWidget(createTables());
        fillTables();
    }

    public void addSelectionChangeHandler(Handler handler) {
        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            table.addSelectionChangeHandler(handler);
        }
    }

    public Map<SharedDimension, Collection<?>> getSelection() {
        Map<SharedDimension, Collection<?>> selection = new HashMap<SharedDimension, Collection<?>>();
        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            Collection<?> specificSelection = table.getSelection();
            if (!specificSelection.isEmpty()) {
                selection.put(table.getDimension(), specificSelection);
            }
        }
        return selection;
    }

    public void applySelection(QueryDefinition queryDefinition) {
        for (Entry<SharedDimension, Iterable<?>> selectionEntry : queryDefinition.getSelection().entrySet()) {
            SelectionTable<?, ?> selectionTable = tablesMappedByDimension.get(selectionEntry.getKey());
            selectionTable.setSelection((Iterable<?>) selectionEntry.getValue());
        }
    }

    public void clearSelection() {
        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            table.clearSelection();
        }
    }
    
    @SuppressWarnings("unchecked") //You can't use instanceof for generic type parameters
    private <ContentType> SelectionTable<ContentType, ?> getTable(SharedDimension dimension) {
        try {
            return (SelectionTable<ContentType, ?>) tablesMappedByDimension.get(dimension);
        } catch (ClassCastException e) {
            return null;
        }
    }

    private void fillTables() {
        sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the regattas from the server: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<RegattaDTO> regattas) {
                Set<RegattaDTO> regattasWithData = new HashSet<RegattaDTO>();
                Set<BoatClassDTO> boatClasses = new HashSet<BoatClassDTO>();
                Set<RaceDTO> races = new HashSet<RaceDTO>();
                Set<CompetitorDTO> competitors = new HashSet<CompetitorDTO>();
                Set<String> nationalities = new HashSet<String>();
                for (RegattaDTO regatta : regattas) {
                    if (regattaContainsData(regatta)) {
                        regattasWithData.add(regatta);
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

                List<RegattaDTO> sortedRegattas = new ArrayList<RegattaDTO>(regattasWithData);
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

                SelectionTable<RegattaDTO, ?> regattaTable = getTable(SharedDimension.RegattaName);
                regattaTable.setContent(sortedRegattas);
                
                SelectionTable<BoatClassDTO, ?> boatClassTable = getTable(SharedDimension.BoatClassName);
                boatClassTable.setContent(sortedBoatClasses);
                
                SelectionTable<RaceDTO, ?> raceNameTable = getTable(SharedDimension.RaceName);
                raceNameTable.setContent(sortedRaces);
                
                SelectionTable<Integer, ?> legNumberTable = getTable(SharedDimension.LegNumber);
                legNumberTable.setContent(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
                
                SelectionTable<LegType, ?> legTypeTable = getTable(SharedDimension.LegType);
                legTypeTable.setContent(Arrays.asList(LegType.values()));
                
                SelectionTable<CompetitorDTO, ?> competitorNameTable = getTable(SharedDimension.CompetitorName);
                competitorNameTable.setContent(competitors);
                
                SelectionTable<CompetitorDTO, ?> competitorSailIDTable = getTable(SharedDimension.SailID);
                competitorSailIDTable.setContent(competitors);
                
                SelectionTable<String, ?> nationalityTable = getTable(SharedDimension.Nationality);
                nationalityTable.setContent(nationalities);
            }
        });
    }

    private boolean regattaContainsData(RegattaDTO regatta) {
        if (regatta != null) {
            for (RaceDTO race : regatta.races) {
                if (race.isTracked || race.status.status == TrackedRaceStatusEnum.FINISHED) {
                    return true;
                }
            }
        }
        return false;
    }

    private Widget createTables() {
        HorizontalPanel tablesPanel = new HorizontalPanel();
        ScrollPanel tablesScrollPanel = new ScrollPanel(tablesPanel);
        tablesScrollPanel.setHeight("21em");
        tablesMappedByDimension = new HashMap<SharedDimension, SelectionTable<?, ?>>();

        SelectionTable<RegattaDTO, String> regattaNameTable = new SelectionTable<RegattaDTO, String>(stringMessages
                .regatta(), SharedDimension.RegattaName) {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.getName();
            }
        };
        tablesPanel.add(regattaNameTable);
        tablesMappedByDimension.put(regattaNameTable.getDimension(), regattaNameTable);

        SelectionTable<BoatClassDTO, String> boatClassTable = new SelectionTable<BoatClassDTO, String>(stringMessages
                .boatClass(), SharedDimension.BoatClassName) {
            @Override
            public String getValue(BoatClassDTO boatClass) {
                return boatClass.getName();
            }
        };
        tablesPanel.add(boatClassTable);
        tablesMappedByDimension.put(boatClassTable.getDimension(), boatClassTable);

        SelectionTable<RaceDTO, String> raceNameTable = new SelectionTable<RaceDTO, String>(stringMessages.race(),
                SharedDimension.RaceName) {
            @Override
            public String getValue(RaceDTO race) {
                return race.getName();
            }
        };
        tablesPanel.add(raceNameTable);
        tablesMappedByDimension.put(raceNameTable.getDimension(), raceNameTable);

        SelectionTable<Integer, Integer> legNumberTable = new SimpleSelectionTable<Integer>(stringMessages.legLabel(),
                SharedDimension.LegNumber);
        tablesPanel.add(legNumberTable);
        tablesMappedByDimension.put(legNumberTable.getDimension(), legNumberTable);

        SelectionTable<LegType, LegType> legTypeTable = new SimpleSelectionTable<LegType>(stringMessages.legType(),
                SharedDimension.LegType);
        tablesPanel.add(legTypeTable);
        tablesMappedByDimension.put(legTypeTable.getDimension(), legTypeTable);

        SelectionTable<CompetitorDTO, String> competitorNameTable = new SelectionTable<CompetitorDTO, String>(stringMessages
                .competitor(), SharedDimension.CompetitorName) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getName();
            }
        };
        tablesPanel.add(competitorNameTable);
        tablesMappedByDimension.put(competitorNameTable.getDimension(), competitorNameTable);

        SelectionTable<CompetitorDTO, String> competitorSailIDTable = new SelectionTable<CompetitorDTO, String>(stringMessages
                .sailID(), SharedDimension.SailID) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getSailID();
            }
        };
        tablesPanel.add(competitorSailIDTable);
        tablesMappedByDimension.put(competitorSailIDTable.getDimension(), competitorSailIDTable);

        SelectionTable<String, String> nationalityTable = new SimpleSelectionTable<String>(stringMessages
                .nationality(), SharedDimension.Nationality);
        tablesPanel.add(nationalityTable);
        tablesMappedByDimension.put(nationalityTable.getDimension(), nationalityTable);
        
        return tablesScrollPanel;
    }

}
