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
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.datamining.shared.QueryDefinition;
import com.sap.sailing.datamining.shared.DimensionIdentifier;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.datamining.SelectionChangedListener;
import com.sap.sailing.gwt.ui.datamining.SelectionProvider;
import com.sap.sailing.gwt.ui.datamining.settings.RefreshingSelectionTablesSettings;
import com.sap.sailing.gwt.ui.datamining.settings.RefreshingSelectionTablesSettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.RaceWithCompetitorsDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;

public class RefreshingSelectionTablesPanel implements SelectionProvider<RefreshingSelectionTablesSettings> {
    
    private static final int resizeDelay = 100;
    private static final double relativeWidthInPercent = 1;
    private static final int widthMargin = 17;
    private static final double relativeHeightInPercent = 0.35;

    private StringMessages stringMessages;
    private SailingServiceAsync sailingService;
    private ErrorReporter errorReporter;
    
    private SimplePanel entryWidget;

    private RefreshingSelectionTablesSettings settings;
    private Timer timer;
    private Map<DimensionIdentifier, SelectionTable<?, ?>> tablesMappedByDimension;
    private Set<SelectionChangedListener> listeners;
    
    public RefreshingSelectionTablesPanel(StringMessages stringMessages, SailingServiceAsync sailingService,
            ErrorReporter errorReporter) {
        this.stringMessages = stringMessages;
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        settings = new RefreshingSelectionTablesSettings();
        listeners = new HashSet<SelectionChangedListener>();

        tablesMappedByDimension = new HashMap<DimensionIdentifier, SelectionTable<?,?>>();
        entryWidget = new SimplePanel();
        entryWidget.setWidget(createTables());
        
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
        
        entryWidget.setWidth(absoluteWidth + "px");
        entryWidget.setHeight(absoluteHeight + "px");
        
        Collection<SelectionTable<?, ?>> tables = tablesMappedByDimension.values();
        for (SelectionTable<?, ?> table : tables) {
            table.setWidth((absoluteWidth / tables.size()) + "px");
            table.setHeight(absoluteHeight + "px");
        }
    }

