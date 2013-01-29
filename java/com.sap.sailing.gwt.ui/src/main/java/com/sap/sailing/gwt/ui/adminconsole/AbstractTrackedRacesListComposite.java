package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLFactory;
import com.sap.sailing.gwt.ui.shared.RaceDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public abstract class AbstractTrackedRacesListComposite extends SimplePanel implements Component<TrackedRacesSettings>,
        RegattaDisplayer, RaceSelectionChangeListener {

    protected final long DEFAULT_LIVE_DELAY_IN_MILLISECONDS = 5000;

    private final boolean multiSelection;

    private boolean dontFireNextSelectionChangeEvent;

    private final SelectionModel<RaceDTO> selectionModel;

    private final CellTable<RaceDTO> raceTable;

    private ListDataProvider<RaceDTO> raceList;

    private Iterable<RaceDTO> allRaces;

    private final VerticalPanel panel;

    private DateTimeFormatRenderer dateFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.DATE_SHORT));
    private DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(
            DateTimeFormat.getFormat(PredefinedFormat.TIME_LONG));

    private final Label noTrackedRacesLabel;

    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    protected final RegattaRefresher regattaRefresher;
    private final RaceSelectionProvider raceSelectionProvider;
    protected final StringMessages stringMessages;

    private final Button btnRefresh;

    private final TextBox filterRacesTextbox;

    protected final TrackedRacesSettings settings;

    public static class AnchorCell extends AbstractCell<SafeHtml> {

        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    interface AnchorTemplates extends SafeHtmlTemplates {
        @SafeHtmlTemplates.Template("<a href=\"{0}\">{1}</a>")
        SafeHtml cell(String url, String displayName);
    }

    private static AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);

    public AbstractTrackedRacesListComposite(final SailingServiceAsync sailingService,
            final ErrorReporter errorReporter, final RegattaRefresher regattaRefresher,
            RaceSelectionProvider raceSelectionProvider, final StringMessages stringMessages, boolean hasMultiSelection) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.regattaRefresher = regattaRefresher;
        this.multiSelection = hasMultiSelection;
        this.raceSelectionProvider = raceSelectionProvider;
        this.stringMessages = stringMessages;

        raceList = new ListDataProvider<RaceDTO>();
        selectionModel = multiSelection ? new MultiSelectionModel<RaceDTO>() : new SingleSelectionModel<RaceDTO>();
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
        filterRacesTextbox = new TextBox();
        filterRacesTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                fillRaceListFromAvailableRacesApplyingFilter();
            }
        });
        filterPanel.add(filterRacesTextbox);

        noTrackedRacesLabel = new Label(stringMessages.noRacesYet());
        noTrackedRacesLabel.setWordWrap(false);
        panel.add(noTrackedRacesLabel);

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        raceTable = new CellTable<RaceDTO>(/* pageSize */10000, tableRes);
        raceTable.ensureDebugId("TrackedRaces");
        ListHandler<RaceDTO> columnSortHandler = setupTableColumns(stringMessages);
        raceTable.setWidth("300px");
        raceTable.setSelectionModel(selectionModel);
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
        HorizontalPanel trackedRacesButtonPanel = new HorizontalPanel();
        trackedRacesButtonPanel.setSpacing(10);
        panel.add(trackedRacesButtonPanel);

        btnRefresh = new Button(stringMessages.refresh());
        btnRefresh.ensureDebugId("Refresh");
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
            public int compare(RaceDTO t1, RaceDTO t2) {
                String regatta1Name = t1.getRegattaName();
                String regatta2Name = t2.getRegattaName();
                boolean ascending = isSortedAscending();
                if (regatta1Name.equals(regatta2Name)) {
                    return 0;
                }
                int val = ascending ? regatta1Name.compareTo(regatta2Name) : -(regatta2Name.compareTo(regatta1Name));
                return val;
            }

            private boolean isSortedAscending() {
                ColumnSortList sortList = raceTable.getColumnSortList();
                return sortList.size() > 0 & sortList.get(0).isAscending();
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
            public int compare(RaceDTO t1, RaceDTO t2) {
                String boatClass1 = t1.boatClass;
                String boatClass2 = t2.boatClass;
                boolean ascending = isSortedAscending();
                if (boatClass1.equals(boatClass2)) {
                    return 0;
                }
                int val = ascending ? boatClass1.compareTo(boatClass2) : -(boatClass2.compareTo(boatClass1));
                return val;
            }

            private boolean isSortedAscending() {
                ColumnSortList sortList = raceTable.getColumnSortList();
                return sortList.size() > 0 & sortList.get(0).isAscending();
            }
        });

        AnchorCell anchorCell = new AnchorCell();
        Column<RaceDTO, SafeHtml> raceNameColumn = new Column<RaceDTO, SafeHtml>(anchorCell) {
            @Override
            public SafeHtml getValue(RaceDTO raceDTO) {
                if (raceDTO.isTracked == true) {
                    RegattaNameAndRaceName raceIdentifier = (RegattaNameAndRaceName) raceDTO.getRaceIdentifier();
                    String debugParam = Window.Location.getParameter("gwt.codesvr");
                    String link = URLFactory.INSTANCE.encode("/gwt/RaceBoard.html?raceName="
                            + raceIdentifier.getRaceName() + "&regattaName=" + raceIdentifier.getRegattaName()
                            + ((debugParam != null && !debugParam.isEmpty()) ? "&gwt.codesvr=" + debugParam : ""));
                    return ANCHORTEMPLATE.cell(link, raceDTO.name);
                } else {
                    return SafeHtmlUtils.fromString(raceDTO.name);
                }
            }
        };

        raceNameColumn.setSortable(true);

        columnSortHandler.setComparator(raceNameColumn, new Comparator<RaceDTO>() {
            @Override
            public int compare(RaceDTO t1, RaceDTO t2) {
                boolean ascending = isSortedAscending();
                if (t1.name.equals(t2.name)) {
                    return 0;
                }
                int val = -1;
                val = (t1 != null && t2 != null && ascending) ? (t1.name.compareTo(t2.name)) : -(t2.name
                        .compareTo(t1.name));
                return val;
            }

            private boolean isSortedAscending() {
                ColumnSortList sortList = raceTable.getColumnSortList();
                return sortList.size() > 0 & sortList.get(0).isAscending();
            }
        });

        TextColumn<RaceDTO> raceStartColumn = new TextColumn<RaceDTO>() {
            @Override
            public String getValue(RaceDTO raceDTO) {
                if (raceDTO.startOfRace != null) {
                    return dateFormatter.render(raceDTO.startOfRace) + " " + timeFormatter.render(raceDTO.startOfRace);
                }

                return "";
            }
        };
        raceStartColumn.setSortable(true);

        columnSortHandler.setComparator(raceStartColumn, new Comparator<RaceDTO>() {
            @Override
            public int compare(RaceDTO r1, RaceDTO r2) {
                if (r1.startOfRace != null && r2.startOfRace != null) {
                    boolean ascending = isSortedAscending();
                    int val = -1;
                    val = (r1.startOfRace.after(r2.startOfRace) && ascending) ? 1 : -1;
                    return val;
                }
                
                return r1.startOfRace == null ? (r2.startOfRace == null ? 0 : -1) : 1;
            }

            private boolean isSortedAscending() {
                ColumnSortList sortList = raceTable.getColumnSortList();
                return sortList.size() > 0 & sortList.get(0).isAscending();
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
                    if (r1.status.status == TrackedRaceStatusEnum.LOADING && r2.status.status == TrackedRaceStatusEnum.LOADING) {
                        return new Double(r1.status.loadingProgress).compareTo(r2.status.loadingProgress);
                    }
                    return new Integer(r1.status.status.ordinal()).compareTo(r2.status.status.ordinal());
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
        
        return columnSortHandler;
    }

    private void fillRaceListFromAvailableRacesApplyingFilter() {
        String text = filterRacesTextbox.getText();
        List<String> wordsToFilter = Arrays.asList(text.split(" "));
        raceList.getList().clear();
        if (text != null && !text.isEmpty()) {
            for (RaceDTO raceDTO : getAllRaces()) {
                boolean failed = false;
                for (String word : wordsToFilter) {
                    String textAsUppercase = word.toUpperCase().trim();
                    if (!raceDTO.getRegattaName().toUpperCase().contains(textAsUppercase)
                            && !raceDTO.boatClass.toUpperCase().contains(textAsUppercase)
                            && !raceDTO.name.toUpperCase().contains(textAsUppercase)) {
                        failed = true;
                        break;
                    }
                }
                if (!failed) {
                    raceList.getList().add(raceDTO);
                }
            }
        } else {
            for (RaceDTO raceFromAllRaces : getAllRaces()) {
                raceList.getList().add(raceFromAllRaces);
            }
        }
        // now sort again according to selected criterion
        ColumnSortEvent.fire(raceTable, raceTable.getColumnSortList());
        onRaceSelectionChange(raceSelectionProvider.getSelectedRaces()); // update selection based on underlying domain
                                                                         // race selection model
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        for (RaceDTO raceFromAllRaces : raceList.getList()) {
            selectionModel.setSelected(raceFromAllRaces, selectedRaces.contains(raceFromAllRaces.getRaceIdentifier()));
        }
    }

    private Iterable<RaceDTO> getAllRaces() {
        return allRaces;
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
                    new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter
                                    .reportError("Exception trying to set the delay to live for the selected tracked races: "
                                            + caught.getMessage());
                        }

                        @Override
                        public void onSuccess(Void result) {
                            regattaRefresher.fillRegattas();
                        }
                    });
        }
    }

    @Override
    public String getLocalizedShortName() {
        return "Tracked races";
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
                        && race.name.equals(raceIdentifier.getRaceName())) {
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
    public void fillRegattas(List<RegattaDTO> regattas) {
        makeControlsReactToFillRegattas(regattas);
        if (regattas.isEmpty()) {
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
        fillRaceListFromAvailableRacesApplyingFilter();
        raceSelectionProvider.setAllRaces(newAllRaceIdentifiers); // have this object be notified; triggers
                                                                  // onRaceSelectionChange
    }

    /**
     * Allows applying some sort of filter to the process of adding races.
     * Defaults to true in standard implementation. Override for custom behavior
     * @param race
     * @return
     */
    protected boolean raceIsToBeAddedToList(RaceDTO race) {
        return true;
    }

    abstract protected void makeControlsReactToFillRegattas(List<RegattaDTO> regattas);

}
