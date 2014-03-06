package com.sap.sailing.gwt.ui.adminconsole;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

/**
 * Allows the user to start and stop tracking of races using the RaceLog-tracking connector.
 */
public class RaceLogTrackingEventManagementPanel extends AbstractLeaderboardConfigPanel {
   
    public RaceLogTrackingEventManagementPanel(SailingServiceAsync sailingService, AdminConsoleEntryPoint adminConsole,
            RegattaRefresher regattaRefresher,
            ErrorReporter errorReporter, StringMessages stringMessages) {
        super(sailingService, adminConsole, errorReporter, stringMessages);
    }
    
    @Override
    protected void addColumnsToLeaderboardTable(CellTable<StrippedLeaderboardDTO> leaderboardTable) {
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
    }
    
    @Override
    protected void addColumnsToRacesTable(CellTable<RaceColumnDTOAndFleetDTOWithNameBasedEquality> racesTable) {
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
                return state.name();
            }
        };

        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> trackerStateColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnAndFleetName) {
                return raceColumnAndFleetName.getB().raceLogTrackerExists ? "Active" : "None";
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
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_START_RACELOG_TRACKING.equals(value)) {
                    startRaceLogTracking(object.getA(), object.getB());
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_COMPETITOR_REGISTRATIONS.equals(value)) {
                    boolean editable = object.getB().raceLogTrackingState != RaceLogTrackingState.TRACKING;
                    new CompetitorRegistrationsDialog(sailingService, stringMessages, errorReporter,
                            getSelectedLeaderboardName(), object.getA().getName(), object.getB().getName(), editable).show();
                }
            }
        });
        
        racesTable.addColumn(raceNameColumn, stringMessages.race());
        racesTable.addColumn(fleetNameColumn, stringMessages.fleet());
        racesTable.addColumn(raceLogTrackingStateColumn, stringMessages.raceStatusColumn());
        racesTable.addColumn(trackerStateColumn, stringMessages.trackerStatus());
        racesTable.addColumn(raceActionColumn, stringMessages.actions());
    }

    @Override
    protected void addLeaderboardConfigControls(Panel configPanel) {}

    @Override
    protected void addLeaderboardCreateControls(Panel createPanel) {}

    @Override
    protected void addSelectedLeaderboardRacesControls(Panel racesPanel) {}

    @Override
    protected void leaderboardRaceColumnSelectionChanged() {
        RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceInLeaderboard = getSelectedRaceColumnWithFleet();
        if (selectedRaceInLeaderboard != null) {
            selectTrackedRaceInRaceList();
        } else {
            trackedRacesListComposite.clearSelection();
        }
    }
    
    @Override
    protected void leaderboardSelectionChanged() {
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
                    trackedRacesListComposite.regattaRefresher.fillRegattas();
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
                    trackedRacesListComposite.regattaRefresher.fillRegattas();
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

    private void startRaceLogTracking(final RaceColumnDTO raceColumn, FleetDTO fleet) {
        final StrippedLeaderboardDTO leaderboard = getSelectedLeaderboard();
        sailingService.startRaceLogTracking(leaderboard.name, raceColumn.getName(), fleet.getName(), new AsyncCallback<Void>() {
            
            @Override
            public void onSuccess(Void result) {
                loadAndRefreshLeaderboard(leaderboard.name, raceColumn.getName());
                trackedRacesListComposite.regattaRefresher.fillRegattas();
            }
            
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Failed to start tracking: " + caught.getMessage());
            }
        });
    }
}