    @Override
    public void addSelectionChangedListener(SelectionChangedListener listener) {
        listeners.add(listener);
        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            table.addSelectionChangeHandler(new Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent event) {
                    notifySelectionChanged();
                }
            });
        }
    }

    private void notifySelectionChanged() {
        for (SelectionChangedListener listener : listeners) {
            listener.selectionChanged();
        }
    }

    @Override
    public Map<DimensionIdentifier, Collection<?>> getSelection() {
        Map<DimensionIdentifier, Collection<?>> selection = new HashMap<DimensionIdentifier, Collection<?>>();
        for (SelectionTable<?, ?> table : tablesMappedByDimension.values()) {
            Collection<?> specificSelection = table.getSelectionAsValues();
            if (!specificSelection.isEmpty()) {
                selection.put(table.getDimension(), specificSelection);
            }
        }
        return selection;
    }

    @Override
    public void applySelection(QueryDefinition queryDefinition) {
        for (Entry<DimensionIdentifier, Iterable<?>> selectionEntry : queryDefinition.getSelection().entrySet()) {
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
    private <ContentType> SelectionTable<ContentType, ?> getTable(DimensionIdentifier dimension) {
        try {
            return (SelectionTable<ContentType, ?>) tablesMappedByDimension.get(dimension);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.selectionTables();
    }

    @Override
    public Widget getEntryWidget() {
        return entryWidget;
    }

    @Override
    public boolean isVisible() {
        return entryWidget.isVisible();
    }

    @Override
    public void setVisible(boolean visibility) {
        entryWidget.setVisible(visibility);
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<RefreshingSelectionTablesSettings> getSettingsDialogComponent() {
        return new RefreshingSelectionTablesSettingsDialogComponent(settings, stringMessages);
    }

    @Override
    public void updateSettings(RefreshingSelectionTablesSettings newSettings) {
        if (settings.isRefreshAutomatically() != newSettings.isRefreshAutomatically()) {
            if (newSettings.isRefreshAutomatically()) {
                updateTables();
            } else {
                timer.cancel();
            }
        }
        
        if (settings.getRefreshIntervalInMilliseconds() != newSettings.getRefreshIntervalInMilliseconds()) {
            if (newSettings.isRefreshAutomatically()) {
                timer.cancel();
                timer.schedule(newSettings.getRefreshIntervalInMilliseconds());
            }
        }
        
        settings = newSettings;
    }

    private void updateTables() {
        sailingService.getRegattas(new AsyncCallback<List<RegattaDTO>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error fetching the regattas from the server: " + caught.getMessage());
            }

            @Override
            public void onSuccess(List<RegattaDTO> regattas) {
                boolean tableContentChanged = false;
                Map<DimensionIdentifier, Collection<?>> content = extractContent(regattas);
                for (Entry<DimensionIdentifier, Collection<?>> contentEntry : content.entrySet()) {
                    boolean currentTableContentChanged = getTable(contentEntry.getKey()).updateContent(contentEntry.getValue());
                    if (!tableContentChanged) {
                        tableContentChanged = currentTableContentChanged;
                    }
                }
                
                if (settings.isRerunQueryAfterRefresh() && tableContentChanged) {
                    notifySelectionChanged();
                    // TODO query is executed, before the data is ready to be analyzed
                }
                if (settings.isRefreshAutomatically()) {
                    timer.schedule(settings.getRefreshIntervalInMilliseconds());
                }
            }
        });
    }
    
    private Map<DimensionIdentifier, Collection<?>> extractContent(Collection<RegattaDTO> regattas) {
        Map<DimensionIdentifier, Collection<?>> content = new HashMap<DimensionIdentifier, Collection<?>>();
        
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
        
        content.put(DimensionIdentifier.RegattaName, sortedRegattas);
        content.put(DimensionIdentifier.BoatClassName, sortedBoatClasses);
        content.put(DimensionIdentifier.RaceName, sortedRaces);
        content.put(DimensionIdentifier.LegNumber, Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        content.put(DimensionIdentifier.LegType, Arrays.asList(LegType.values()));
        content.put(DimensionIdentifier.CompetitorName, competitors);
        content.put(DimensionIdentifier.SailID, competitors);
        content.put(DimensionIdentifier.Nationality, nationalities);
        
        return content;
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
        tablesMappedByDimension = new HashMap<DimensionIdentifier, SelectionTable<?, ?>>();

        SelectionTable<RegattaDTO, String> regattaNameTable = new SelectionTable<RegattaDTO, String>(stringMessages
                .regatta(), DimensionIdentifier.RegattaName) {
            @Override
            public String getValue(RegattaDTO regatta) {
                return regatta.getName();
            }
        };
        tablesPanel.add(regattaNameTable);
        tablesMappedByDimension.put(regattaNameTable.getDimension(), regattaNameTable);

        SelectionTable<BoatClassDTO, String> boatClassTable = new SelectionTable<BoatClassDTO, String>(stringMessages
                .boatClass(), DimensionIdentifier.BoatClassName) {
            @Override
            public String getValue(BoatClassDTO boatClass) {
                return boatClass.getName();
            }
        };
        tablesPanel.add(boatClassTable);
        tablesMappedByDimension.put(boatClassTable.getDimension(), boatClassTable);

        SelectionTable<RaceDTO, String> raceNameTable = new SelectionTable<RaceDTO, String>(stringMessages.race(),
                DimensionIdentifier.RaceName) {
            @Override
            public String getValue(RaceDTO race) {
                return race.getName();
            }
        };
        tablesPanel.add(raceNameTable);
        tablesMappedByDimension.put(raceNameTable.getDimension(), raceNameTable);

        SelectionTable<LegType, LegType> legTypeTable = new SimpleSelectionTable<LegType>(stringMessages.legType(),
                DimensionIdentifier.LegType);
        tablesPanel.add(legTypeTable);
        tablesMappedByDimension.put(legTypeTable.getDimension(), legTypeTable);

        SelectionTable<Integer, Integer> legNumberTable = new SimpleSelectionTable<Integer>(stringMessages.legLabel(),
                DimensionIdentifier.LegNumber);
        tablesPanel.add(legNumberTable);
        tablesMappedByDimension.put(legNumberTable.getDimension(), legNumberTable);

        SelectionTable<String, String> nationalityTable = new SimpleSelectionTable<String>(stringMessages
                .nationality(), DimensionIdentifier.Nationality);
        tablesPanel.add(nationalityTable);
        tablesMappedByDimension.put(nationalityTable.getDimension(), nationalityTable);

        SelectionTable<CompetitorDTO, String> competitorNameTable = new SelectionTable<CompetitorDTO, String>(stringMessages
                .competitor(), DimensionIdentifier.CompetitorName) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getName();
            }
        };
        tablesPanel.add(competitorNameTable);
        tablesMappedByDimension.put(competitorNameTable.getDimension(), competitorNameTable);

        SelectionTable<CompetitorDTO, String> competitorSailIDTable = new SelectionTable<CompetitorDTO, String>(stringMessages
                .sailID(), DimensionIdentifier.SailID) {
            @Override
            public String getValue(CompetitorDTO competitor) {
                return competitor.getSailID();
            }
        };
        tablesPanel.add(competitorSailIDTable);
        tablesMappedByDimension.put(competitorSailIDTable.getDimension(), competitorSailIDTable);
        
        return tablesPanel;
    }

}
