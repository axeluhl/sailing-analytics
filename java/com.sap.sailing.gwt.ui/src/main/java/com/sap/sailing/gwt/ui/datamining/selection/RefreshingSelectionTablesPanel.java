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

import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
import com.sap.sailing.gwt.ui.datamining.SelectionProvider;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RefreshingSelectionTablesPanel implements SelectionProvider {
    
    private static final int resizeDelay = 100;
    private static final double relativeWidthInPercent = 1;
    private static final int widthMargin = 17;
    private static final double relativeHeightInPercent = 0.35;

    private static final int refreshRate = 5000;
    
    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    
    private SimplePanel widget;

    private Timer timer;
    private Map<SharedDimension, SelectionTable<?, ?>> tablesMappedByDimension;
    
    public RefreshingSelectionTablesPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;

        tablesMappedByDimension = new HashMap<SharedDimension, SelectionTable<?,?>>();
        widget = new SimplePanel();
        widget.setWidget(createTables());
        
        timer = new Timer() {
            @Override
            public void run() {
                updateTables();
            }
        };

        Window.addResizeHandler(new ResizeHandler() {
            private final Timer timer = new Timer() {
                @Override
                public void run() {
                    doLayout(Window.getClientWidth(), Window.getClientHeight());
                }
            };
            @Override
            public void onResize(ResizeEvent event) {
                timer.schedule(resizeDelay);
            }
        });

        updateTables();
        doLayout(Window.getClientWidth(), Window.getClientHeight());
    }
    
    private void doLayout(int newWindowWidth, int newWindowHeight) {
        int absoluteWidth = (int) ((newWindowWidth - widthMargin) * relativeWidthInPercent);
        int absoluteHeight = (int) (newWindowHeight * relativeHeightInPercent);
        
        widget.setWidth(absoluteWidth + "px");
        widget.setHeight(absoluteHeight + "px");
        
        Collection<SelectionTable<?, ?>> tables = tablesMappedByDimension.values();
        for (SelectionTable<?, ?> table : tables) {
            table.setWidth((absoluteWidth / tables.size()) + "px");
            table.setHeight(absoluteHeight + "px");
        }
    }

    @Override
    public void addSelectionChangeHandler(Handler handler) {
        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            table.addSelectionChangeHandler(handler);
        }
    }

    @Override
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

    @Override
    public void applySelection(QueryDefinition queryDefinition) {
        for (Entry<SharedDimension, Iterable<?>> selectionEntry : queryDefinition.getSelection().entrySet()) {
            SelectionTable<?, ?> selectionTable = tablesMappedByDimension.get(selectionEntry.getKey());
            selectionTable.setSelection((Iterable<?>) selectionEntry.getValue());
        }
    }

    @Override
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
    
    @Override
    public Widget getWidget() {
        return widget;
    }

    private void updateTables() {
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
                regattaTable.updateContent(sortedRegattas);
                
                SelectionTable<BoatClassDTO, ?> boatClassTable = getTable(SharedDimension.BoatClassName);
                boatClassTable.updateContent(sortedBoatClasses);
                
                SelectionTable<RaceDTO, ?> raceNameTable = getTable(SharedDimension.RaceName);
                raceNameTable.updateContent(sortedRaces);
                
                SelectionTable<Integer, ?> legNumberTable = getTable(SharedDimension.LegNumber);
                legNumberTable.updateContent(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
                
                SelectionTable<LegType, ?> legTypeTable = getTable(SharedDimension.LegType);
                legTypeTable.updateContent(Arrays.asList(LegType.values()));
                
                SelectionTable<CompetitorDTO, ?> competitorNameTable = getTable(SharedDimension.CompetitorName);
                competitorNameTable.updateContent(competitors);
                
                SelectionTable<CompetitorDTO, ?> competitorSailIDTable = getTable(SharedDimension.SailID);
                competitorSailIDTable.updateContent(competitors);
                
                SelectionTable<String, ?> nationalityTable = getTable(SharedDimension.Nationality);
                nationalityTable.updateContent(nationalities);
                
                timer.schedule(refreshRate);
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
        
        return tablesPanel;
    }

}
