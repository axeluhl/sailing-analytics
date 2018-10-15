package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.AbstractCellTable;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.panels.CustomizableFilterablePanel;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractTrackedRacesListComposite extends AbstractCompositeComponent<TrackedRacesSettings> implements
        RegattasDisplayer {

    protected final long DEFAULT_LIVE_DELAY_IN_MILLISECONDS = 5000;

    private final boolean multiSelection;

    protected RefreshableSelectionModel<RaceDTO> refreshableSelectionModel;
    
    protected CellTable<RaceDTO> raceTable;

    private ListDataProvider<RaceDTO> raceList;

    private Iterable<RaceDTO> allRaces;

    private Label noTrackedRacesLabel;

    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    protected final RegattaRefresher regattaRefresher;
    protected final StringMessages stringMessages;

    private Button btnRefresh;

    private CustomizableFilterablePanel<RaceDTO> filterablePanelRaces;

    protected TrackedRacesSettings settings;

    private ListBox listBoxRegattas;

    public static class AnchorCell extends AbstractCell<SafeHtml> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    public AbstractTrackedRacesListComposite(Component<?> parent, ComponentContext<?> context,
            final SailingServiceAsync sailingService,
            final ErrorReporter errorReporter, final RegattaRefresher regattaRefresher,
            final StringMessages stringMessages, boolean hasMultiSelection) {
        super(parent, context);
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;
        this.multiSelection = hasMultiSelection;
        this.stringMessages = stringMessages;
    }

    public void setRegattaFilterValue(String regattaName) {
        for (int i = 0; i < listBoxRegattas.getItemCount(); i++) {
            if (listBoxRegattas.getValue(i).equals(regattaName)) {
                listBoxRegattas.setSelectedIndex(i);
                // Firing change event on combobox to filter
                DomEvent.fireNativeEvent(Document.get().createChangeEvent(), listBoxRegattas);
                return;
            }
        }

        // Set 'All' option in case there are no tracked race related to regatta
        if (listBoxRegattas.getItemCount() > 0) {
            listBoxRegattas.setSelectedIndex(0);
            DomEvent.fireNativeEvent(Document.get().createChangeEvent(), listBoxRegattas);
        }
    }
    
    protected void createUI() {
        settings = new TrackedRacesSettings();
        settings.setDelayToLiveInSeconds(DEFAULT_LIVE_DELAY_IN_MILLISECONDS / 1000l);
        VerticalPanel panel = new VerticalPanel();
        initWidget(panel);
        HorizontalPanel filterPanel = new HorizontalPanel();
        panel.add(filterPanel);
        
        noTrackedRacesLabel = new Label(stringMessages.noRacesYet());
        noTrackedRacesLabel.setWordWrap(false);
        panel.add(noTrackedRacesLabel);
        TableWrapper<RaceDTO, RefreshableSelectionModel<RaceDTO>> raceTableWrapper = new TrackedRacesTableWrapper(sailingService, stringMessages, errorReporter, multiSelection, /* enablePager */ true);
        raceTable = raceTableWrapper.getTable();
        raceTable.setPageSize(1000);
        raceTable.ensureDebugId("TrackedRacesCellTable");
        
        Label lblFilterRaces = new Label(stringMessages.filterRaces()+":");
        lblFilterRaces.setWordWrap(false);
        lblFilterRaces.getElement().getStyle().setFontWeight(FontWeight.BOLD);
        lblFilterRaces.getElement().getStyle().setMarginRight(10, Unit.PX);
        filterPanel.add(lblFilterRaces);
        filterPanel.setCellVerticalAlignment(lblFilterRaces, HasVerticalAlignment.ALIGN_MIDDLE);
        raceList = raceTableWrapper.getDataProvider();
        filterablePanelRaces = new CustomizableFilterablePanel<RaceDTO>(allRaces, raceList) {            
            @Override
            public List<String> getSearchableStrings(RaceDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.getName());
                strings.add(t.boatClass);
                strings.add(t.getRegattaName());
                return strings;
            }

            @Override
            public AbstractCellTable<RaceDTO> getCellTable() {
                return raceTable;
            }
        };
        raceTableWrapper.registerSelectionModelOnNewDataProvider(filterablePanelRaces.getAllListDataProvider());
        Label lblFilterByRegatta = new Label(stringMessages.filterByRegatta());
        lblFilterByRegatta.setWordWrap(false);
        listBoxRegattas = new ListBox();
        listBoxRegattas.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                filterablePanelRaces.filter();
            }
        });
        filterablePanelRaces.add(lblFilterByRegatta, listBoxRegattas, new Filter<RaceDTO>() {
            @Override
            public boolean matches(RaceDTO t) {
                return listBoxRegattas.getSelectedIndex() == 0 /* All */ || Util.equalsWithNull(listBoxRegattas.getSelectedValue(), t.getRegattaName());
            }
            @Override
            public String getName() {
                return "TrackedRacesByRegattaFilter";
            }
        });

        Label lblFilterRacesByName = new Label(stringMessages.filterByNameOrBoatClass() + ":");
        lblFilterRacesByName.setWordWrap(false);
        filterablePanelRaces.add(lblFilterRacesByName);
        filterablePanelRaces.addDefaultTextBox();
        filterablePanelRaces.getTextBox().ensureDebugId("TrackedRacesFilterTextBox");
        
        filterPanel.add(filterablePanelRaces);
        filterPanel.setCellVerticalAlignment(filterablePanelRaces, HasVerticalAlignment.ALIGN_MIDDLE);

        refreshableSelectionModel = raceTableWrapper.getSelectionModel();
        setupTableColumns(stringMessages, raceTableWrapper.getColumnSortHandler());
        raceTable.setWidth("300px");

        raceTable.setVisible(false);
        panel.add(raceTableWrapper);
        refreshableSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<RaceDTO> selectedRaces = refreshableSelectionModel.getSelectedSet();
                makeControlsReactToSelectionChange(selectedRaces);
            }
        });
        
        HorizontalPanel trackedRacesButtonPanel = new HorizontalPanel();
        trackedRacesButtonPanel.setSpacing(10);
        panel.add(trackedRacesButtonPanel);

        btnRefresh = new Button(stringMessages.refresh());
        btnRefresh.ensureDebugId("RefreshButton");
        btnRefresh.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                regattaRefresher.fillRegattas();
            }
        });
        trackedRacesButtonPanel.add(btnRefresh);
        addControlButtons(trackedRacesButtonPanel);
    }
    
    abstract protected void makeControlsReactToSelectionChange(Set<RaceDTO> selectedRaces);

    abstract protected void makeControlsReactToFillRegattas(Iterable<RegattaDTO> regattas);

    abstract protected void addControlButtons(HorizontalPanel trackedRacesButtonPanel);

    private void setupTableColumns(final StringMessages stringMessages, ListHandler<RaceDTO> columnSortHandler) {
        TextColumn<RaceDTO> regattaNameColumn = new TextColumn<RaceDTO>() {
            @Override
            public String getValue(RaceDTO raceDTO) {
                return raceDTO.getRegattaName();
            }
        };
        regattaNameColumn.setSortable(true);
        columnSortHandler.setComparator(regattaNameColumn, new Comparator<RaceDTO>() {
            @Override
            public int compare(RaceDTO r1, RaceDTO r2) {
                return new NaturalComparator().compare(r1.getRegattaName(), r2.getRegattaName());
            }
        });

        TextColumn<RaceDTO> boatClassNameColumn = new TextColumn<RaceDTO>() {
            @Override
            public String getValue(RaceDTO raceDTO) {
                return raceDTO.boatClass == null ? "" : raceDTO.boatClass;
            }
        };
        boatClassNameColumn.setSortable(true);
        columnSortHandler.setComparator(boatClassNameColumn, new Comparator<RaceDTO>() {
            @Override
            public int compare(RaceDTO r1, RaceDTO r2) {
                return new NaturalComparator(false).compare(r1.boatClass, r2.boatClass);
            }
        });

        AnchorCell anchorCell = new AnchorCell();
        Column<RaceDTO, SafeHtml> raceNameColumn = new Column<RaceDTO, SafeHtml>(anchorCell) {
            @Override
            public SafeHtml getValue(RaceDTO raceDTO) {
                return SafeHtmlUtils.fromString(raceDTO.getName());
            }
        };
        raceNameColumn.setSortable(true);
        columnSortHandler.setComparator(raceNameColumn, new Comparator<RaceDTO>() {
            @Override
            public int compare(RaceDTO r1, RaceDTO r2) {
                return new NaturalComparator().compare(r1.getName(), r2.getName());
            }
        });

        TextColumn<RaceDTO> raceStartColumn = new TextColumn<RaceDTO>() {
            @Override
            public String getValue(RaceDTO raceDTO) {
                if (raceDTO.startOfRace != null) {
                    return DateAndTimeFormatterUtil.defaultDateFormatter.render(raceDTO.startOfRace) + " " + 
                            DateAndTimeFormatterUtil.defaultTimeFormatter.render(raceDTO.startOfRace);
                }

                return "";
            }
        };
        raceStartColumn.setSortable(true);
        columnSortHandler.setComparator(raceStartColumn, new Comparator<RaceDTO>() {
            @Override
            public int compare(RaceDTO r1, RaceDTO r2) {
                if (r1.startOfRace != null && r2.startOfRace != null) {
                    return r1.startOfRace.compareTo(r2.startOfRace);
                }

                return r1.startOfRace == null ? (r2.startOfRace == null ? 0 : -1) : 1;
            }
        });

        TextColumn<RaceDTO> hasWindDataColumn = new TextColumn<RaceDTO>() {
            @Override
            public String getValue(RaceDTO raceDTO) {
                if (raceDTO.trackedRace != null && raceDTO.trackedRace.hasWindData == true)
                    return stringMessages.yes();
                else
                    return stringMessages.no();
            }
        };
        hasWindDataColumn.setSortable(true);
        columnSortHandler.setComparator(hasWindDataColumn, new Comparator<RaceDTO>() {

            @Override
            public int compare(RaceDTO r1, RaceDTO r2) {
                return new Boolean(hasWindData(r1)).compareTo(hasWindData(r2));
            }

            private boolean hasWindData(RaceDTO race) {
                return race.trackedRace != null && race.trackedRace.hasWindData == true;
            }

        });

        TextColumn<RaceDTO> hasGPSDataColumn = new TextColumn<RaceDTO>() {
            @Override
            public String getValue(RaceDTO raceDTO) {
                if (raceDTO.trackedRace != null && raceDTO.trackedRace.hasGPSData == true) {
                    return stringMessages.yes();
                } else {
                    return stringMessages.no();
                }
            }
        };
        hasGPSDataColumn.setSortable(true);
        columnSortHandler.setComparator(hasGPSDataColumn, new Comparator<RaceDTO>() {

            @Override
            public int compare(RaceDTO r1, RaceDTO r2) {
                return new Boolean(hasGPSData(r1)).compareTo(hasGPSData(r2));
            }

            private boolean hasGPSData(RaceDTO race) {
                return race.trackedRace != null && race.trackedRace.hasGPSData == true;
            }

        });

        TextColumn<RaceDTO> raceStatusColumn = new TextColumn<RaceDTO>() {
            @Override
            public String getValue(RaceDTO raceDTO) {
                return raceDTO.status == null ? "" : raceDTO.status.toString();
            }
        };
        raceStatusColumn.setSortable(true);
        columnSortHandler.setComparator(raceStatusColumn, new Comparator<RaceDTO>() {
            @Override
            public int compare(RaceDTO r1, RaceDTO r2) {
                if (r1.status != null && r2.status != null) {
                    if (r1.status.status == TrackedRaceStatusEnum.LOADING
                            && r2.status.status == TrackedRaceStatusEnum.LOADING) {
                        return new Double(r1.status.loadingProgress).compareTo(r2.status.loadingProgress);
                    }
                    return new Integer(r1.status.status.getOrder()).compareTo(r2.status.status.getOrder());
                }

                return r1.status == null ? (r2.status == null ? 0 : -1) : 1;
            }
        });

        TextColumn<RaceDTO> raceLiveDelayColumn = new TextColumn<RaceDTO>() {
            @Override
            public String getValue(RaceDTO raceDTO) {
                if (raceDTO.isTracked && raceDTO.trackedRace != null && raceDTO.trackedRace.delayToLiveInMs > 0) {
                    return "" + raceDTO.trackedRace.delayToLiveInMs / 1000;
                }
                return "";
            }
        };
        raceLiveDelayColumn.setSortable(true);
        columnSortHandler.setComparator(raceLiveDelayColumn, new Comparator<RaceDTO>() {
            @Override
            public int compare(RaceDTO r1, RaceDTO r2) {
                Long r1Delay = getDelay(r1);
                Long r2Delay = getDelay(r2);
                if (r1Delay != null && r2Delay != null) {
                    return r1Delay.compareTo(r2Delay);
                }

                return r1Delay == null ? (r2Delay == null ? 0 : -1) : 1;
            }

            private Long getDelay(RaceDTO race) {
                return race.isTracked && race.trackedRace != null ? race.trackedRace.delayToLiveInMs : null;
            }
        });
        raceTable.addColumn(regattaNameColumn, stringMessages.regatta());
        raceTable.addColumn(boatClassNameColumn, stringMessages.boatClass());
        raceTable.addColumn(raceNameColumn, stringMessages.race());
        raceTable.addColumn(raceStartColumn, stringMessages.startTime());
        raceTable.addColumn(hasWindDataColumn, stringMessages.windData());
        raceTable.addColumn(hasGPSDataColumn, stringMessages.gpsData());
        raceTable.addColumn(raceStatusColumn, stringMessages.status());
        raceTable.addColumn(raceLiveDelayColumn, stringMessages.delayInSeconds());
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<TrackedRacesSettings> getSettingsDialogComponent(TrackedRacesSettings settings) {
        return new TrackedRacesSettingsDialogComponent<TrackedRacesSettings>(settings, stringMessages);
    }

    @Override
    public void updateSettings(TrackedRacesSettings newSettings) {
        settings.setDelayToLiveInSeconds(newSettings.getDelayToLiveInSeconds());

        // set the new delay to all selected races
        List<RegattaAndRaceIdentifier> raceIdentifiersToUpdate = new ArrayList<RegattaAndRaceIdentifier>();
        for (RaceDTO raceDTO : refreshableSelectionModel.getSelectedSet()) {
            raceIdentifiersToUpdate.add(raceDTO.getRaceIdentifier());
        }

        if (raceIdentifiersToUpdate != null && !raceIdentifiersToUpdate.isEmpty()) {
            sailingService.updateRacesDelayToLive(raceIdentifiersToUpdate, settings.getDelayToLiveInSeconds() * 1000l,
                    new MarkedAsyncCallback<Void>(
                            new AsyncCallback<Void>() {
                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError(
                                            "Exception trying to set the delay to live for the selected tracked races: "
                                                    + caught.getMessage());
                                }

                                @Override
                                public void onSuccess(Void result) {
                                    regattaRefresher.fillRegattas();
                                }
                            }
                    ));
        }
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.trackedRaces();
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    public void addRaceSelectionChangeHandler(Handler handler) {
        refreshableSelectionModel.addSelectionChangeHandler(handler);
    }

    public RaceDTO getRaceByIdentifier(RaceIdentifier raceIdentifier) {
        RaceDTO result = null;
        if (raceList != null) {
            for (RaceDTO race : raceList.getList()) {
                if (race.getRaceIdentifier().equals(raceIdentifier)) {
                    result = race;
                    break;
                }
            }
        }
        return result;
    }

    public void selectRaceByIdentifier(RegattaAndRaceIdentifier raceIdentifier) {
        if (raceList != null) {
            for (RaceDTO race : raceList.getList()) {
                String regattaName = race.getRegattaName();
                if (regattaName.equals(raceIdentifier.getRegattaName())
                        && race.getName().equals(raceIdentifier.getRaceName())) {
                    refreshableSelectionModel.setSelected(race, true);
                    break;
                }
            }
        }
    }

    public void clearSelection() {
        refreshableSelectionModel.clear();
    }

    /**
     * @param regattas
     */
    @Override
    public void fillRegattas(Iterable<RegattaDTO> regattas) {
        makeControlsReactToFillRegattas(regattas);
        displayRaceTableUI(regattas);
        final List<RaceDTO> newAllRaces = new ArrayList<RaceDTO>();
        final List<String> regattaNames = new ArrayList<>();
        for (RegattaDTO regatta : regattas) {
            for (RaceDTO race : regatta.races) {
                if (race != null) {
                    if (raceIsToBeAddedToList(race)) {
                        // We need only those regatta names which are available
                        // at tracking table
                        if (!regattaNames.contains(regatta.getName())) {
                            regattaNames.add(regatta.getName());
                        }
                        newAllRaces.add(race);
                    }
                }
            }
        }
        refreshListBoxRegattas(regattaNames);
        allRaces = newAllRaces;
        filterablePanelRaces.updateAll(allRaces);
    }

    private void refreshListBoxRegattas(List<String> regattaNames) {
        final String lastSelectedRegattaName = listBoxRegattas.getSelectedValue();
        listBoxRegattas.clear();
        listBoxRegattas.addItem(stringMessages.all(), "");
        regattaNames.stream().sorted().forEach(regatta -> listBoxRegattas.addItem(regatta, regatta));
        restoreListBoxRegattasSelection(lastSelectedRegattaName);
    }

    private void restoreListBoxRegattasSelection(String lastSelectedRegattaName) {
        for (int i = 0; i < listBoxRegattas.getItemCount(); i++) {
            if (listBoxRegattas.getValue(i).equals(lastSelectedRegattaName)) {
                listBoxRegattas.setSelectedIndex(i);
                break;
            }
        }
    }

    private void displayRaceTableUI(Iterable<RegattaDTO> regattas) {
        if (Util.isEmpty(regattas)) {
            hideRaceTable();
        } else {
            showRaceTable();
        }
    }

    private void showRaceTable() {
        raceTable.setVisible(true);
        noTrackedRacesLabel.setVisible(false);
    }

    private void hideRaceTable() {
        raceTable.setVisible(false);
        noTrackedRacesLabel.setVisible(true);
    }

    /**
     * Allows applying some sort of filter to the process of adding races. Defaults to true in standard implementation.
     * Override for custom behavior
     */
    protected boolean raceIsToBeAddedToList(RaceDTO race) {
        return true;
    }
    
    public RefreshableSelectionModel<RaceDTO> getSelectionModel() {
        return refreshableSelectionModel;
    }

}
