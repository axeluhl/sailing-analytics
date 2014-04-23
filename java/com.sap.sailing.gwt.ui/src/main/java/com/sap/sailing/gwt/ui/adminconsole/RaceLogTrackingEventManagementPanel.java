package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeAndProcedureDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;

/**
 * Allows the user to start and stop tracking of races using the RaceLog-tracking connector.
 */
public class RaceLogTrackingEventManagementPanel extends AbstractLeaderboardConfigPanel {
    private Button startTrackingButton;
    private MultiSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality> multiRaceSelectionModel;
    
    public RaceLogTrackingEventManagementPanel(SailingServiceAsync sailingService, AdminConsoleEntryPoint adminConsole,
            RegattaRefresher regattaRefresher,
            ErrorReporter errorReporter, StringMessages stringMessages) {
        super(sailingService, adminConsole, errorReporter, stringMessages,
                new MultiSelectionModel<RaceColumnDTOAndFleetDTOWithNameBasedEquality>());
        multiRaceSelectionModel = new MultiSelectionModel<>();
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
                if (RaceLogTrackingEventManagementImagesBarCell.ACTION_DENOTE_FOR_RACELOG_TRACKING.equals(value)) {
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
                final String leaderboardName = getSelectedLeaderboardName();
                final String raceColumnName = object.getA().getName();
                final String fleetName = object.getB().getName();
                boolean editable = object.getB().raceLogTrackingState != RaceLogTrackingState.TRACKING;
                if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_DENOTE_FOR_RACELOG_TRACKING.equals(value)) {
                    denoteForRaceLogTracking(object.getA(), object.getB());
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_REMOVE_DENOTATION.equals(value)) {
                    removeDenotation(object.getA(), object.getB());
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_COMPETITOR_REGISTRATIONS.equals(value)) {
                    new RaceLogTrackingCompetitorRegistrationsDialog(sailingService, stringMessages, errorReporter,
                            getSelectedLeaderboardName(), object.getA().getName(), object.getB().getName(), editable).show();
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_DEFINE_COURSE.equals(value)) {
                    new RaceLogTrackingCourseDefinitionDialog(sailingService, stringMessages, errorReporter, leaderboardName, raceColumnName, fleetName).show();
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_MAP_DEVICES.equals(value)) {
                    new RaceLogTrackingDeviceMappingsDialog(sailingService, stringMessages, errorReporter, leaderboardName, raceColumnName, fleetName).show();
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_COPY_COURSE.equals(value)) {
                    final Label selectToCopy = new Label(stringMessages.selectRowInTableToCopyCourse());
                    final VerticalPanel panelToAddTo = (VerticalPanel) selectedLeaderBoardPanel.getContentWidget();
                    panelToAddTo.add(selectToCopy);
                    raceColumnTableSelectionModel.addSelectionChangeHandler(new Handler() {
                        private boolean done = false;
                        @Override
                        public void onSelectionChange(SelectionChangeEvent event) {
                            if (! done && raceColumnTableSelectionModel.getSelectedSet().size() == 1) {
                                final String leaderboardTo = getSelectedLeaderboardName();
                                final String raceColumnTo = getSelectedRaceColumnWithFleet().getA().getName();
                                String fleetTo = getSelectedRaceColumnWithFleet().getB().getName();
                                if (!(leaderboardTo.equals(leaderboardName) && raceColumnTo.equals(raceColumnName) && fleetTo
                                        .equals(fleetName)) && getSelectedRaceColumnWithFleet().getB().raceLogTrackingState.isForTracking()) {

                                    done = true;
                                    panelToAddTo.remove(selectToCopy);
                                    sailingService.copyCourseToOtherRaceLog(leaderboardName, raceColumnName, fleetName,
                                            leaderboardTo, raceColumnTo, fleetTo, new AsyncCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void result) {
                                            loadAndRefreshLeaderboard(leaderboardTo, raceColumnTo);
                                        }

                                        @Override
                                        public void onFailure(Throwable caught) {
                                            errorReporter.reportError("Could not copy course: " + caught.getMessage());
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_SET_START_TIME.equals(value)) {
                    setStartTime(getSelectedRaceColumnWithFleet().getA(), getSelectedRaceColumnWithFleet().getB());
                }
            }
        });
        
        racesTable.addColumn(raceNameColumn, stringMessages.race());
        racesTable.addColumn(fleetNameColumn, stringMessages.fleet());
        racesTable.addColumn(raceLogTrackingStateColumn, stringMessages.raceStatusColumn());
        racesTable.addColumn(trackerStateColumn, stringMessages.trackerStatus());
        racesTable.addColumn(raceActionColumn, stringMessages.actions());
        racesTable.setWidth("600px");
        
        //set multi selection model, overriding single selection model
        racesTable.setSelectionModel(multiRaceSelectionModel);
    }

    @Override
    protected void addLeaderboardConfigControls(Panel configPanel) {}

    @Override
    protected void addLeaderboardCreateControls(Panel createPanel) {}

    @Override
    protected void addSelectedLeaderboardRacesControls(Panel racesPanel) {
        startTrackingButton = new Button(stringMessages.startTracking());
        startTrackingButton.setEnabled(false);
        racesPanel.add(startTrackingButton);
        startTrackingButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                startTracking(raceColumnTableSelectionModel.getSelectedSet());
            }
        });
        
        raceColumnTableSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                enableStartTrackingButtonIfAppropriateRacesSelected();
            }
        });
    }
    
    private void enableStartTrackingButtonIfAppropriateRacesSelected() {
        boolean enable = raceColumnTableSelectionModel.getSelectedSet().size() > 0;
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality race : raceColumnTableSelectionModel.getSelectedSet()) {
            if (! race.getB().raceLogTrackingState.isForTracking() || race.getB().raceLogTrackerExists) {
                enable = false;
            }
        }
        startTrackingButton.setEnabled(enable);
    }

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
                raceColumnTableSelectionModel.clear();
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
    
    private void removeDenotation(final RaceColumnDTO raceColumn, final FleetDTO fleet) {
        final StrippedLeaderboardDTO leaderboard = getSelectedLeaderboard();
        sailingService.removeDenotationForRaceLogTracking(leaderboard.name, raceColumn.getName(), fleet.getName(), new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadAndRefreshLeaderboard(leaderboard.name, raceColumn.getName());
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not remove denotation: " + caught.getMessage());
            }
        });
    }
    
    private void startTracking(Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality> races) {
        final StrippedLeaderboardDTO leaderboard = getSelectedLeaderboard();
        
        //prompt user if competitor registrations are missing for same races
        String namesOfRacesMissingRegistrations = "";
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality race : races) {
            if (! race.getB().competitorRegistrationsExist) {
                namesOfRacesMissingRegistrations += race.getA().getName() + "/" + race.getB().getName() + " ";
            }
        }
        if (! namesOfRacesMissingRegistrations.isEmpty()) {
            boolean proceed = Window.confirm(stringMessages.competitorRegistrationsMissingProceed(
                    namesOfRacesMissingRegistrations));
            if (! proceed) {
                return;
            }
        }

        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality race : races) {
            final RaceColumnDTO raceColumn = race.getA();
            final FleetDTO fleet = race.getB();

            sailingService.startRaceLogTracking(leaderboard.name, raceColumn.getName(), fleet.getName(),
                    new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    loadAndRefreshLeaderboard(leaderboard.name, raceColumn.getName());
                    trackedRacesListComposite.regattaRefresher.fillRegattas();
                }

                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Failed to start tracking " + raceColumn.getName() + " - "
                            + fleet.getName() + ": " + caught.getMessage());
                }
            });
        }
    }
    
    private void setStartTime(RaceColumnDTO raceColumnDTO, FleetDTO fleetDTO) {
        new SetStartTimeDialog(sailingService, errorReporter, getSelectedLeaderboardName(), raceColumnDTO.getName(), 
                fleetDTO.getName(), stringMessages, new DataEntryDialog.DialogCallback<RaceLogSetStartTimeAndProcedureDTO>() {
            @Override
            public void ok(RaceLogSetStartTimeAndProcedureDTO editedObject) {
                sailingService.setStartTimeAndProcedure(editedObject, new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Boolean result) {
                        if (!result) {
                            Window.alert(stringMessages.failedToSetNewStartTime());
                        } else {
                            trackedRacesListComposite.regattaRefresher.fillRegattas();
                        }
                    }
                });
            }

            @Override
            public void cancel() { }
        }).show();
    }
}
