package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
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
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.NamedDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.gwt.ui.adminconsole.RaceColumnInLeaderboardDialog.RaceColumnDescriptor;
import com.sap.sailing.gwt.ui.client.LeaderboardsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardsRefresher;
import com.sap.sailing.gwt.ui.client.ParallelExecutionCallback;
import com.sap.sailing.gwt.ui.client.ParallelExecutionHolder;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.FlushableCellTable;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sailing.gwt.ui.shared.RaceLogDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.RegattaLogDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.EntityIdentityComparator;
import com.sap.sse.gwt.client.celltable.RefreshableMultiSelectionModel;
import com.sap.sse.gwt.client.celltable.RefreshableSelectionModel;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.gwt.client.panels.LabeledAbstractFilterablePanel;

public abstract class AbstractLeaderboardConfigPanel extends FormPanel implements SelectedLeaderboardProvider,
        RegattasDisplayer, TrackedRaceChangedListener, LeaderboardsDisplayer {
    protected final VerticalPanel mainPanel;

    protected final TrackedRacesListComposite trackedRacesListComposite;

    protected final StringMessages stringMessages;

    protected final SailingServiceAsync sailingService;

    protected final ListDataProvider<StrippedLeaderboardDTO> filteredLeaderboardList;

    protected final ErrorReporter errorReporter;

    protected final FlushableCellTable<StrippedLeaderboardDTO> leaderboardTable;

    protected final RaceTableWrapper<RefreshableSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>> raceColumnTable;
    protected final RefreshableSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceColumnTableSelectionModel;

    protected RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceInLeaderboard;

    protected final CaptionPanel selectedLeaderBoardPanel;
    protected final CaptionPanel trackedRacesCaptionPanel;
    protected final List<RegattaDTO> allRegattas;

    protected LabeledAbstractFilterablePanel<StrippedLeaderboardDTO> filterLeaderboardPanel;

    protected List<StrippedLeaderboardDTO> availableLeaderboardList;

    protected final RefreshableMultiSelectionModel<StrippedLeaderboardDTO> leaderboardSelectionModel;

    protected final RefreshableSelectionModel<RaceDTO> refreshableTrackedRaceSelectionModel;
    protected final SelectionChangeEvent.Handler trackedRaceListHandler;
    protected HandlerRegistration trackedRaceListHandlerRegistration;
    
    private final LeaderboardsRefresher leaderboardsRefresher;
    
    public static class RaceColumnDTOAndFleetDTOWithNameBasedEquality extends Triple<RaceColumnDTO, FleetDTO, StrippedLeaderboardDTO> {
        private static final long serialVersionUID = -8742476113296862662L;

        public RaceColumnDTOAndFleetDTOWithNameBasedEquality(RaceColumnDTO a, FleetDTO b, StrippedLeaderboardDTO c) {
            super(a, b, c);
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
            StringMessages theStringConstants, boolean multiSelection) {
        this.stringMessages = theStringConstants;
        this.sailingService = sailingService;
        filteredLeaderboardList = new ListDataProvider<StrippedLeaderboardDTO>();
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

        HorizontalPanel leaderboardControlsPanel = new HorizontalPanel();
        Label lblFilterEvents = new Label(stringMessages.filterLeaderboardsByName() + ": ");
        leaderboardControlsPanel.setSpacing(5);
        addLeaderboardControls(leaderboardControlsPanel);
        leaderboardsPanel.add(leaderboardControlsPanel);

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        leaderboardTable = new FlushableCellTable<StrippedLeaderboardDTO>(/* pageSize */10000, tableRes);
        filterLeaderboardPanel = new LabeledAbstractFilterablePanel<StrippedLeaderboardDTO>(lblFilterEvents,
                availableLeaderboardList, leaderboardTable, filteredLeaderboardList) {
            @Override
            public List<String> getSearchableStrings(StrippedLeaderboardDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.name);
                strings.add(t.displayName);
                return strings;
            }
        };
        filterLeaderboardPanel.getTextBox().ensureDebugId("LeaderboardsFilterTextBox");

        leaderboardsPanel.add(filterLeaderboardPanel);
        leaderboardTable.ensureDebugId("AvailableLeaderboardsTable");
        addColumnsToLeaderboardTableAndSetSelectionModel(leaderboardTable, tableRes,
                filterLeaderboardPanel.getAllListDataProvider());
        @SuppressWarnings("unchecked")
        RefreshableMultiSelectionModel<StrippedLeaderboardDTO> multiSelectionModel = (RefreshableMultiSelectionModel<StrippedLeaderboardDTO>) leaderboardTable
                .getSelectionModel();
        leaderboardSelectionModel = multiSelectionModel;
        leaderboardTable.setWidth("100%");
        leaderboardSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                if (trackedRacesListComposite != null) {
                    trackedRacesListComposite.setRegattaFilterValue(getSelectedLeaderboardName());
                }
                leaderboardSelectionChanged();
                raceColumnTable.setSelectedLeaderboardName(getSelectedLeaderboardName());
            }
        });
        filteredLeaderboardList.addDataDisplay(leaderboardTable);
        leaderboardsPanel.add(leaderboardTable);
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

        trackedRacesListComposite = new TrackedRacesListComposite(null, null, sailingService, errorReporter,
                regattaRefresher,
                stringMessages, /* multiselection */false, isActionButtonsEnabled());
        refreshableTrackedRaceSelectionModel = trackedRacesListComposite.getSelectionModel();
        trackedRacesListComposite.ensureDebugId("TrackedRacesListComposite");
        trackedRacesPanel.add(trackedRacesListComposite);
        trackedRacesListComposite.addTrackedRaceChangeListener(this);
        trackedRaceListHandler = new SelectionChangeEvent.Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Set<RaceDTO> selectedRaces = refreshableTrackedRaceSelectionModel.getSelectedSet();
                RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceColumnAndFleetName = getSelectedRaceColumnWithFleet();
                // if no leaderboard column is selected, ignore the race selection change
                if (selectedRaceColumnAndFleetName != null) {
                    RaceColumnDTO selectedRaceColumn = selectedRaceColumnAndFleetName.getA();
                    FleetDTO selectedRaceColumnFleet = selectedRaceColumnAndFleetName.getB();
                    if (selectedRaces.isEmpty()) {
                        if (hasLink(selectedRaceColumnAndFleetName)) {
                            unlinkRaceColumnFromTrackedRace(selectedRaceColumn.getRaceColumnName(),
                                    selectedRaceColumnFleet);
                        }
                    } else {
                        RaceDTO selectedRace = selectedRaces.iterator().next();
                        if (hasLink(selectedRaceColumnAndFleetName)
                                && !isLinkedToRace(selectedRaceColumnAndFleetName, selectedRace)) {
                            if (Window.confirm(stringMessages.trackedRaceAlreadyLinked())) {
                                linkTrackedRaceToSelectedRaceColumn(selectedRaceColumn, selectedRaceColumnFleet,
                                        selectedRace.getRaceIdentifier());
                            } else {
                                selectTrackedRaceInRaceList();
                            }
                        } else {
                            linkTrackedRaceToSelectedRaceColumn(selectedRaceColumn, selectedRaceColumnFleet,
                                    selectedRace.getRaceIdentifier());
                        }
                    }
                }
            }

            private boolean hasLink(RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceColumnAndFleetName) {
                return selectedRaceColumnAndFleetName.getA()
                        .getRaceIdentifier(selectedRaceColumnAndFleetName.getB()) != null;
            }
            
            private boolean isLinkedToRace(RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceColumnAndFleetName, RaceDTO selectedRace){
                return selectedRaceColumnAndFleetName.getA().getRaceIdentifier(selectedRaceColumnAndFleetName.getB()).equals(selectedRace.getRaceIdentifier());
            }
        };
        trackedRaceListHandlerRegistration = refreshableTrackedRaceSelectionModel.addSelectionChangeHandler(trackedRaceListHandler);
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
                Notification.notify(stringMessages.raceLogReloaded(), NotificationType.ERROR);
            }
        });
        vPanel.add(reloadAllRaceLogs);
        Label lblRaceNamesIn = new Label(stringMessages.races());
        vPanel.add(lblRaceNamesIn);
        raceColumnTable = new RaceTableWrapper<RefreshableSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>>(
                sailingService, stringMessages, errorReporter, multiSelection);
        raceColumnTable.asWidget().ensureDebugId("RaceColumnTable");
        raceColumnTable.getTable().setWidth("100%");
        addColumnsToRacesTable(raceColumnTable.getTable());
        
        this.raceColumnTableSelectionModel = raceColumnTable.getSelectionModel();
        raceColumnTableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                // If the selection on the raceColumnTable changes,
                // you don't want to link or unlink raceColumns with the
                // trackedRaceListHandler.
                removeTrackedRaceListHandlerTemporarily();
                leaderboardRaceColumnSelectionChanged();
            }
        });
        vPanel.add(raceColumnTable);

        HorizontalPanel selectedLeaderboardRaceButtonPanel = new HorizontalPanel();
        selectedLeaderboardRaceButtonPanel.setSpacing(5);
        vPanel.add(selectedLeaderboardRaceButtonPanel);
        
        addSelectedLeaderboardRacesControls(selectedLeaderboardRaceButtonPanel);
    }

    protected boolean isActionButtonsEnabled() {
        return /* actionButtonsEnabled */ false;
    }
    
    protected abstract void addLeaderboardControls(Panel controlsPanel);
    protected abstract void addSelectedLeaderboardRacesControls(Panel racesPanel);
    protected abstract void addColumnsToLeaderboardTableAndSetSelectionModel(FlushableCellTable<StrippedLeaderboardDTO> leaderboardTable, 
            AdminConsoleTableResources tableRes, ListDataProvider<StrippedLeaderboardDTO> listDataProvider);
    protected abstract void addColumnsToRacesTable(CellTable<RaceColumnDTOAndFleetDTOWithNameBasedEquality> racesTable);

    protected SelectionCheckboxColumn<StrippedLeaderboardDTO> createSortableSelectionCheckboxColumn(
            final FlushableCellTable<StrippedLeaderboardDTO> leaderboardTable, AdminConsoleTableResources tableResources,
            ListHandler<StrippedLeaderboardDTO> leaderboardColumnListHandler, ListDataProvider<StrippedLeaderboardDTO> listDataProvider) {
        SelectionCheckboxColumn<StrippedLeaderboardDTO> selectionCheckboxColumn = new SelectionCheckboxColumn<StrippedLeaderboardDTO>(
                tableResources.cellTableStyle().cellTableCheckboxSelected(),
                tableResources.cellTableStyle().cellTableCheckboxDeselected(),
                tableResources.cellTableStyle().cellTableCheckboxColumnCell(), new EntityIdentityComparator<StrippedLeaderboardDTO>() {
                    @Override
                    public boolean representSameEntity(StrippedLeaderboardDTO dto1, StrippedLeaderboardDTO dto2) {
                        return dto1.name.equals(dto2.name);
                    }
                    @Override
                    public int hashCode(StrippedLeaderboardDTO t) {
                        return t.name.hashCode();
                    }
                }, listDataProvider, leaderboardTable);
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
     */
    public void loadAndRefreshLeaderboard(final String leaderboardName) {
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
                            leaderboardSelectionChanged();
                            getLeaderboardsRefresher().updateLeaderboards(filteredLeaderboardList.getList(), AbstractLeaderboardConfigPanel.this);
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
                            Notification.notify(stringMessages.raceLogReloaded(), NotificationType.SUCCESS);
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
                                    // This method should select the linked trackedRace.
                                    // So you don't want to link or unlink it again throw the trackedRaceListHandler.
                                    removeTrackedRaceListHandlerTemporarily();
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

    protected boolean canBoatsOfCompetitorsChangePerRace() {
        return getSelectedLeaderboard() != null ? getSelectedLeaderboard().canBoatsOfCompetitorsChangePerRace: false;
    }

    protected abstract void leaderboardSelectionChanged();

    @Override
    public void fillRegattas(Iterable<RegattaDTO> regattas) {
        removeTrackedRaceListHandlerTemporarily();
        trackedRacesListComposite.fillRegattas(regattas);
        allRegattas.clear();
        Util.addAll(regattas, allRegattas);
    }

    @Override
    public void racesStoppedTracking(Iterable<? extends RegattaAndRaceIdentifier> regattaAndRaceIdentifiers) {
        // nothing needs to be done here; the race doesn't change its linkedness status only because it stopped tracking
    }

    /**
     * When a race is removed from the server, it will also have been unlinked. Represent the unlinking by clearing the
     * tracked race link for any race column / fleet that points to it:
     */
    @Override
    public void racesRemoved(Iterable<? extends RegattaAndRaceIdentifier> regattaAndRaceIdentifiers) {
        for (RegattaAndRaceIdentifier regattaAndRaceIdentifier : regattaAndRaceIdentifiers) {
            for (StrippedLeaderboardDTO leaderboard : filteredLeaderboardList.getList()) {
                for (RaceColumnDTO raceColumn : leaderboard.getRaceList()) {
                    for (FleetDTO fleet : raceColumn.getFleets()) {
                        if (Util.equalsWithNull(raceColumn.getRaceIdentifier(fleet), regattaAndRaceIdentifier)) {
                            raceColumn.setRaceIdentifier(fleet, null); // remove link from leaderboard to tracked race
                            raceColumn.getRaceLogTrackingInfo(fleet).raceLogTrackerExists = false;
                        }
                    }
                }
            }
        }
        raceColumnTable.getDataProvider().refresh();
    }

    protected void onTrackedRaceForRaceInRaceColumnTableRemoved(
            RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnAndFleetName) {
        raceColumnAndFleetName.getA().setRaceIdentifier(raceColumnAndFleetName.getB(), null);
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

    protected void editRaceColumnOfLeaderboard(final RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnWithFleet) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final boolean oldIsMedalRace = raceColumnWithFleet.getA().isMedalRace();
        final String oldRaceColumnName = raceColumnWithFleet.getA().getRaceColumnName();
        final Double oldExplicitFactor = raceColumnWithFleet.getA().getExplicitFactor();
        // use a set to avoid duplicates in the case of regatta leaderboards with multiple fleets per column
        Set<RaceColumnDTO> existingRacesWithoutThisRace = new HashSet<RaceColumnDTO>();
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality pair : raceColumnTable.getDataProvider().getList()) {
            existingRacesWithoutThisRace.add(pair.getA());
        }
        existingRacesWithoutThisRace.remove(raceColumnWithFleet.getA());
        final RaceColumnInLeaderboardDialog raceDialog = new RaceColumnInLeaderboardDialog(existingRacesWithoutThisRace,
                raceColumnWithFleet.getA(), getSelectedLeaderboard().type.isRegattaLeaderboard(), stringMessages, new DialogCallback<RaceColumnDescriptor>() {
            @Override
            public void cancel() {
            }
    
            @Override
            public void ok(final RaceColumnDescriptor result) {
                boolean rename = !oldRaceColumnName.equals(result.getName());
                boolean updateIsMedalRace = oldIsMedalRace != result.isMedalRace();
                boolean updateFactor = oldExplicitFactor != result.getExplicitFactor();
                List<ParallelExecutionCallback<Void>> callbacks = new ArrayList<ParallelExecutionCallback<Void>>();
                final ParallelExecutionCallback<Void> renameLeaderboardColumnCallback = new ParallelExecutionCallback<Void>();
                if (rename) {
                    callbacks.add(renameLeaderboardColumnCallback);
                }
                final ParallelExecutionCallback<Void> updateIsMedalRaceCallback = new ParallelExecutionCallback<Void>();
                if (updateIsMedalRace) {
                    callbacks.add(updateIsMedalRaceCallback);
                }
                final ParallelExecutionCallback<Void> updateLeaderboardColumnFactorCallback = new ParallelExecutionCallback<Void>();
                if (updateFactor) {
                    callbacks.add(updateLeaderboardColumnFactorCallback);
                }
                new ParallelExecutionHolder(callbacks.toArray(new ParallelExecutionCallback<?>[0])) {
                    @Override
                    public void handleSuccess() {
                        loadAndRefreshLeaderboard(selectedLeaderboardName);
                    }
                    @Override
                    public void handleFailure(Throwable t) {
                        errorReporter.reportError("Error trying to update data of race column "
                                + oldRaceColumnName + " in leaderboard " + selectedLeaderboardName + ": "
                                + t.getMessage());
                    }
                };
                if (rename) {
                    sailingService.renameLeaderboardColumn(selectedLeaderboardName, oldRaceColumnName,
                            result.getName(), renameLeaderboardColumnCallback);
                }
                if (updateIsMedalRace) {
                    sailingService.updateIsMedalRace(selectedLeaderboardName, result.getName(),
                            result.isMedalRace(), updateIsMedalRaceCallback);
                }
                if (updateFactor) {
                    sailingService.updateLeaderboardColumnFactor(selectedLeaderboardName, result.getName(),
                            result.getExplicitFactor(), updateLeaderboardColumnFactorCallback);
                }
            }
        });
        raceDialog.show();
    }

    protected void showRaceLog(final RaceColumnDTO raceColumnDTO, final FleetDTO fleetDTO) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        sailingService.getRaceLog(selectedLeaderboardName, raceColumnDTO, fleetDTO,
                new MarkedAsyncCallback<RaceLogDTO>(
                        new AsyncCallback<RaceLogDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(caught.getMessage(), true);
                            }
                            @Override
                            public void onSuccess(RaceLogDTO result) {
                                openRaceLogDialog(result);
                            }
                        }));
    }

    private void openRaceLogDialog(RaceLogDTO raceLogDTO) {
        RaceLogDialog dialog = new RaceLogDialog(raceLogDTO, stringMessages, new DialogCallback<RaceLogDTO>() { 
            @Override
            public void cancel() {
            }
    
            @Override
            public void ok(RaceLogDTO result) {
            }
        });
        dialog.show();
    }

    protected void showRegattaLog() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        sailingService.getRegattaLog(selectedLeaderboardName,
                new MarkedAsyncCallback<RegattaLogDTO>(
                        new AsyncCallback<RegattaLogDTO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(caught.getMessage(), true);
                            }
                            @Override
                            public void onSuccess(RegattaLogDTO result) {
                                openRegattaLogDialog(result);
                            }
                        }));
    }

    private void openRegattaLogDialog(RegattaLogDTO regattaLogDTO) {
        RegattaLogDialog dialog = new RegattaLogDialog(regattaLogDTO, stringMessages, new DialogCallback<RegattaLogDTO>() { 
            @Override
            public void cancel() {
            }
    
            @Override
            public void ok(RegattaLogDTO result) {
            }
        });
        dialog.show();
    }
    
    /**
     * Removes the {@link SelectionChangeEvent.Handler} until the browser regains control. The handler will be added
     * again using {@link Scheduler#scheduleDeferred(ScheduledCommand)} method.
     * <p>
     * Use this method if you change the {@link ListDataProvider} or {@link RefreshableSelectionModel} of
     * {@link TrackedRacesListComposite} and you don't want to trigger the
     * {@link SelectionChangeEvent.Handler#onSelectionChange(SelectionChangeEvent)}.
     */
    private void removeTrackedRaceListHandlerTemporarily() {
        if (trackedRaceListHandlerRegistration == null) {
            return;
        }
        trackedRaceListHandlerRegistration.removeHandler();
        trackedRaceListHandlerRegistration = null;
        // It is necessary to do this with the ScheduleDeferred() method,
        // because the SelectionChangeEvent isn't fired directly after
        // selection changes. So an remove of SelectionChangeHandler before 
        // the selection change and and new registration directly after it
        // isn't possible.
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                trackedRaceListHandlerRegistration = refreshableTrackedRaceSelectionModel
                        .addSelectionChangeHandler(trackedRaceListHandler);
            }
        });
    }

    /**
     * Looks up the regatta for the selected leaderboard by name in {@link #allRegattas}
     */
    protected RegattaDTO getSelectedRegatta() {
        final String regattaName = getSelectedLeaderboard() == null ? "" : getSelectedLeaderboard().regattaName;
        return getRegattaByName(regattaName);
    }

    /**
     * Looks up a regatta with name {@code regattaName} in {@link #allRegattas}
     */
    protected RegattaDTO getRegattaByName(final String regattaName) {
        RegattaDTO regatta = null;
        if (regattaName != null) {
            if (allRegattas != null) {
                for (RegattaDTO i : allRegattas) {
                    if (regattaName.equals(i.getName())) {
                        regatta = i;
                        break;
                    }
                }
            }
        }
        return regatta;
    }
}