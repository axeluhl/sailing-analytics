package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.LeaderboardType;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.gwt.ui.adminconsole.LeaderboardConfigPanel.RaceColumnDTOAndFleetDTOWithNameBasedEquality;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.panels.LabeledAbstractFilterablePanel;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

/**
 * Allows the user to start and stop tracking of races using the RaceLog-tracking connector.
 */
public class RaceLogTrackingEventManagementPanel extends AbstractEventManagementPanel {
    private final ListDataProvider<StrippedLeaderboardDTO> leaderboardList;
    private final ListDataProvider<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceColumnAndFleetList;
    private final CellTable<StrippedLeaderboardDTO> leaderboardTable;
    private final CellTable<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceColumnTable;
    private final CaptionPanel selectedLeaderBoardPanel;
    private final CaptionPanel trackedRacesCaptionPanel;
    private LabeledAbstractFilterablePanel<StrippedLeaderboardDTO> filterLeaderboardPanel;
    final SingleSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceColumnTableSelectionModel;
    private List<StrippedLeaderboardDTO> availableLeaderboardList;
    private final MultiSelectionModel<StrippedLeaderboardDTO> leaderboardSelectionModel;

    public RaceLogTrackingEventManagementPanel(SailingServiceAsync sailingService, RegattaRefresher regattaRefresher,
            ErrorReporter errorReporter, StringMessages stringMessages) {
        super(sailingService, regattaRefresher, errorReporter, new RaceSelectionModel(), stringMessages);
        leaderboardList = new ListDataProvider<StrippedLeaderboardDTO>();
        raceColumnAndFleetList = new ListDataProvider<RaceColumnDTOAndFleetDTOWithNameBasedEquality>();
        this.errorReporter = errorReporter;
        this.availableLeaderboardList = new ArrayList<StrippedLeaderboardDTO>();
        VerticalPanel mainPanel = new VerticalPanel();
        mainPanel.setWidth("100%");
        this.setWidget(mainPanel);

        //Create leaderboards list and functionality
        CaptionPanel leaderboardsCaptionPanel = new CaptionPanel(stringMessages.leaderboards());
        leaderboardsCaptionPanel.setStyleName("bold");
        leaderboardsCaptionPanel.setWidth("75%");
        mainPanel.add(leaderboardsCaptionPanel);

        VerticalPanel leaderboardsPanel = new VerticalPanel();
        leaderboardsCaptionPanel.add(leaderboardsPanel);

        HorizontalPanel leaderboardConfigControlsPanel = new HorizontalPanel();
        Label lblFilterEvents = new Label(stringMessages.filterLeaderboardsByName() + ": ");
        leaderboardConfigControlsPanel.setSpacing(5);

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        leaderboardTable = new CellTable<StrippedLeaderboardDTO>(/* pageSize */10000, tableRes);
        filterLeaderboardPanel = new LabeledAbstractFilterablePanel<StrippedLeaderboardDTO>(lblFilterEvents, availableLeaderboardList, leaderboardTable, leaderboardList) {
            @Override
            public List<String> getSearchableStrings(StrippedLeaderboardDTO t) {
                List<String> strings = new ArrayList<String>();
                strings.add(t.name);
                strings.add(t.displayName);
                return strings;
            }
        };
        leaderboardConfigControlsPanel.add(filterLeaderboardPanel);
        leaderboardsPanel.add(leaderboardConfigControlsPanel);
        ListHandler<StrippedLeaderboardDTO> leaderboardColumnListHandler = new ListHandler<StrippedLeaderboardDTO>(
                leaderboardList.getList());

        TextColumn<StrippedLeaderboardDTO> leaderboardNameColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                return leaderboard.name;
            }
        };

        TextColumn<StrippedLeaderboardDTO> leaderboardDisplayNameColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                return leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : "";
            }
        };

        ImagesBarColumn<StrippedLeaderboardDTO, RaceLogTrackingEventManagementImagesBarCell> leaderboardActionColumn =
                new ImagesBarColumn<StrippedLeaderboardDTO, RaceLogTrackingEventManagementImagesBarCell>(
                new RaceLogTrackingEventManagementImagesBarCell(stringMessages));
        leaderboardActionColumn.setFieldUpdater(new FieldUpdater<StrippedLeaderboardDTO, String>() {
            @Override
            public void update(int index, StrippedLeaderboardDTO leaderboardDTO, String value) {
                if (RaceLogTrackingEventManagementImagesBarCell.ACTION_ADD_RACELOG_TRACKERS.equals(value)) {
                    addRaceLogTrackers(leaderboardDTO);
                } else if (RaceLogTrackingEventManagementImagesBarCell.ACTION_DENOTE_FOR_RACELOG_TRACKING.equals(value)) {
                    denoteForRaceLogTracking(leaderboardDTO);
                }
            }
        });

        leaderboardTable.addColumn(leaderboardNameColumn, stringMessages.name());
        leaderboardTable.addColumn(leaderboardDisplayNameColumn, stringMessages.displayName());
        leaderboardTable.addColumn(leaderboardActionColumn, stringMessages.actions());
        leaderboardTable.addColumnSortHandler(leaderboardColumnListHandler);
        leaderboardTable.setWidth("100%");
        leaderboardSelectionModel = new MultiSelectionModel<StrippedLeaderboardDTO>();
        leaderboardTable.setSelectionModel(leaderboardSelectionModel);
        leaderboardSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                leaderboardSelectionChanged();
            }
        });
        leaderboardList.addDataDisplay(leaderboardTable);
        leaderboardsPanel.add(leaderboardTable);
        HorizontalPanel leaderboardButtonPanel = new HorizontalPanel();
        leaderboardButtonPanel.setSpacing(5);
        leaderboardsPanel.add(leaderboardButtonPanel);

        mainPanel.add(new Grid(1, 1));

        // caption panels for the selected leaderboard and tracked races
        HorizontalPanel splitPanel = new HorizontalPanel();
        mainPanel.add(splitPanel);

        selectedLeaderBoardPanel = new CaptionPanel(stringMessages.leaderboard());
        selectedLeaderBoardPanel.setWidth("50%");
        splitPanel.add(selectedLeaderBoardPanel);

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        selectedLeaderBoardPanel.setContentWidget(vPanel);

        trackedRacesCaptionPanel = new CaptionPanel(stringMessages.trackedRaces());
        trackedRacesCaptionPanel.setWidth("50%");
        splitPanel.add(trackedRacesCaptionPanel);

        VerticalPanel trackedRacesPanel = new VerticalPanel();
        trackedRacesPanel.setWidth("100%");
        trackedRacesCaptionPanel.setContentWidget(trackedRacesPanel);
        trackedRacesCaptionPanel.setStyleName("bold");


        // ------------ races of the selected leaderboard ----------------
        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceNameColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality object) {
                return object.getA().getName();
            }
        };
        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> fleetNameColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality object) {
                return object.getB().getName();
            }
        };

        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceLogTrackingStateColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnAndFleetName) {
                RaceLogTrackingState state = raceColumnAndFleetName.getB().raceLogTrackingState;
                if (raceColumnAndFleetName.getB().raceLogTrackerExists) {
                    if (state == RaceLogTrackingState.TRACKING) {
                        return "Tracking";
                    } else {
                        return "Tracker is listening";
                    }
                } else if (raceColumnAndFleetName.getB().raceLogTrackingState.isForTracking()) {
                    return "Denoted";
                } else {
                    return "Not denoted";
                }
            }
        };

        ImagesBarColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality, RaceLogTrackingEventManagementRaceImagesBarCell> raceActionColumn =
                new ImagesBarColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality, RaceLogTrackingEventManagementRaceImagesBarCell>(
                        new RaceLogTrackingEventManagementRaceImagesBarCell(stringMessages));
        raceActionColumn.setFieldUpdater(new FieldUpdater<RaceColumnDTOAndFleetDTOWithNameBasedEquality, String>() {
            @Override
            public void update(int index, RaceColumnDTOAndFleetDTOWithNameBasedEquality object, String value) {
                if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_ADD_RACELOG_TRACKER.equals(value)) {
                    addRaceLogTracker(object.getA(), object.getB());
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_DENOTE_FOR_RACELOG_TRACKING.equals(value)) {
                    denoteForRaceLogTracking(object.getA(), object.getB());
                }
            }
        });
        
        Label lblRaceNamesIn = new Label(stringMessages.races());
        vPanel.add(lblRaceNamesIn);
        raceColumnTable = new CellTable<RaceColumnDTOAndFleetDTOWithNameBasedEquality>(/* pageSize */200, tableRes);
        raceColumnTable.ensureDebugId("RaceColumnTable");
        raceColumnTable.addColumn(raceNameColumn, stringMessages.race());
        raceColumnTable.addColumn(fleetNameColumn, stringMessages.fleet());
        raceColumnTable.addColumn(raceLogTrackingStateColumn, stringMessages.status());
        raceColumnTable.addColumn(raceActionColumn, stringMessages.actions());
        raceColumnAndFleetList.addDataDisplay(raceColumnTable);
        raceColumnTable.setWidth("500px");
        raceColumnTableSelectionModel = new SingleSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>();
        raceColumnTable.setSelectionModel(raceColumnTableSelectionModel);
        vPanel.add(raceColumnTable);

        HorizontalPanel selectedLeaderboardRaceButtonPanel = new HorizontalPanel();
        selectedLeaderboardRaceButtonPanel.setSpacing(5);
        vPanel.add(selectedLeaderboardRaceButtonPanel);

        loadLeaderboards();
    }

    public void loadLeaderboards() {
        sailingService.getLeaderboards(new AsyncCallback<List<StrippedLeaderboardDTO>>() {
            @Override
            public void onSuccess(List<StrippedLeaderboardDTO> leaderboards) {
                leaderboardList.getList().clear();
                availableLeaderboardList.clear();
                for (StrippedLeaderboardDTO leaderboard : leaderboards) {
                    if (leaderboard.type == LeaderboardType.RegattaLeaderboard) {
                        leaderboardList.getList().add(leaderboard);
                        availableLeaderboardList.add(leaderboard);
                    }
                }
                filterLeaderboardPanel.updateAll(availableLeaderboardList);
                leaderboardSelectionChanged();
            }

            @Override
            public void onFailure(Throwable t) {
                RaceLogTrackingEventManagementPanel.this.errorReporter.reportError("Error trying to obtain list of leaderboards: "
                        + t.getMessage());
            }
        });
    }

    /**
     * @param nameOfRaceColumnToSelect
     *            if not <code>null</code>, selects the first race column name with this name found in the leaderboard
     *            after the refresh has successfully completed. See {@link #selectRaceColumn(String)}.
     */
    public void loadAndRefreshLeaderboard(final String leaderboardName, final String nameOfRaceColumnToSelect) {
        sailingService.getLeaderboard(leaderboardName, new AsyncCallback<StrippedLeaderboardDTO>() {
            @Override
            public void onSuccess(StrippedLeaderboardDTO leaderboard) {
                for (StrippedLeaderboardDTO leaderboardDTO : leaderboardSelectionModel.getSelectedSet()) {
                    if (leaderboardDTO.name.equals(leaderboardName)) {
                        leaderboardSelectionModel.setSelected(leaderboardDTO, false);
                        break;
                    }
                }

                replaceLeaderboardInList(leaderboardList.getList(), leaderboardName, leaderboard);
                replaceLeaderboardInList(availableLeaderboardList, leaderboardName, leaderboard);
                leaderboardSelectionModel.setSelected(leaderboard, true);
                if (nameOfRaceColumnToSelect != null) {
                    selectRaceColumn(nameOfRaceColumnToSelect);
                }
                leaderboardSelectionChanged();
            }

            @Override
            public void onFailure(Throwable t) {
                RaceLogTrackingEventManagementPanel.this.errorReporter.reportError("Error trying to update leaderboard with name " + leaderboardName + " : "
                        + t.getMessage());
            }
        });
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

    private void selectRaceColumn(String raceCoumnName) {
        List<RaceColumnDTOAndFleetDTOWithNameBasedEquality> list = raceColumnAndFleetList.getList();
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality pair : list) {
            if (pair.getA().getName().equals(raceCoumnName)) {
                raceColumnTableSelectionModel.setSelected(pair, true);
                break;
            }
        }
    }

    private RaceColumnDTOAndFleetDTOWithNameBasedEquality getSelectedRaceColumnWithFleet() {
        RaceColumnDTOAndFleetDTOWithNameBasedEquality raceInLeaderboardAndFleetName = raceColumnTableSelectionModel.getSelectedObject();
        return raceInLeaderboardAndFleetName;
    }

    private void leaderboardSelectionChanged() {
        StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard();
        if (leaderboardSelectionModel.getSelectedSet().size() == 1 && selectedLeaderboard != null) {
            raceColumnAndFleetList.getList().clear();
            for (RaceColumnDTO raceColumn : selectedLeaderboard.getRaceList()) {
                for (FleetDTO fleet : raceColumn.getFleets()) {
                    raceColumnAndFleetList.getList().add(new RaceColumnDTOAndFleetDTOWithNameBasedEquality(raceColumn, fleet));
                }
            }
            selectedLeaderBoardPanel.setVisible(true);
            selectedLeaderBoardPanel.setCaptionText("Details of leaderboard '" + selectedLeaderboard.name + "'");
            if (!selectedLeaderboard.type.isMetaLeaderboard()) {
                trackedRacesCaptionPanel.setVisible(true);
            }
        } else {
            selectedLeaderBoardPanel.setVisible(false);
            trackedRacesCaptionPanel.setVisible(false);
        }
    }

    private StrippedLeaderboardDTO getSelectedLeaderboard() {
        return leaderboardSelectionModel.getSelectedSet().isEmpty() ? null : leaderboardSelectionModel.getSelectedSet().iterator().next();
    }

    private void denoteForRaceLogTracking(final StrippedLeaderboardDTO leaderboard) {
        sailingService.denoteForRaceLogTracking(leaderboard.name, new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadAndRefreshLeaderboard(leaderboard.name, null);
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not denote for RaceLog tracking: " + caught.getMessage());
            }
        });
    }

    private void denoteForRaceLogTracking(final RaceColumnDTO raceColumn, final FleetDTO fleet) {
        final StrippedLeaderboardDTO leaderboard = getSelectedLeaderboard();
        sailingService.denoteForRaceLogTracking(leaderboard.name, raceColumn.getName(), fleet.getName(), new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadAndRefreshLeaderboard(leaderboard.name, raceColumn.getName());
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not denote for RaceLog tracking: " + caught.getMessage());
            }
        });
    }

    private void addRaceLogTrackers(final StrippedLeaderboardDTO leaderboard) {
        final RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRCFleet = getSelectedRaceColumnWithFleet();
        if (leaderboard.raceLogTrackersCanBeAdded) {
            sailingService.addRaceLogTrackers(leaderboard.name, new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loadAndRefreshLeaderboard(leaderboard.name, selectedRCFleet.getA().getName());
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Failed to add RaceLog tracker: " + caught.getMessage());
                }
            });
        } else {
            errorReporter.reportError("No RaceLogTracker can be added for any of the RaceLogs in this leaderboard");
        }
    }

    private void addRaceLogTracker(final RaceColumnDTO raceColumn, FleetDTO fleet) {
        if (fleet.raceLogTrackerCanBeAdded) {
            final StrippedLeaderboardDTO leaderboard = getSelectedLeaderboard();
            sailingService.addRaceLogTracker(leaderboard.name, raceColumn.getName(), fleet.getName(), new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loadAndRefreshLeaderboard(leaderboard.name, raceColumn.getName());
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Failed to add RaceLog trackers: " + caught.getMessage());
                }
            });
        } else {
            errorReporter.reportError("No RaceLogTracker can be added for the RaceLog of this fleet");
        }       
    }
}
