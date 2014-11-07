package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SetSelectionModel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.NamedDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.LeaderboardsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardsRefresher;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public abstract class AbstractLeaderboardConfigPanel extends FormPanel implements SelectedLeaderboardProvider,
        RegattasDisplayer, RaceSelectionChangeListener, TrackedRaceChangedListener, LeaderboardsDisplayer {
    protected final VerticalPanel mainPanel;

    protected final TrackedRacesListComposite trackedRacesListComposite;

    protected final StringMessages stringMessages;

    protected final SailingServiceAsync sailingService;

    protected final ListDataProvider<StrippedLeaderboardDTO> leaderboardList;

    protected final ErrorReporter errorReporter;

    protected final CellTable<StrippedLeaderboardDTO> leaderboardTable;

    protected final RaceTableWrapper<SetSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>> raceColumnTable;
    protected final SetSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceColumnTableSelectionModel;

    protected RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceInLeaderboard;

    protected final CaptionPanel selectedLeaderBoardPanel;
    protected final CaptionPanel trackedRacesCaptionPanel;
    protected final List<RegattaDTO> allRegattas;

    protected LabeledAbstractFilterablePanel<StrippedLeaderboardDTO> filterLeaderboardPanel;

    protected List<StrippedLeaderboardDTO> availableLeaderboardList;

    protected final MultiSelectionModel<StrippedLeaderboardDTO> leaderboardSelectionModel;

    protected final RaceSelectionProvider raceSelectionProvider;

    private final LeaderboardsRefresher leaderboardsRefresher;

    public static class RaceColumnDTOAndFleetDTOWithNameBasedEquality extends Util.Pair<RaceColumnDTO, FleetDTO> {
        private static final long serialVersionUID = -8742476113296862662L;

        public RaceColumnDTOAndFleetDTOWithNameBasedEquality(RaceColumnDTO a, FleetDTO b) {
            super(a, b);
        }

        @Override
        public int hashCode() {
            return getA().getName().hashCode() ^ getB().getName().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            } else {
                if (obj == null) {
                    return false;
                } else {
                    return equalNamesOrBothNull(getA(), ((RaceColumnDTOAndFleetDTOWithNameBasedEquality) obj).getA())
                            && equalNamesOrBothNull(getB(), ((RaceColumnDTOAndFleetDTOWithNameBasedEquality) obj).getB());
                }
            }
        }

        private boolean equalNamesOrBothNull(NamedDTO a, NamedDTO b) {
            if (a == null) {
                return b == null;
            } else {
                if (b == null) {
                    return false;
                } else {
                    return Util.equalsWithNull(a.getName(), b.getName());
                }
            }
        }
    }

    public AbstractLeaderboardConfigPanel(final SailingServiceAsync sailingService, RegattaRefresher regattaRefresher,
            LeaderboardsRefresher leaderboardsRefresher, final ErrorReporter errorReporter,
            StringMessages theStringConstants, SetSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceColumnTableSelectionModel) {
        this.stringMessages = theStringConstants;
        this.sailingService = sailingService;
        leaderboardList = new ListDataProvider<StrippedLeaderboardDTO>();
        allRegattas = new ArrayList<RegattaDTO>();
        this.errorReporter = errorReporter;
        this.leaderboardsRefresher = leaderboardsRefresher;
        this.availableLeaderboardList = new ArrayList<StrippedLeaderboardDTO>();
        mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        this.setWidget(mainPanel);

        //Create leaderboards list and functionality
        CaptionPanel leaderboardsCaptionPanel = new CaptionPanel(stringMessages.leaderboards());
        leaderboardsCaptionPanel.setStyleName("bold");
        mainPanel.add(leaderboardsCaptionPanel);

        VerticalPanel leaderboardsPanel = new VerticalPanel();
        leaderboardsCaptionPanel.add(leaderboardsPanel);

        HorizontalPanel leaderboardConfigControlsPanel = new HorizontalPanel();
        Label lblFilterEvents = new Label(stringMessages.filterLeaderboardsByName() + ": ");
        leaderboardConfigControlsPanel.setSpacing(5);

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        leaderboardTable = new CellTable<StrippedLeaderboardDTO>(/* pageSize */10000, tableRes);
        filterLeaderboardPanel = new LabeledAbstractFilterablePanel<StrippedLeaderboardDTO>(lblFilterEvents,
                availableLeaderboardList, leaderboardTable, leaderboardList) {
            @Override
            public List<String> getSearchableStrings(StrippedLeaderboardDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.name);
                strings.add(t.displayName);
                return strings;
            }
        };
        leaderboardConfigControlsPanel.add(filterLeaderboardPanel);
        addLeaderboardConfigControls(leaderboardConfigControlsPanel);
        leaderboardsPanel.add(leaderboardConfigControlsPanel);
        leaderboardTable.ensureDebugId("AvailableLeaderboardsTable");
        addColumnsToLeaderboardTableAndSetSelectionModel(leaderboardTable, tableRes);
        @SuppressWarnings("unchecked")
        MultiSelectionModel<StrippedLeaderboardDTO> multiSelectionModel = (MultiSelectionModel<StrippedLeaderboardDTO>) leaderboardTable.getSelectionModel();
        leaderboardSelectionModel = multiSelectionModel;
        leaderboardTable.setWidth("100%");
        leaderboardSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                leaderboardSelectionChanged();
                raceColumnTable.setSelectedLeaderboardName(getSelectedLeaderboardName());
            }
        });
        leaderboardList.addDataDisplay(leaderboardTable);
        leaderboardsPanel.add(leaderboardTable);
        HorizontalPanel leaderboardButtonPanel = new HorizontalPanel();
        leaderboardButtonPanel.setSpacing(5);
        leaderboardsPanel.add(leaderboardButtonPanel);
        addLeaderboardCreateControls(leaderboardButtonPanel);
        mainPanel.add(new Grid(1, 1));

        // caption panels for the selected leaderboard and tracked races
        HorizontalPanel splitPanel = new HorizontalPanel();
        splitPanel.setWidth("100%");
        splitPanel.ensureDebugId("LeaderboardDetailsPanel");
        mainPanel.add(splitPanel);

        selectedLeaderBoardPanel = new CaptionPanel(stringMessages.leaderboard());
        splitPanel.add(selectedLeaderBoardPanel);
        splitPanel.setCellWidth(selectedLeaderBoardPanel, "50%");
        
        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        selectedLeaderBoardPanel.setContentWidget(vPanel);

        trackedRacesCaptionPanel = new CaptionPanel(stringMessages.trackedRaces());
        splitPanel.add(trackedRacesCaptionPanel);
        splitPanel.setCellWidth(trackedRacesCaptionPanel, "50%");

        VerticalPanel trackedRacesPanel = new VerticalPanel();
        trackedRacesPanel.setWidth("100%");
        trackedRacesCaptionPanel.setContentWidget(trackedRacesPanel);
        trackedRacesCaptionPanel.setStyleName("bold");

        raceSelectionProvider = new RaceSelectionModel();
        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, regattaRefresher,
                raceSelectionProvider, stringMessages, /* multiselection */false);
        trackedRacesListComposite.ensureDebugId("TrackedRacesListComposite");
        trackedRacesPanel.add(trackedRacesListComposite);
        trackedRacesListComposite.addTrackedRaceChangeListener(this);
        raceSelectionProvider.addRaceSelectionChangeListener(this);

        Button reloadAllRaceLogs = new Button(stringMessages.reloadAllRaceLogs());
        reloadAllRaceLogs.ensureDebugId("ReloadAllRaceLogsButton");
        reloadAllRaceLogs.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                StrippedLeaderboardDTO leaderboard = getSelectedLeaderboard();
                for (RaceColumnDTO column : leaderboard.getRaceList()) {
                    for (FleetDTO fleet : column.getFleets()) {
                        refreshRaceLog(column, fleet, false);
                    }
                }
                Window.alert(stringMessages.raceLogReloaded());
            }
        });
        vPanel.add(reloadAllRaceLogs);

        
        Label lblRaceNamesIn = new Label(stringMessages.races());
        vPanel.add(lblRaceNamesIn);
        
        raceColumnTable = new RaceTableWrapper<SetSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>>(
                sailingService, stringMessages, errorReporter, raceColumnTableSelectionModel);
        raceColumnTable.getTable().setWidth("100%");
        addColumnsToRacesTable(raceColumnTable.getTable());
        this.raceColumnTableSelectionModel = raceColumnTableSelectionModel;
        raceColumnTableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                leaderboardRaceColumnSelectionChanged();
            }
        });
        vPanel.add(raceColumnTable);

        HorizontalPanel selectedLeaderboardRaceButtonPanel = new HorizontalPanel();
        selectedLeaderboardRaceButtonPanel.setSpacing(5);
        vPanel.add(selectedLeaderboardRaceButtonPanel);
        
        addSelectedLeaderboardRacesControls(selectedLeaderboardRaceButtonPanel);
    }
    
    protected abstract void addLeaderboardConfigControls(Panel configPanel);
    protected abstract void addLeaderboardCreateControls(Panel createPanel);
    protected abstract void addSelectedLeaderboardRacesControls(Panel racesPanel);
    protected abstract void addColumnsToLeaderboardTableAndSetSelectionModel(CellTable<StrippedLeaderboardDTO> leaderboardTable, AdminConsoleTableResources tableRes);
    protected abstract void addColumnsToRacesTable(CellTable<RaceColumnDTOAndFleetDTOWithNameBasedEquality> racesTable);

    protected SelectionCheckboxColumn<StrippedLeaderboardDTO> createSortableSelectionCheckboxColumn(
            final CellTable<StrippedLeaderboardDTO> leaderboardTable, AdminConsoleTableResources tableResources,
            ListHandler<StrippedLeaderboardDTO> leaderboardColumnListHandler) {
        SelectionCheckboxColumn<StrippedLeaderboardDTO> selectionCheckboxColumn = new SelectionCheckboxColumn<StrippedLeaderboardDTO>(tableResources.cellTableStyle().cellTableCheckboxSelected(),
                tableResources.cellTableStyle().cellTableCheckboxDeselected(), tableResources.cellTableStyle().cellTableCheckboxColumnCell()) {
            @Override
            protected ListDataProvider<StrippedLeaderboardDTO> getListDataProvider() {
                return leaderboardList;
            }

            @Override
            public Boolean getValue(StrippedLeaderboardDTO row) {
                return leaderboardTable.getSelectionModel().isSelected(row);
            }
        };
        selectionCheckboxColumn.setSortable(true);
        leaderboardColumnListHandler.setComparator(selectionCheckboxColumn, new Comparator<StrippedLeaderboardDTO>() {
            @Override
            public int compare(StrippedLeaderboardDTO o1, StrippedLeaderboardDTO o2) {
                return (leaderboardTable.getSelectionModel().isSelected(o1) ? 1 : 0) - (leaderboardTable.getSelectionModel().isSelected(o2) ? 1 : 0);
            }
        });
        return selectionCheckboxColumn;
    }
    
    @Override
    public void fillLeaderboards(Iterable<StrippedLeaderboardDTO> leaderboards) {
        availableLeaderboardList.clear();
        Util.addAll(leaderboards, availableLeaderboardList);
        filterLeaderboardPanel.updateAll(availableLeaderboardList); // also maintains the filtered leaderboardList
        leaderboardSelectionChanged();
        leaderboardRaceColumnSelectionChanged();
    }

    /**
     * @param nameOfRaceColumnToSelect
     *            if not <code>null</code>, selects the first race column name with this name found in the leaderboard
     *            after the refresh has successfully completed. See {@link #selectRaceColumn(String)}.
     */
    public void loadAndRefreshLeaderboard(final String leaderboardName, final String nameOfRaceColumnToSelect) {
        sailingService.getLeaderboard(leaderboardName, new MarkedAsyncCallback<StrippedLeaderboardDTO>(
                new AsyncCallback<StrippedLeaderboardDTO>() {
                        @Override
                        public void onSuccess(StrippedLeaderboardDTO leaderboard) {
                            for (StrippedLeaderboardDTO leaderboardDTO : leaderboardSelectionModel.getSelectedSet()) {
                                if (leaderboardDTO.name.equals(leaderboardName)) {
                                    leaderboardSelectionModel.setSelected(leaderboardDTO, false);
                                    break;
                                }
                            }
                            replaceLeaderboardInList(availableLeaderboardList, leaderboardName, leaderboard);
                            filterLeaderboardPanel.updateAll(availableLeaderboardList); // also updates leaderboardList provider
                            leaderboardSelectionModel.setSelected(leaderboard, true);
                            if (nameOfRaceColumnToSelect != null) {
                                selectRaceColumn(nameOfRaceColumnToSelect);
                            }
                            leaderboardSelectionChanged();
                            getLeaderboardsRefresher().updateLeaderboards(leaderboardList.getList(), AbstractLeaderboardConfigPanel.this);
                        }
            
                        @Override
                        public void onFailure(Throwable t) {
                            AbstractLeaderboardConfigPanel.this.errorReporter.reportError("Error trying to update leaderboard with name " + leaderboardName + " : "
                                    + t.getMessage());
                        }
                }));
    }

    private void replaceLeaderboardInList(List<StrippedLeaderboardDTO> leaderboardList, String leaderboardToReplace, StrippedLeaderboardDTO newLeaderboard) {
        int index = -1;
        for (StrippedLeaderboardDTO existingLeaderboard : leaderboardList) {
            index++;
            if (existingLeaderboard.name.equals(leaderboardToReplace)) {
                break;
            }
        }
        if (index >= 0) {
            leaderboardList.set(index, newLeaderboard);
        }
    }

    protected void unlinkRaceColumnFromTrackedRace(final String raceColumnName, final FleetDTO fleet) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        sailingService.disconnectLeaderboardColumnFromTrackedRace(selectedLeaderboardName, raceColumnName, fleet.getName(),
                new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to unlink tracked race from column " + raceColumnName
                        + " from leaderboard " + selectedLeaderboardName + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(Void arg0) {
                trackedRacesListComposite.clearSelection();
                getSelectedRaceColumnWithFleet().getA().setRaceIdentifier(fleet, null);
                raceColumnTable.getDataProvider().refresh();
            }
        });
    }

    protected void refreshRaceLog(final RaceColumnDTO raceColumnDTO, final FleetDTO fleet, final boolean showAlerts) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        sailingService.reloadRaceLog(selectedLeaderboardName, raceColumnDTO, fleet, new MarkedAsyncCallback<Void>(
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        if (showAlerts) {
                            errorReporter.reportError(caught.getMessage());
                        }
                    }
                    @Override
                    public void onSuccess(Void result) {
                        if (showAlerts) {
                            Window.alert(stringMessages.raceLogReloaded());
                        }
                    }
                }));
    }

    protected abstract void leaderboardRaceColumnSelectionChanged();
    
    protected void selectRaceColumn(String raceColumnName) {
        List<RaceColumnDTOAndFleetDTOWithNameBasedEquality> list = raceColumnTable.getDataProvider().getList();
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality pair : list) {
            if (pair.getA().getName().equals(raceColumnName)) {
                raceColumnTable.getSelectionModel().setSelected(pair, true);
                break;
            }
        }
    }

    protected void selectTrackedRaceInRaceList() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        if (selectedLeaderboardName != null) {
            final RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceColumnAndFleetNameInLeaderboard = getSelectedRaceColumnWithFleet();
            final String selectedRaceColumnName = selectedRaceColumnAndFleetNameInLeaderboard.getA().getRaceColumnName();
            final String selectedFleetName = selectedRaceColumnAndFleetNameInLeaderboard.getB().getName();
            sailingService.getRegattaAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(selectedLeaderboardName,
                    selectedRaceColumnName, new MarkedAsyncCallback<Map<String, RegattaAndRaceIdentifier>>(
                            new AsyncCallback<Map<String, RegattaAndRaceIdentifier>>() {
                                @Override
                                public void onFailure(Throwable t) {
                                    errorReporter.reportError("Error trying to determine tracked race linked to race column "
                                            + selectedRaceColumnName + " in leaderboard " + selectedLeaderboardName + ": "
                                            + t.getMessage());
                                }
                                
                                @Override
                                public void onSuccess(Map<String, RegattaAndRaceIdentifier> regattaAndRaceNamesPerFleet) {
                                    if (regattaAndRaceNamesPerFleet != null && !regattaAndRaceNamesPerFleet.isEmpty()) {
                                        RegattaAndRaceIdentifier raceIdentifier = regattaAndRaceNamesPerFleet.get(selectedFleetName);
                                        
                                        if (raceIdentifier != null) {
                                            selectRaceInList(raceIdentifier.getRegattaName(), raceIdentifier.getRaceName());
                                        } else {
                                            trackedRacesListComposite.clearSelection();
                                        }
                                    } else {
                                        trackedRacesListComposite.clearSelection();
                                    }
                                }
                            }));
        }
    }

    protected void selectRaceInList(String regattaName, String raceName) {
        RegattaNameAndRaceName raceIdentifier = new RegattaNameAndRaceName(regattaName, raceName);
        trackedRacesListComposite.selectRaceByIdentifier(raceIdentifier);
    }

    protected RaceColumnDTOAndFleetDTOWithNameBasedEquality getSelectedRaceColumnWithFleet() {
        if (raceColumnTable.getSelectionModel().getSelectedSet().isEmpty()) {
            return null;
        }
        return raceColumnTable.getSelectionModel().getSelectedSet().iterator().next();
    }

    protected String getSelectedLeaderboardName() {
        return getSelectedLeaderboard() != null ? getSelectedLeaderboard().name : null;
    }

    protected abstract void leaderboardSelectionChanged();

    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        trackedRacesListComposite.fillRegattas(regattas);

        allRegattas.clear();
        allRegattas.addAll(regattas);
    }

    @Override
    public void changeTrackingRace(Iterable<? extends RegattaAndRaceIdentifier> regattaAndRaceIdentifiers, boolean isTracked) {
        for (RegattaAndRaceIdentifier regattaAndRaceIdentifier : regattaAndRaceIdentifiers) {
            for (RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnAndFleetName : raceColumnTable.getDataProvider().getList()) {
                if (raceColumnAndFleetName.getA().getRaceColumnName().equals(regattaAndRaceIdentifier.getRaceName())) {
                    raceColumnAndFleetName.getA().setRaceIdentifier(raceColumnAndFleetName.getB(),
                            regattaAndRaceIdentifier);
                }
            }
            raceColumnTable.getDataProvider().refresh();
        }
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        // if no leaderboard column is selected, ignore the race selection change
        RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceColumnAndFleetName = getSelectedRaceColumnWithFleet();
        if (selectedRaceColumnAndFleetName != null) {
            if (selectedRaces.isEmpty()) {
                if (selectedRaceColumnAndFleetName.getA().getRaceIdentifier(selectedRaceColumnAndFleetName.getB()) != null) {
                    unlinkRaceColumnFromTrackedRace(selectedRaceColumnAndFleetName.getA().getRaceColumnName(), selectedRaceColumnAndFleetName.getB());
                }
            } else {
                linkTrackedRaceToSelectedRaceColumn(selectedRaceColumnAndFleetName.getA(), selectedRaceColumnAndFleetName.getB(),
                        selectedRaces.iterator().next());
            }
        }
    }

    private void linkTrackedRaceToSelectedRaceColumn(final RaceColumnDTO selectedRaceInLeaderboard,
            final FleetDTO fleet, final RegattaAndRaceIdentifier selectedRace) {
        sailingService.connectTrackedRaceToLeaderboardColumn(getSelectedLeaderboardName(), selectedRaceInLeaderboard
                .getRaceColumnName(), fleet.getName(), selectedRace,
                new MarkedAsyncCallback<Boolean>(
                        new AsyncCallback<Boolean>() {
                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError("Error trying to link tracked race " + selectedRace + " to race column named "
                                        + selectedRaceInLeaderboard.getRaceColumnName() + " of leaderboard "
                                        + getSelectedLeaderboardName() + ": " + t.getMessage());
                                trackedRacesListComposite.clearSelection();
                            }
                
                            @Override
                            public void onSuccess(Boolean success) {
                                if (success) {
                                    // TODO consider enabling the Unlink button
                                    selectedRaceInLeaderboard.setRaceIdentifier(fleet, selectedRace);
                                    raceColumnTable.getDataProvider().refresh();
                                }
                            }
                        }));
    }

    @Override
    public StrippedLeaderboardDTO getSelectedLeaderboard() {
        return leaderboardSelectionModel.getSelectedSet().isEmpty() ? null : leaderboardSelectionModel.getSelectedSet().iterator().next();
    }

    protected LeaderboardsRefresher getLeaderboardsRefresher() {
        return leaderboardsRefresher;
    }
}