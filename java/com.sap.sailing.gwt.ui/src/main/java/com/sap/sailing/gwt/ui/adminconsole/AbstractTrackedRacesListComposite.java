package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.impl.NaturalComparator;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sailing.gwt.ui.common.client.DateAndTimeFormatterUtil;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public abstract class AbstractTrackedRacesListComposite extends SimplePanel implements Component<TrackedRacesSettings>,
        RegattasDisplayer, RaceSelectionChangeListener {

    protected final long DEFAULT_LIVE_DELAY_IN_MILLISECONDS = 5000;

    private final boolean multiSelection;

    private boolean dontFireNextSelectionChangeEvent;

    private final SelectionModel<RaceDTO> selectionModel;
    
    private final SelectionCheckboxColumn<RaceDTO> selectionCheckboxColumn;

    private final CellTable<RaceDTO> raceTable;

    private ListDataProvider<RaceDTO> raceList;

    private Iterable<RaceDTO> allRaces;

    private final VerticalPanel panel;

    private final Label noTrackedRacesLabel;

    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    protected final RegattaRefresher regattaRefresher;
    private final RaceSelectionProvider raceSelectionProvider;
    protected final StringMessages stringMessages;

    private final Button btnRefresh;

    private final LabeledAbstractFilterablePanel<RaceDTO> filterablePanelRaces;

    protected final TrackedRacesSettings settings;

    public static class AnchorCell extends AbstractCell<SafeHtml> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    public AbstractTrackedRacesListComposite(final SailingServiceAsync sailingService,
            final ErrorReporter errorReporter, final RegattaRefresher regattaRefresher,
            final RaceSelectionProvider raceSelectionProvider, final StringMessages stringMessages, boolean hasMultiSelection) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;
        this.multiSelection = hasMultiSelection;
        this.raceSelectionProvider = raceSelectionProvider;
        this.stringMessages = stringMessages;
        AdminConsoleTableResources tableResources = GWT.create(AdminConsoleTableResources.class);
        this.selectionCheckboxColumn = new SelectionCheckboxColumn<RaceDTO>(tableResources.cellTableStyle().cellTableCheckboxSelected(),
                tableResources.cellTableStyle().cellTableCheckboxDeselected(), tableResources.cellTableStyle().cellTableCheckboxColumnCell()) {
            @Override
            protected ListDataProvider<RaceDTO> getListDataProvider() {
                return raceList;
            }

            @Override
            public Boolean getValue(RaceDTO row) {
                return raceTable.getSelectionModel().isSelected(row);
            }
        };

        raceList = new ListDataProvider<RaceDTO>();
        settings = new TrackedRacesSettings();
        settings.setDelayToLiveInSeconds(DEFAULT_LIVE_DELAY_IN_MILLISECONDS / 1000l);

        panel = new VerticalPanel();
        setWidget(panel);

        HorizontalPanel filterPanel = new HorizontalPanel();
        panel.add(filterPanel);
        Label lblFilterRaces = new Label(stringMessages.filterRacesByName() + ":");
        lblFilterRaces.setWordWrap(false);
        filterPanel.setSpacing(5);
        filterPanel.add(lblFilterRaces);
        filterPanel.setCellVerticalAlignment(lblFilterRaces, HasVerticalAlignment.ALIGN_MIDDLE);
        noTrackedRacesLabel = new Label(stringMessages.noRacesYet());
        noTrackedRacesLabel.setWordWrap(false);
        panel.add(noTrackedRacesLabel);

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        raceTable = new CellTable<RaceDTO>(/* pageSize */10000, tableRes);
        raceTable.ensureDebugId("TrackedRacesCellTable");
        ListHandler<RaceDTO> columnSortHandler = setupTableColumns(stringMessages);
        raceTable.setWidth("300px");
        if (multiSelection) {
            selectionModel = this.selectionCheckboxColumn.getSelectionModel();
            raceTable.setSelectionModel(selectionModel, this.selectionCheckboxColumn.getSelectionManager());
        } else {
            selectionModel = new SingleSelectionModel<RaceDTO>();
            raceTable.setSelectionModel(selectionModel);
        }
        raceTable.setVisible(false);
        panel.add(raceTable);
        raceList.addDataDisplay(raceTable);
        raceTable.addColumnSortHandler(columnSortHandler);
        raceTable.getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                List<RaceDTO> selectedRaces = getSelectedRaces();
                makeControlsReactToSelectionChange(selectedRaces);
                if (dontFireNextSelectionChangeEvent) {
                    dontFireNextSelectionChangeEvent = false;
                } else {
                    List<RegattaAndRaceIdentifier> selectedRaceIdentifiers = new ArrayList<RegattaAndRaceIdentifier>();
                    for (RaceDTO selectedRace : selectedRaces) {
                        selectedRaceIdentifiers.add(selectedRace.getRaceIdentifier());
                    }
                    AbstractTrackedRacesListComposite.this.raceSelectionProvider.setSelection(selectedRaceIdentifiers,
                            AbstractTrackedRacesListComposite.this);
                }
            }
        });
        filterablePanelRaces = new LabeledAbstractFilterablePanel<RaceDTO>(lblFilterRaces, allRaces, raceTable, raceList) {
            @Override
            public List<String> getSearchableStrings(RaceDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.getName());
                strings.add(t.boatClass);
                strings.add(t.getRegattaName());
                strings.add(t.toString());
                return strings;
            }
        };
        filterablePanelRaces.getTextBox().ensureDebugId("TrackedRacesFilterTextBox");
        filterPanel.add(filterablePanelRaces);
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

    abstract protected void makeControlsReactToSelectionChange(List<RaceDTO> selectedRaces);

    abstract protected void addControlButtons(HorizontalPanel trackedRacesButtonPanel);

    private ListHandler<RaceDTO> setupTableColumns(final StringMessages stringMessages) {
        ListHandler<RaceDTO> columnSortHandler = new ListHandler<RaceDTO>(raceList.getList());
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
        if (multiSelection) {
            columnSortHandler.setComparator(selectionCheckboxColumn, selectionCheckboxColumn.getComparator());
            raceTable.addColumn(selectionCheckboxColumn, selectionCheckboxColumn.getHeader());
        }
        raceTable.addColumn(regattaNameColumn, stringMessages.regatta());
        raceTable.addColumn(boatClassNameColumn, stringMessages.boatClass());
        raceTable.addColumn(raceNameColumn, stringMessages.race());
        raceTable.addColumn(raceStartColumn, stringMessages.startTime());
        raceTable.addColumn(hasWindDataColumn, stringMessages.windData());
        raceTable.addColumn(hasGPSDataColumn, stringMessages.gpsData());
        raceTable.addColumn(raceStatusColumn, stringMessages.status());
        raceTable.addColumn(raceLiveDelayColumn, stringMessages.delayInSeconds());

        return columnSortHandler;
    }

    
    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        for (RaceDTO raceFromAllRaces : raceList.getList()) {
            selectionModel.setSelected(raceFromAllRaces, selectedRaces.contains(raceFromAllRaces.getRaceIdentifier()));
        }
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<TrackedRacesSettings> getSettingsDialogComponent() {
        return new TrackedRacesSettingsDialogComponent<TrackedRacesSettings>(settings, stringMessages);
    }

    @Override
    public void updateSettings(TrackedRacesSettings newSettings) {
        settings.setDelayToLiveInSeconds(newSettings.getDelayToLiveInSeconds());

        // set the new delay to all selected races
        List<RegattaAndRaceIdentifier> raceIdentifiersToUpdate = new ArrayList<RegattaAndRaceIdentifier>();
        for (RaceDTO raceDTO : getSelectedRaces()) {
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

    public void addRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        this.raceSelectionProvider.addRaceSelectionChangeListener(listener);
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

    List<RaceDTO> getSelectedRaces() {
        List<RaceDTO> result = new ArrayList<RaceDTO>();
        if (raceList != null) {
            for (RaceDTO race : raceList.getList()) {
                if (selectionModel.isSelected(race)) {
                    result.add(race);
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
                    dontFireNextSelectionChangeEvent = true;
                    selectionModel.setSelected(race, true);
                    break;
                }
            }
        }
    }

    public void clearSelection() {
        List<RegattaAndRaceIdentifier> emptySelection = Collections.emptyList();
        raceSelectionProvider.setSelection(emptySelection, /* listenersNotToNotify */this);
        if (raceList != null) {
            for (RaceDTO race : raceList.getList()) {
                selectionModel.setSelected(race, false);
            }
        }
    }

    @Override
    public void fillRegattas(Iterable<RegattaDTO> regattas) {
        makeControlsReactToFillRegattas(regattas);
        if (Util.isEmpty(regattas)) {
            raceTable.setVisible(false);
            noTrackedRacesLabel.setVisible(true);
        } else {
            raceTable.setVisible(true);
            noTrackedRacesLabel.setVisible(false);
        }
        List<RaceDTO> newAllRaces = new ArrayList<RaceDTO>();
        List<RegattaDTO> newAllRegattas = new ArrayList<RegattaDTO>();
        List<RegattaAndRaceIdentifier> newAllRaceIdentifiers = new ArrayList<RegattaAndRaceIdentifier>();
        for (RegattaDTO regatta : regattas) {
            newAllRegattas.add(regatta);
            for (RaceDTO race : regatta.races) {
                if (race != null) {
                    if (raceIsToBeAddedToList(race)) {
                        newAllRaces.add(race);
                        newAllRaceIdentifiers.add(race.getRaceIdentifier());
                    }
                }
            }
        }
        allRaces = newAllRaces;
        filterablePanelRaces.updateAll(allRaces);
        raceSelectionProvider.setAllRaces(newAllRaceIdentifiers); // have this object be notified; triggers
                                                                  // onRaceSelectionChange
    }

    /**
     * Allows applying some sort of filter to the process of adding races. Defaults to true in standard implementation.
     * Override for custom behavior
     * 
     * @param race
     * @return
     */
    protected boolean raceIsToBeAddedToList(RaceDTO race) {
        return true;
    }

    abstract protected void makeControlsReactToFillRegattas(Iterable<RegattaDTO> regattas);

}
