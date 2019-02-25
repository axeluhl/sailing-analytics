package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.LocaleInfo;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.sap.sailing.domain.common.CourseDesignerMode;
import com.sap.sailing.domain.common.LeaderboardNameConstants;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.TrackedRaceStatusEnum;
import com.sap.sailing.domain.common.abstractlog.TimePointSpecificationFoundInLog;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.CompetitorWithBoatDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.domain.common.dto.RaceDTO;
import com.sap.sailing.domain.common.racelog.tracking.RaceLogTrackingState;
import com.sap.sailing.domain.common.security.SecuredDomainType;
import com.sap.sailing.gwt.ui.client.LeaderboardsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardsRefresher;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.RaceMapSettings;
import com.sap.sailing.gwt.ui.shared.DeviceConfigurationDTO;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetFinishingAndFinishTimeDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeAndProcedureDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTOWithSecurity;
import com.sap.sse.common.Distance;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.Util.Triple;
import com.sap.sse.common.util.NaturalComparator;
import com.sap.sse.gwt.adminconsole.AdminConsoleTableResources;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.Notification;
import com.sap.sse.gwt.client.Notification.NotificationType;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.celltable.FlushableCellTable;
import com.sap.sse.gwt.client.celltable.ImagesBarColumn;
import com.sap.sse.gwt.client.celltable.SelectionCheckboxColumn;
import com.sap.sse.gwt.client.dialog.DataEntryDialog;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;
import com.sap.sse.security.shared.HasPermissions;
import com.sap.sse.security.shared.HasPermissions.DefaultActions;
import com.sap.sse.security.ui.client.UserService;
import com.sap.sse.security.ui.client.component.AccessControlledButtonPanel;
import com.sap.sse.security.ui.client.component.EditOwnershipDialog;
import com.sap.sse.security.ui.client.component.SecuredDTOOwnerColumn;
import com.sap.sse.security.ui.client.component.editacl.EditACLDialog;

/**
 * Allows the user to start and stop tracking of races using the RaceLog-tracking connector.
 */
public class SmartphoneTrackingEventManagementPanel
        extends AbstractLeaderboardConfigPanel
        implements LeaderboardsDisplayer<StrippedLeaderboardDTOWithSecurity> {
    private ToggleButton startStopTrackingButton;
    private TrackFileImportDeviceIdentifierTableWrapper deviceIdentifierTable;
    private CheckBox correctWindDirectionForDeclination;
    private CheckBox trackWind;
    private ImagesBarColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality, RaceLogTrackingEventManagementRaceImagesBarCell> raceActionColumn;
    protected boolean regattaHasCompetitors = false; 
    private Map<Triple<String, String, String>, Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>> raceWithStartAndEndOfTrackingTime = new HashMap<>();
    
    public SmartphoneTrackingEventManagementPanel(SailingServiceAsync sailingService, UserService userService,
            RegattaRefresher regattaRefresher,
            LeaderboardsRefresher<StrippedLeaderboardDTOWithSecurity> leaderboardsRefresher,
            ErrorReporter errorReporter, StringMessages stringMessages) {
        super(sailingService, userService, regattaRefresher, leaderboardsRefresher, errorReporter,
                stringMessages, /* multiSelection */ true);
        // add upload panel
        CaptionPanel importPanel = new CaptionPanel(stringMessages.importFixes());
        VerticalPanel importContent = new VerticalPanel();
        mainPanel.add(importPanel);
        deviceIdentifierTable = new TrackFileImportDeviceIdentifierTableWrapper(sailingService, stringMessages, errorReporter);
        TrackFileImportWidget importWidget = new TrackFileImportWidget(deviceIdentifierTable, stringMessages,
                sailingService, errorReporter);
        importPanel.add(importContent);
        importContent.add(importWidget);
        importContent.add(deviceIdentifierTable);
        trackedRacesListComposite.addTrackedRaceChangeListener(new TrackedRaceChangedListener() {
            
            @Override
            public void racesStoppedTracking(Iterable<? extends RegattaAndRaceIdentifier> regattaAndRaceIdentifiers) {
                loadAndRefreshLeaderboard(getSelectedLeaderboard().getName()); 
            }
            
            @Override
            public void racesRemoved(Iterable<? extends RegattaAndRaceIdentifier> regattaAndRaceIdentifiers) {
                loadAndRefreshLeaderboard(getSelectedLeaderboard().getName()); 
            }
        });
    }

    /**
     * When doing race log tracking, the Remove and Stop Tracking buttons are required.
     */
    protected boolean isActionButtonsEnabled() {
        return /* actionButtonsEnabled */ true;
    }
    
    @Override
    protected void addColumnsToLeaderboardTableAndSetSelectionModel(UserService userService,
            FlushableCellTable<StrippedLeaderboardDTOWithSecurity> leaderboardTable,
            AdminConsoleTableResources tableResources,
            ListDataProvider<StrippedLeaderboardDTOWithSecurity> listDataProvider) {
        ListHandler<StrippedLeaderboardDTOWithSecurity> leaderboardColumnListHandler = new ListHandler<StrippedLeaderboardDTOWithSecurity>(
                filteredLeaderboardList.getList());
        SelectionCheckboxColumn<StrippedLeaderboardDTOWithSecurity> selectionCheckboxColumn = createSortableSelectionCheckboxColumn(
                leaderboardTable, tableResources, leaderboardColumnListHandler, listDataProvider);
        TextColumn<StrippedLeaderboardDTOWithSecurity> leaderboardNameColumn = new TextColumn<StrippedLeaderboardDTOWithSecurity>() {
            @Override
            public String getValue(StrippedLeaderboardDTOWithSecurity leaderboard) {
                return leaderboard.getName();
            }
        };
        leaderboardNameColumn.setSortable(true);
        leaderboardColumnListHandler.setComparator(leaderboardNameColumn,
                new Comparator<StrippedLeaderboardDTOWithSecurity>() {
            @Override
                    public int compare(StrippedLeaderboardDTOWithSecurity o1, StrippedLeaderboardDTOWithSecurity o2) {
                return new NaturalComparator(false).compare(o1.getName(), o2.getName());
            }
        });

        TextColumn<StrippedLeaderboardDTOWithSecurity> leaderboardDisplayNameColumn = new TextColumn<StrippedLeaderboardDTOWithSecurity>() {
            @Override
            public String getValue(StrippedLeaderboardDTOWithSecurity leaderboard) {
                return leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : "";
            }
        };
        leaderboardDisplayNameColumn.setSortable(true);
        leaderboardColumnListHandler.setComparator(leaderboardDisplayNameColumn,
                new Comparator<StrippedLeaderboardDTOWithSecurity>() {
                    @Override
                    public int compare(StrippedLeaderboardDTOWithSecurity o1, StrippedLeaderboardDTOWithSecurity o2) {
                        return new NaturalComparator(false).compare(o1.getDisplayName(), o2.getDisplayName());
                    }
                });

        TextColumn<StrippedLeaderboardDTOWithSecurity> leaderboardCanBoatsOfCompetitorsChangePerRaceColumn = new TextColumn<StrippedLeaderboardDTOWithSecurity>() {
            @Override
            public String getValue(StrippedLeaderboardDTOWithSecurity leaderboard) {
                return leaderboard.canBoatsOfCompetitorsChangePerRace ? stringMessages.yes() : stringMessages.no();
            }
        };
        leaderboardCanBoatsOfCompetitorsChangePerRaceColumn.setSortable(true);
        leaderboardColumnListHandler.setComparator(leaderboardCanBoatsOfCompetitorsChangePerRaceColumn, (l1, l2)->
            Boolean.valueOf(l1.canBoatsOfCompetitorsChangePerRace).compareTo(Boolean.valueOf(l2.canBoatsOfCompetitorsChangePerRace)));

        final HasPermissions type = SecuredDomainType.EVENT;

        final EditOwnershipDialog.DialogConfig<StrippedLeaderboardDTOWithSecurity> configOwnership = EditOwnershipDialog
                .create(userService.getUserManagementService(), type, leaderboard -> {
                }, stringMessages);

        final EditACLDialog.DialogConfig<StrippedLeaderboardDTOWithSecurity> configACL = EditACLDialog.create(
                userService.getUserManagementService(), type, leaderboard -> leaderboard.getAccessControlList(),
                stringMessages);

        ImagesBarColumn<StrippedLeaderboardDTOWithSecurity, RaceLogTrackingEventManagementImagesBarCell> leaderboardActionColumn = new ImagesBarColumn<StrippedLeaderboardDTOWithSecurity, RaceLogTrackingEventManagementImagesBarCell>(
                new RaceLogTrackingEventManagementImagesBarCell(stringMessages));
        leaderboardActionColumn.setFieldUpdater(new FieldUpdater<StrippedLeaderboardDTOWithSecurity, String>() {
            @Override
            public void update(int index, StrippedLeaderboardDTOWithSecurity leaderboardDTO, String value) {
                final String leaderboardName = leaderboardDTO.getName();
                final boolean canBoatsOfCompetitorsChangePerRace = leaderboardDTO.canBoatsOfCompetitorsChangePerRace;
                if (RaceLogTrackingEventManagementImagesBarCell.ACTION_DENOTE_FOR_RACELOG_TRACKING.equals(value)) {
                    denoteForRaceLogTracking(leaderboardDTO);
                } else if (RaceLogTrackingEventManagementImagesBarCell.ACTION_COMPETITOR_REGISTRATIONS.equals(value)) {
                    RegattaDTO regatta = getSelectedRegatta();
                    String boatClassName = regatta.boatClass.getName();

                    new RegattaLogCompetitorRegistrationDialog(boatClassName, sailingService, userService, stringMessages,
                            errorReporter, /* editable */true, leaderboardName, canBoatsOfCompetitorsChangePerRace,
                            new DialogCallback<Set<CompetitorDTO>>() {
                                @Override
                                public void ok(Set<CompetitorDTO> registeredCompetitors) {
                                    sailingService.setCompetitorRegistrationsInRegattaLog(leaderboardName,
                                            registeredCompetitors, new AsyncCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void result) {
                                                     // pass
                                                }

                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    errorReporter
                                                            .reportError("Could not save competitor registrations: "
                                                                    + caught.getMessage());
                                                }
                                            });
                                }

                                @Override
                                public void cancel() {
                                }
                            }).show();
                } else if (RaceLogTrackingEventManagementImagesBarCell.ACTION_BOAT_REGISTRATIONS.equals(value)) {
                    if (canBoatsOfCompetitorsChangePerRace) {
                        RegattaDTO regatta = getSelectedRegatta();
                        String boatClassName = regatta.boatClass.getName();
                        
                        new RegattaLogBoatRegistrationDialog(boatClassName, sailingService, userService, stringMessages,
                                errorReporter, /* editable */true, leaderboardName, canBoatsOfCompetitorsChangePerRace,
                                new DialogCallback<Set<BoatDTO>>() {
                                    @Override
                                    public void ok(Set<BoatDTO> registeredBoats) {
                                        sailingService.setBoatRegistrationsInRegattaLog(leaderboardName,
                                            registeredBoats, new AsyncCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void result) {
                                                    // pass
                                                }

                                                @Override
                                                public void onFailure(Throwable caught) {
                                                    errorReporter.reportError("Could not save boat registrations: " + caught.getMessage());
                                                }
                                            });
                                    }

                                    @Override
                                    public void cancel() {
                                    }
                                }).show();
                    } else {
                        Notification.notify(stringMessages.canNotRegisterBoats(), NotificationType.ERROR);
                    }
                } else if (RaceLogTrackingEventManagementImagesBarCell.ACTION_MAP_DEVICES.equals(value)) {
                    sailingService.getSecretForRegattaByName(leaderboardName, new AsyncCallback<String>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            // if this happens, the user did apparently not have sufficient rights.
                            Notification.notify(stringMessages.youDontHaveRequiredPermission(), NotificationType.ERROR);
                        }

                        @Override
                        public void onSuccess(String secret) {
                            new RegattaLogTrackingDeviceMappingsDialog(sailingService, userService, stringMessages,
                                    errorReporter, leaderboardName, secret, new DialogCallback<Void>() {
                                        @Override
                                        public void ok(Void editedObject) {
                                        }

                                        @Override
                                        public void cancel() {
                                        }
                                    }).show();
                        }
                    });
                } else if (RaceLogTrackingEventManagementImagesBarCell.ACTION_INVITE_BUOY_TENDERS.equals(value)) {
                    openChooseEventDialogAndSendMails(leaderboardName);
                } else if (RaceLogTrackingEventManagementImagesBarCell.ACTION_SHOW_REGATTA_LOG.equals(value)) {
                    showRegattaLog();
                } else if (DefaultActions.CHANGE_OWNERSHIP.name().equals(value)) {
                    configOwnership.openDialog(getSelectedLeaderboard());
                } else if (DefaultActions.CHANGE_ACL.name().equals(value)) {
                    configACL.openDialog(getSelectedLeaderboard());
                }
            }

            
        });
        leaderboardTable.addColumn(selectionCheckboxColumn, selectionCheckboxColumn.getHeader());
        leaderboardTable.addColumn(leaderboardNameColumn, stringMessages.name());
        leaderboardTable.addColumn(leaderboardDisplayNameColumn, stringMessages.displayName());
        leaderboardTable.addColumn(leaderboardCanBoatsOfCompetitorsChangePerRaceColumn, stringMessages.canBoatsChange());
        SecuredDTOOwnerColumn.configureOwnerColumns(leaderboardTable, leaderboardColumnListHandler, stringMessages);
        leaderboardTable.addColumn(leaderboardActionColumn, stringMessages.actions());
        leaderboardTable.addColumnSortHandler(leaderboardColumnListHandler);
        leaderboardTable.setSelectionModel(selectionCheckboxColumn.getSelectionModel(), selectionCheckboxColumn.getSelectionManager());
    }
    
    private RaceLogTrackingState getTrackingState(
            RaceColumnDTOAndFleetDTOWithNameBasedEquality race) {
        return race.getA().getRaceLogTrackingInfo(race.getB()).raceLogTrackingState;
    }
    
    private boolean trackerExists(RaceColumnDTOAndFleetDTOWithNameBasedEquality race) {
        return race.getA().getRaceLogTrackingInfo(race.getB()).raceLogTrackerExists;
    }
    
    private boolean isFinished(RaceColumnDTOAndFleetDTOWithNameBasedEquality race) {
        RaceDTO raceDTO = race.getA().getRace(race.getB());
        
        boolean raceFinished = false;
        if (raceDTO != null){
            raceFinished = raceDTO.status.status.equals(TrackedRaceStatusEnum.FINISHED);
        }
        return raceFinished;
    }
    
    private boolean doesTrackerExist(
            RaceColumnDTOAndFleetDTOWithNameBasedEquality race) {
        return race.getA().getRaceLogTrackingInfo(race.getB()).raceLogTrackerExists;
    }

    private boolean doesCourseExist(RaceColumnDTOAndFleetDTOWithNameBasedEquality race) {
        return race.getA().getRaceLogTrackingInfo(race.getB()).courseExists;
    }

    private boolean doCompetitorRegistrationsExist(
            RaceColumnDTOAndFleetDTOWithNameBasedEquality race) {
        return race.getA().getRaceLogTrackingInfo(race.getB()).competitorRegistrationsExists;
    }
    
    @Override
    protected void addColumnsToRacesTable(
            CellTable<RaceColumnDTOAndFleetDTOWithNameBasedEquality> racesTable) {
        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> raceLogTrackingStateColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(
                    RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnAndFleetName) {
                RaceLogTrackingState state = getTrackingState(raceColumnAndFleetName);
                return state.name();
            }
        };

        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> trackerStateColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(
                    RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnAndFleetName) {
                return doesTrackerExist(raceColumnAndFleetName) ? stringMessages.active() : stringMessages.none();
            }
        };

        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> courseStateColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(
                    RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnAndFleetName) {
                return doesCourseExist(raceColumnAndFleetName) ? stringMessages.ok() : stringMessages.none();
            }
        };

        raceActionColumn =
                new ImagesBarColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality, RaceLogTrackingEventManagementRaceImagesBarCell>(
                        new RaceLogTrackingEventManagementRaceImagesBarCell(stringMessages, this));
        raceActionColumn.setFieldUpdater(
                new FieldUpdater<RaceColumnDTOAndFleetDTOWithNameBasedEquality, String>() {
            @Override
                    public void update(int index,
                            final RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnDTOAndFleetDTO,
                            String value) {
                final String leaderboardName = getSelectedLeaderboardName();
                final boolean canBoatsOfCompetitorsChangePerRace = canBoatsOfCompetitorsChangePerRace();
                final String raceColumnName = raceColumnDTOAndFleetDTO.getA().getName();
                final String fleetName = raceColumnDTOAndFleetDTO.getB().getName();
                final boolean editable = ! (doesTrackerExist(raceColumnDTOAndFleetDTO) &&
                        getTrackingState(raceColumnDTOAndFleetDTO) == RaceLogTrackingState.TRACKING);
                if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_DENOTE_FOR_RACELOG_TRACKING.equals(value)) {
                    denoteForRaceLogTracking(raceColumnDTOAndFleetDTO.getA(), raceColumnDTOAndFleetDTO.getB());
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_REMOVE_DENOTATION.equals(value)) {
                    removeDenotation(raceColumnDTOAndFleetDTO.getA(), raceColumnDTOAndFleetDTO.getB());
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_COMPETITOR_REGISTRATIONS.equals(value)) {
                    registerCompetitorsInRaceLog(getSelectedRegatta(), editable, leaderboardName, canBoatsOfCompetitorsChangePerRace, raceColumnName, fleetName,
                            raceColumnDTOAndFleetDTO);
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_DEFINE_COURSE.equals(value)) {
                    new RaceLogTrackingCourseDefinitionDialog(sailingService, stringMessages, errorReporter, leaderboardName, raceColumnName, 
                            fleetName, new DialogCallback<RaceLogTrackingCourseDefinitionDialog.Result>() {
                        @Override
                        public void cancel() {
                        }

                        @Override
                        public void ok(RaceLogTrackingCourseDefinitionDialog.Result waypointPairs) {
                            sailingService.addCourseDefinitionToRaceLog(leaderboardName, raceColumnName, fleetName, waypointPairs.getWaypoints(),
                                    waypointPairs.getPriority(), new AsyncCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    loadAndRefreshLeaderboard(leaderboardName);
                                }

                                @Override
                                public void onFailure(Throwable caught) {
                                    errorReporter.reportError("Could note save course: " + caught.getMessage());
                                }
                            });
                        }
                    }).show();
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_COPY.equals(value)) {
                            List<RaceColumnDTOAndFleetDTOWithNameBasedEquality> races =
                            new ArrayList<>(raceColumnTable.getDataProvider().getList());
                    races.remove(raceColumnDTOAndFleetDTO);
                    Distance buoyZoneRadius = getSelectedRegatta() == null
                            ? RaceMapSettings.DEFAULT_BUOY_ZONE_RADIUS
                            : getSelectedRegatta().getCalculatedBuoyZoneRadius();
                    new CopyCourseAndCompetitorsDialog(sailingService, errorReporter, stringMessages, races,
                            leaderboardName, buoyZoneRadius, new DialogCallback<CourseAndCompetitorCopyOperation>() {
                                @Override
                                public void ok(CourseAndCompetitorCopyOperation operation) {
                                    operation.perform(leaderboardName, raceColumnDTOAndFleetDTO, /* onSuccessCallback */ new Runnable() {
                                        @Override public void run() { loadAndRefreshLeaderboard(leaderboardName); }});
                                }

                                @Override
                                public void cancel() {}
                    }).show();
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    editRaceColumnOfLeaderboard(raceColumnDTOAndFleetDTO);
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_UNLINK.equals(value)) {
                    unlinkRaceColumnFromTrackedRace(raceColumnDTOAndFleetDTO.getA().getRaceColumnName(), raceColumnDTOAndFleetDTO.getB());
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_REFRESH_RACELOG.equals(value)) {
                    refreshRaceLog(raceColumnDTOAndFleetDTO.getA(), raceColumnDTOAndFleetDTO.getB(), true);
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_SET_STARTTIME.equals(value)) {
                    setStartTime(getSelectedRaceColumnWithFleet().getA(), getSelectedRaceColumnWithFleet().getB());
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_SET_FINISHING_AND_FINISH_TIME.equals(value)) {
                    setEndTime(getSelectedRaceColumnWithFleet().getA(), getSelectedRaceColumnWithFleet().getB());
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_SHOW_RACELOG.equals(value)) {
                    showRaceLog(raceColumnDTOAndFleetDTO.getA(), raceColumnDTOAndFleetDTO.getB());
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_SET_TRACKING_TIMES.equals(value)) {
                    showSetTrackingTimesDialog(getSelectedRaceColumnWithFleet().getA(), getSelectedRaceColumnWithFleet().getB());;
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_START_TRACKING.equals(value)) {
                    startTracking(Collections.singleton(raceColumnDTOAndFleetDTO), trackWind.getValue(), correctWindDirectionForDeclination.getValue());
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_STOP_TRACKING.equals(value)) {
                    stopTracking(Collections.singleton(raceColumnDTOAndFleetDTO));
                } else if (RaceLogTrackingEventManagementRaceImagesBarCell.ACTION_EDIT_COMPETITOR_TO_BOAT_MAPPINGS.equals(value)) {
                    showCompetitorToBoatMappings(raceColumnDTOAndFleetDTO.getA(), raceColumnDTOAndFleetDTO.getB());
                } 
            }
        });
        
        racesTable.addColumn(raceLogTrackingStateColumn, stringMessages.raceStatusColumn());
        racesTable.addColumn(trackerStateColumn, stringMessages.trackerStatus());
        racesTable.addColumn(courseStateColumn, stringMessages.courseStatus());
        racesTable.addColumn(raceActionColumn, stringMessages.actions());
        racesTable.setWidth("600px");
    }
    
    private void registerCompetitorsInRaceLog(RegattaDTO selectedRegatta, boolean editable, String leaderboardName, 
            boolean canBoatsOfCompetitorsChangePerRace, String raceColumnName, String fleetName,
            RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnDTOAndFleetDTO) {
        RegattaDTO regatta = getSelectedRegatta();
        String boatClassName = regatta.boatClass.getName();
        RaceLogCompetitorRegistrationDialog dialog = new RaceLogCompetitorRegistrationDialog(boatClassName, sailingService, userService, stringMessages,
            errorReporter, editable, leaderboardName, canBoatsOfCompetitorsChangePerRace, raceColumnName, fleetName,
            raceColumnDTOAndFleetDTO.getA().getFleets(), new DialogCallback<Set<CompetitorDTO>>() {
                @Override
                public void ok(final Set<CompetitorDTO> registeredCompetitors) {
                    if (canBoatsOfCompetitorsChangePerRace) {
                        sailingService.getCompetitorAndBoatRegistrationsInRaceLog(leaderboardName, raceColumnName, fleetName, new AsyncCallback<Map<CompetitorDTO, BoatDTO>>() {
                            @Override
                            public void onSuccess(Map<CompetitorDTO, BoatDTO> existingCompetitorToBoatMappings) {
                                // remove the competitors which has been removed in the first dialog (competitor selection)
                                Map<CompetitorDTO, BoatDTO> newCompetitorToBoatMappings = new HashMap<>();
                                for (CompetitorDTO competitorDTO : registeredCompetitors) {
                                    if (existingCompetitorToBoatMappings.containsKey((competitorDTO))) {
                                        BoatDTO boatDTO = existingCompetitorToBoatMappings.get(competitorDTO);
                                        newCompetitorToBoatMappings.put(competitorDTO, boatDTO);
                                    } else {
                                        newCompetitorToBoatMappings.put(competitorDTO, null);
                                    }
                                }
                                new CompetitorToBoatMappingsDialog(sailingService, stringMessages,
                                        errorReporter, leaderboardName, newCompetitorToBoatMappings, new DialogCallback<Map<CompetitorDTO, BoatDTO>>() {
                                    @Override
                                    public void ok(final Map<CompetitorDTO, BoatDTO> competitorToBoatMappings) {
                                        sailingService.setCompetitorRegistrationsInRaceLog(leaderboardName, raceColumnName,
                                            fleetName, competitorToBoatMappings, new AsyncCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void result) {
                                            }

                                            @Override
                                            public void onFailure(Throwable caught) {
                                                errorReporter.reportError("Could not save competitor and boat registrations: " + caught.getMessage());
                                            }
                                        });
                                    }
                                    @Override
                                    public void cancel() {
                                    }
                                }).show();                                    
                            }
                            
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Could not read the competitor/boat assignments: " + caught.getMessage());
                            }
                        });
                    } else {
                        final Set<CompetitorWithBoatDTO> registeredCompetitorsWithBoat = new HashSet<>();
                        for (final CompetitorDTO competitor : registeredCompetitors) {
                            registeredCompetitorsWithBoat.add((CompetitorWithBoatDTO) competitor);
                        }
                        sailingService.setCompetitorRegistrationsInRaceLog(leaderboardName, raceColumnName,
                            fleetName, registeredCompetitorsWithBoat, new AsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Could not save competitor registrations: " + caught.getMessage());
                            }
                        });
                    }
                }

                @Override
                public void cancel() {
                }
            });
        
        dialog.show();
    }
    
    @Override
    protected void addLeaderboardControls(final AccessControlledButtonPanel buttonPanel) {}

    @Override
    protected void addSelectedLeaderboardRacesControls(Panel racesPanel) {
        trackWind = new CheckBox(stringMessages.trackWind());
        trackWind.setValue(true);
        correctWindDirectionForDeclination = new CheckBox(stringMessages.declinationCheckbox());
        correctWindDirectionForDeclination.setValue(true);
        startStopTrackingButton = new ToggleButton(stringMessages.startTracking(), stringMessages.stopTracking());
        startStopTrackingButton.ensureDebugId("StartTrackingButton");
        startStopTrackingButton.setEnabled(false);
        racesPanel.add(trackWind);
        racesPanel.add(correctWindDirectionForDeclination);
        racesPanel.add(startStopTrackingButton);
        startStopTrackingButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (startStopTrackingButton.isDown()){
                    startTracking(raceColumnTableSelectionModel.getSelectedSet(), trackWind.getValue(), correctWindDirectionForDeclination.getValue());
                } else {
                    stopTracking(raceColumnTableSelectionModel.getSelectedSet());
                }
                refreshTrackingActionButtons();
            }
        });
        
        raceColumnTableSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                enableStartTrackingButtonIfAppropriateRacesSelected();
            }
        });
    }
    
    private void stopTracking(
            final Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality> selectedSet) {
        final List<RegattaAndRaceIdentifier> racesToStopTracking = new ArrayList<RegattaAndRaceIdentifier>();        
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnDTOAndFleetDTOWithNameBasedEquality : selectedSet) {
            RaceDTO race = raceColumnDTOAndFleetDTOWithNameBasedEquality.getA().getRace(raceColumnDTOAndFleetDTOWithNameBasedEquality.getB());
            if (race != null && race.isTracked){
                racesToStopTracking.add(race.getRaceIdentifier());
            }   
        }
        sailingService.stopTrackingRaces(racesToStopTracking, new MarkedAsyncCallback<Void>(
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(stringMessages.errorStoppingRaceTracking(Util.toStringOrNull(racesToStopTracking), caught.getMessage()));
                    }
        
                    @Override
                    public void onSuccess(Void result) {
                        trackedRacesListComposite.regattaRefresher.fillRegattas();
                        for (TrackedRaceChangedListener listener : trackedRacesListComposite.raceIsTrackedRaceChangeListener) {
                            listener.racesStoppedTracking(racesToStopTracking);
                        }
                        loadAndRefreshLeaderboard(getSelectedLeaderboard().getName());
                    }
                }));
    }

    private void enableStartTrackingButtonIfAppropriateRacesSelected() {
        boolean onlyUntrackedRacesPresent = raceColumnTableSelectionModel.getSelectedSet().size() > 0;
        boolean onlyTrackedRacesPresent = raceColumnTableSelectionModel.getSelectedSet().size() > 0;
        boolean onlyRacesWithNonExistentTracker = raceColumnTableSelectionModel.getSelectedSet().size() > 0;
        
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality race : raceColumnTableSelectionModel.getSelectedSet()) {
            if (getTrackingState(race).isForTracking() && isFinished(race)){
                onlyUntrackedRacesPresent = false;
                onlyTrackedRacesPresent = false;
            }
            
            if (! getTrackingState(race).isForTracking() || doesTrackerExist(race) || isFinished(race)) {
                onlyUntrackedRacesPresent = false;
            } else {
                onlyTrackedRacesPresent = false;
            }
            
            if (trackerExists(race)){
                onlyRacesWithNonExistentTracker = false;
            }
        }
        
        if ((!onlyTrackedRacesPresent && !onlyUntrackedRacesPresent)){
            startStopTrackingButton.setEnabled(false);
        }
        
        if (onlyTrackedRacesPresent){
            startStopTrackingButton.setDown(true);
            startStopTrackingButton.setEnabled(true);
        }
        
        if (onlyUntrackedRacesPresent || onlyRacesWithNonExistentTracker){
            startStopTrackingButton.setDown(false);
            startStopTrackingButton.setEnabled(true);
        }
        
    }

    @Override
    protected void leaderboardRaceColumnSelectionChanged() {
        RaceColumnDTOAndFleetDTOWithNameBasedEquality selectedRaceInLeaderboard = getSelectedRaceColumnWithFleet();
        if (selectedRaceInLeaderboard != null) {
            selectTrackedRaceInRaceList();
        } else {
            trackedRacesListComposite.clearSelection();
        }
        enableStartTrackingButtonIfAppropriateRacesSelected();
    }
    
    @Override
    protected void leaderboardSelectionChanged() {
        final StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard();
        regattaHasCompetitors = false;
        if (leaderboardSelectionModel.getSelectedSet().size() == 1 && selectedLeaderboard != null) {
            List<Triple<String, String, String>> raceColumnsAndFleets = new ArrayList<Triple<String, String, String>>();
            for (RaceColumnDTO raceColumn : selectedLeaderboard.getRaceList()) {
                for (FleetDTO fleet : raceColumn.getFleets()) {
                    raceColumnsAndFleets.add(new Triple<String, String, String>(selectedLeaderboard.getName(), raceColumn.getName(), fleet.getName()));
                }
            }
            sailingService.getTrackingTimes(raceColumnsAndFleets,
                    new AsyncCallback<Map<Triple<String, String, String>, Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>>>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error retrieving tracking times: " + caught.getMessage());
                }

                @Override
                public void onSuccess(Map<Triple<String, String, String>, Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog>> result) {
                    raceWithStartAndEndOfTrackingTime = result;
                    raceColumnTable.getDataProvider().getList().clear();
                    for (RaceColumnDTO raceColumn : selectedLeaderboard.getRaceList()) {
                        for (FleetDTO fleet : raceColumn.getFleets()) {
                            RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnDTOAndFleet2 = new RaceColumnDTOAndFleetDTOWithNameBasedEquality(raceColumn, fleet, getSelectedLeaderboard());
                            raceColumnTable.getDataProvider().getList().add(raceColumnDTOAndFleet2);
                        }
                    }
                }
            });
            selectedLeaderBoardPanel.setVisible(true);
            selectedLeaderBoardPanel.setCaptionText("Details of leaderboard '" + selectedLeaderboard.getName() + "'");
            if (!selectedLeaderboard.type.isMetaLeaderboard()) {
                trackedRacesListComposite.setRegattaFilterValue(selectedLeaderboard.regattaName);
                trackedRacesCaptionPanel.setVisible(true);
            }
            sailingService.doesRegattaLogContainCompetitors(((StrippedLeaderboardDTO) leaderboardSelectionModel.getSelectedSet().toArray()[0]).getName(), new RegattaLogCallBack());
        } else {
            selectedLeaderBoardPanel.setVisible(false);
            trackedRacesCaptionPanel.setVisible(false);
        }
    }
    
    public Pair<TimePointSpecificationFoundInLog, TimePointSpecificationFoundInLog> getTrackingTimesFor(RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnDTOAndFleet){
        return raceWithStartAndEndOfTrackingTime.get(new Triple<String, String, String>(getSelectedLeaderboard().getName(), raceColumnDTOAndFleet.getA().getName(), raceColumnDTOAndFleet.getB().getName()));
    }

    
    private class RegattaLogCallBack implements AsyncCallback<Boolean>{
        @Override
        public void onFailure(Throwable caught) {
            regattaHasCompetitors = false;
        }

        @Override
        public void onSuccess(Boolean result) {
            regattaHasCompetitors = true;
        }
    }
    
    private void denoteForRaceLogTracking(final StrippedLeaderboardDTO leaderboard) {
        final ChooseNameDenoteEventDialog dialog = new ChooseNameDenoteEventDialog(stringMessages,leaderboard,
                new DialogCallback<String>() {

                    @Override
                    public void ok(String prefix) {
                        sailingService.denoteForRaceLogTracking(leaderboard.getName(), prefix, new AsyncCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                loadAndRefreshLeaderboard(leaderboard.getName());
                                updateRegattaConfigDesignerModeToByMarks(leaderboard.regattaName);
                                raceColumnTableSelectionModel.clear();
                            }

                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter
                                        .reportError("Could not denote for RaceLog tracking: " + caught.getMessage());
                            }
                        });

                    }

                    @Override
                    public void cancel() {
                    }
                });
        dialog.show();
    }

    private void updateRegattaConfigDesignerModeToByMarks(final String regattaName) {
        final RegattaDTO regatta = getRegattaByName(regattaName);
        if (regatta != null) {
            DeviceConfigurationDTO.RegattaConfigurationDTO configuration = regatta.configuration;
            if (configuration == null) {
                configuration = new DeviceConfigurationDTO.RegattaConfigurationDTO();
                configuration.defaultCourseDesignerMode = CourseDesignerMode.BY_MARKS;
                updateRegattaConfiguration(regatta, configuration);
            } else {
                if (configuration.defaultCourseDesignerMode != CourseDesignerMode.BY_MARKS) {
                    DialogBox dialogBox = createOverrideConfigurationDialog(regatta, configuration);
                    dialogBox.center();
                }
            }
        }
    }

    private DialogBox createOverrideConfigurationDialog(final RegattaDTO regatta,
            final DeviceConfigurationDTO.RegattaConfigurationDTO configuration) {
        final DialogBox dialogBox = new DialogBox(true, true);
        dialogBox.setText(stringMessages.allRacesHaveBeenDenoted());

        VerticalPanel contentPanel = new VerticalPanel();
        contentPanel.add(new HTML(new SafeHtmlBuilder()
                .appendEscapedLines(stringMessages.warningOverrideRegattaConfigurationCourseDesignerToByMarks())
                .toSafeHtml()));

        HorizontalPanel buttonPanel = new HorizontalPanel();
        buttonPanel.setSpacing(5);
        contentPanel.add(buttonPanel);

        Button yesButton = new Button(stringMessages.yes());
        yesButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                configuration.defaultCourseDesignerMode = CourseDesignerMode.BY_MARKS;
                updateRegattaConfiguration(regatta, configuration);
                dialogBox.hide();
            }
        });
        buttonPanel.add(yesButton);

        Button noButton = new Button(stringMessages.no(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialogBox.hide();
            }
        });
        buttonPanel.add(noButton);

        dialogBox.setWidget(contentPanel);
        return dialogBox;
    }

    private void updateRegattaConfiguration(final RegattaDTO regatta,
            DeviceConfigurationDTO.RegattaConfigurationDTO configuration) {
        final RegattaIdentifier regattaIdentifier = new RegattaName(regatta.getName());
        sailingService.updateRegatta(regattaIdentifier, regatta.startDate, regatta.endDate,
                regatta.defaultCourseAreaUuid, configuration, regatta.buoyZoneRadiusInHullLengths,
                regatta.useStartTimeInference, regatta.controlTrackingFromStartAndFinishTimes,
                regatta.registrationLinkSecret, regatta.competitorRegistrationType,
                new MarkedAsyncCallback<Void>(new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(
                                stringMessages.errorUpdatingRegatta(regatta.getName(), caught.getMessage()));
                    }

                    @Override
                    public void onSuccess(Void result) {
                        Notification.notify(stringMessages.notificationRegattaConfigurationUpdatedUsingByMarks(),
                                NotificationType.SUCCESS);
                    }
                }));
    }

    private void denoteForRaceLogTracking(final RaceColumnDTO raceColumn, final FleetDTO fleet) {
        final StrippedLeaderboardDTO leaderboard = getSelectedLeaderboard();
        sailingService.denoteForRaceLogTracking(leaderboard.getName(), raceColumn.getName(), fleet.getName(), new AsyncCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                if (result == true) {
                    loadAndRefreshLeaderboard(leaderboard.getName());
                }
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorLoadingRaceLog(caught.getMessage()));
            }
        });
    }
    
    private void removeDenotation(final RaceColumnDTO raceColumn, final FleetDTO fleet) {
        final StrippedLeaderboardDTO leaderboard = getSelectedLeaderboard();
        sailingService.removeDenotationForRaceLogTracking(leaderboard.getName(), raceColumn.getName(), fleet.getName(), new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadAndRefreshLeaderboard(leaderboard.getName());
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Could not remove denotation: " + caught.getMessage());
            }
        });
    }
    
    private void startTracking(Set<RaceColumnDTOAndFleetDTOWithNameBasedEquality> races,
            boolean trackWind, boolean correctWindByDeclination) {
        final StrippedLeaderboardDTO leaderboard = getSelectedLeaderboard();
        //prompt user if competitor registrations are missing for same races
        String namesOfRacesMissingRegistrations = "";
        if (!regattaHasCompetitors) {
            for (RaceColumnDTOAndFleetDTOWithNameBasedEquality race : races) {
                if (!doCompetitorRegistrationsExist(race)) {
                    namesOfRacesMissingRegistrations += race.getA().getName() + "/" + race.getB().getName() + " ";
                }
            }
        }
        if (! namesOfRacesMissingRegistrations.isEmpty()) {
            boolean proceed = Window.confirm(stringMessages.competitorRegistrationsMissingProceed(
                    namesOfRacesMissingRegistrations));
            if (! proceed) {
                return;
            }
        }
        final List<Triple<String, String, String>> leaderboardRaceColumnFleetNames = new ArrayList<>();
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality race : races) {
            final RaceColumnDTO raceColumn = race.getA();
            final FleetDTO fleet = race.getB();
            leaderboardRaceColumnFleetNames.add(new Triple<>(leaderboard.getName(), raceColumn.getName(), fleet.getName()));
        }
        sailingService.startRaceLogTracking(leaderboardRaceColumnFleetNames, trackWind, correctWindByDeclination,
                new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadAndRefreshLeaderboard(leaderboard.getName());
                trackedRacesListComposite.regattaRefresher.fillRegattas();
            }

            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorStartingTracking(Util.toStringOrNull(leaderboardRaceColumnFleetNames),caught.getMessage()));
            }
        });
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
                            Notification.notify(stringMessages.failedToSetNewStartTime(), NotificationType.ERROR);
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
    
    private void setEndTime(RaceColumnDTO raceColumnDTO, FleetDTO fleetDTO) {
        new SetFinishingAndFinishedTimeDialog(sailingService, errorReporter, getSelectedLeaderboardName(), raceColumnDTO.getName(),
                fleetDTO.getName(), stringMessages, new DialogCallback<RaceLogSetFinishingAndFinishTimeDTO>() {
                    @Override
                    public void ok(RaceLogSetFinishingAndFinishTimeDTO editedObject) {
                        sailingService.setFinishingAndEndTime(editedObject, new AsyncCallback<Pair<Boolean, Boolean>>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(caught.getMessage());
                            }

                            @Override
                            public void onSuccess(Pair<Boolean, Boolean> result) {
                                if (!result.getA() || !result.getB()) {
                                    Notification.notify(stringMessages.failedToSetNewFinishingAndFinishTime(), NotificationType.ERROR);
                                } else {
                                    trackedRacesListComposite.regattaRefresher.fillRegattas();
                                }
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                    }
                }).show();
    }
    
    private void refreshTrackingActionButtons(){
        leaderboardSelectionChanged();
    }
    
    private void showSetTrackingTimesDialog(RaceColumnDTO raceColumn, FleetDTO fleet) {
        new SetTrackingTimesDialog(sailingService, errorReporter, getSelectedLeaderboardName(), raceColumn.getName(),
                fleet.getName(), stringMessages, new DataEntryDialog.DialogCallback<RaceLogSetTrackingTimesDTO>() {
                    @Override
                    public void ok(RaceLogSetTrackingTimesDTO editedObject) {
                        sailingService.setTrackingTimes(editedObject, new AsyncCallback<Void>(){
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error while setting tracking times: " + caught.getMessage());
                                refreshTrackingActionButtons();
                            }

                            @Override
                            public void onSuccess(Void result) {
                                refreshTrackingActionButtons();
                            }
                        });
                    }

                    @Override
                    public void cancel() {
                        //toggle buttons in dialog lead to a change although dialog is canceled --> reload tracking times
                        refreshTrackingActionButtons();
                    }
                }).show();
    }

    private void showCompetitorToBoatMappings(final RaceColumnDTO raceColumnDTO, final FleetDTO fleetDTO) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String raceColumnName = raceColumnDTO.getName();
        final String fleetName = fleetDTO.getName();
        final String raceName = LeaderboardNameConstants.DEFAULT_FLEET_NAME.equals(fleetName) ? raceColumnName : raceColumnName + ", " + fleetName;
        ShowCompetitorToBoatMappingsDialog dialog = new ShowCompetitorToBoatMappingsDialog(sailingService, 
                stringMessages, errorReporter, selectedLeaderboardName, raceColumnName, fleetName, 
                raceName);
        dialog.center();
    }

    private String getLocaleInfo() {
        return LocaleInfo.getCurrentLocale().getLocaleName();
    }
    
    private void openChooseEventDialogAndSendMails(final String leaderboardName) {
        new InviteBuoyTenderDialog(stringMessages, sailingService, leaderboardName, errorReporter, new DialogCallback<Triple<EventDTO, String, String>>() {
            @Override
            public void ok(Triple<EventDTO, String, String> result) {
                sailingService.inviteBuoyTenderViaEmail(result.getB(), result.getA(), leaderboardName, result.getC(),
                        null,
                        stringMessages.playStoreBuoyPingerApp(),
                        getLocaleInfo(), new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                Notification.notify(stringMessages.sendingMailsFailed() + caught.getMessage(), NotificationType.ERROR);
                            }

                            @Override
                            public void onSuccess(Void result) {
                                Notification.notify(stringMessages.sendingMailsSuccessful(), NotificationType.SUCCESS);
                            }
                        });
            }

            @Override
            public void cancel() {
                
            }
        }).show();
    }
}
