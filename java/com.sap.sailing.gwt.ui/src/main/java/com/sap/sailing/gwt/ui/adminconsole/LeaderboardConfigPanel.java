package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Panel;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.dto.AbstractLeaderboardDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.FleetDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.adminconsole.DisablableCheckboxCell.IsEnabled;
import com.sap.sailing.gwt.ui.client.EntryPointLinkFactory;
import com.sap.sailing.gwt.ui.client.LeaderboardsDisplayer;
import com.sap.sailing.gwt.ui.client.LeaderboardsRefresher;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.RegattasDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.controls.SelectionCheckboxColumn;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.EventDTO;
import com.sap.sailing.gwt.ui.shared.RaceLogSetStartTimeAndProcedureDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.URLEncoder;
import com.sap.sse.gwt.client.async.MarkedAsyncCallback;
import com.sap.sse.gwt.client.dialog.DataEntryDialog.DialogCallback;

public class LeaderboardConfigPanel extends AbstractLeaderboardConfigPanel implements SelectedLeaderboardProvider, RegattasDisplayer, RaceSelectionChangeListener,
TrackedRaceChangedListener, LeaderboardsDisplayer {
    private final AnchorTemplates ANCHORTEMPLATE = GWT.create(AnchorTemplates.class);
    
    private final boolean showRaceDetails;

    private Button leaderboardRemoveButton;

    private Button addRaceColumnsButton;

    private Button columnMoveUpButton;
    private Button columnMoveDownButton;

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

    public LeaderboardConfigPanel(final SailingServiceAsync sailingService, RegattaRefresher regattaRefresher,
            final ErrorReporter errorReporter, StringMessages theStringConstants, final boolean showRaceDetails,
            LeaderboardsRefresher leaderboardsRefresher) {
        super(sailingService, regattaRefresher, leaderboardsRefresher, errorReporter, theStringConstants,
                /* multi-selection */ false);
        this.showRaceDetails = showRaceDetails;
        leaderboardTable.ensureDebugId("LeaderboardsCellTable");
    }
    
    @Override
    protected void addLeaderboardConfigControls(Panel configPanel) {
        filterLeaderboardPanel.getTextBox().ensureDebugId("LeaderboardsFilterTextBox");
        
        leaderboardRemoveButton = new Button(stringMessages.remove());
        leaderboardRemoveButton.ensureDebugId("LeaderboardsRemoveButton");
        leaderboardRemoveButton.setEnabled(false);
        leaderboardRemoveButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (Window.confirm(stringMessages.doYouReallyWantToRemoveLeaderboards())) {
                    removeLeaderboards(leaderboardSelectionModel.getSelectedSet());
                }
            }
        });
        configPanel.add(leaderboardRemoveButton);
    }
    
    @Override
    protected void addColumnsToLeaderboardTableAndSetSelectionModel(final CellTable<StrippedLeaderboardDTO> leaderboardTable, AdminConsoleTableResources tableResources) {
        ListHandler<StrippedLeaderboardDTO> leaderboardColumnListHandler = new ListHandler<StrippedLeaderboardDTO>(
                leaderboardList.getList());
        SelectionCheckboxColumn<StrippedLeaderboardDTO> selectionCheckboxColumn = createSortableSelectionCheckboxColumn(
                leaderboardTable, tableResources, leaderboardColumnListHandler);
        AnchorCell anchorCell = new AnchorCell();
        Column<StrippedLeaderboardDTO, SafeHtml> linkColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(anchorCell) {
            @Override
            public SafeHtml getValue(StrippedLeaderboardDTO object) {
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLEncoder.encode("/gwt/Leaderboard.html?name=" + object.name
                        + (showRaceDetails ? "&showRaceDetails=true" : "")
                        + (object.displayName != null ? "&displayName="+object.displayName : "")
                        + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                return ANCHORTEMPLATE.cell(link, object.name);
            }

        };
        linkColumn.setSortable(true);
        leaderboardColumnListHandler.setComparator(linkColumn, new Comparator<StrippedLeaderboardDTO>() {

            @Override
            public int compare(StrippedLeaderboardDTO o1, StrippedLeaderboardDTO o2) {
                boolean ascending = isSortedAscending();
                if (o1.name.equals(o2.name)) {
                    return 0;
                }
                int val = -1;
                val = (o1 != null && o2 != null && ascending) ? (o1.name.compareTo(o2.name)) : -(o2.name
                        .compareTo(o1.name));

                return val;
            }

            private boolean isSortedAscending() {
                ColumnSortList sortList = leaderboardTable.getColumnSortList();
                return sortList.size() > 0 & sortList.get(0).isAscending();
            }
        });

        TextColumn<StrippedLeaderboardDTO> leaderboardDisplayNameColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                return leaderboard.getDisplayName() != null ? leaderboard.getDisplayName() : "";
            }
        };

        TextColumn<StrippedLeaderboardDTO> discardingOptionsColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                String result = "";
                if (leaderboard.discardThresholds != null) {
                    for (int discardThreshold : leaderboard.discardThresholds) {
                        result += discardThreshold + " ";
                    }
                }
                return result;
            }
        };

        TextColumn<StrippedLeaderboardDTO> leaderboardTypeColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                String result = leaderboard.type.isRegattaLeaderboard() ? "Regatta" : "Flexible";
                if (leaderboard.type.isMetaLeaderboard()) {
                    result += " , Meta";
                }
                return result;
            }
        };

        TextColumn<StrippedLeaderboardDTO> scoringSystemColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                return leaderboard.scoringScheme == null ? "" : ScoringSchemeTypeFormatter.format(leaderboard.scoringScheme, stringMessages);
            }
        };

        TextColumn<StrippedLeaderboardDTO> courseAreaColumn = new TextColumn<StrippedLeaderboardDTO>() {
            @Override
            public String getValue(StrippedLeaderboardDTO leaderboard) {
                return leaderboard.defaultCourseAreaId == null ? "" : leaderboard.defaultCourseAreaName;
            }
        };

        ImagesBarColumn<StrippedLeaderboardDTO, LeaderboardConfigImagesBarCell> leaderboardActionColumn = new ImagesBarColumn<StrippedLeaderboardDTO, LeaderboardConfigImagesBarCell>(
                new LeaderboardConfigImagesBarCell(stringMessages));
        leaderboardActionColumn.setFieldUpdater(new FieldUpdater<StrippedLeaderboardDTO, String>() {
            @Override
            public void update(int index, StrippedLeaderboardDTO leaderboardDTO, String value) {
                if (LeaderboardConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.doYouReallyWantToRemoveLeaderboard(leaderboardDTO.name))) {
                        removeLeaderboard(leaderboardDTO);
                    }
                } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    final String oldLeaderboardName = leaderboardDTO.name;
                    List<StrippedLeaderboardDTO> otherExistingLeaderboard = new ArrayList<StrippedLeaderboardDTO>();
                    otherExistingLeaderboard.addAll(availableLeaderboardList);
                    otherExistingLeaderboard.remove(leaderboardDTO);
                    if (leaderboardDTO.type.isMetaLeaderboard()) {
                        Window.alert(stringMessages.metaLeaderboardCannotBeChanged());
                    } else {
                        if (leaderboardDTO.type.isRegattaLeaderboard()) {
                            LeaderboardDescriptor descriptor = new LeaderboardDescriptor(leaderboardDTO.name,
                                    leaderboardDTO.displayName, /* scoring scheme provided by regatta */ null,
                                    leaderboardDTO.discardThresholds, leaderboardDTO.regattaName,
                                    leaderboardDTO.defaultCourseAreaId);
                            AbstractLeaderboardDialog dialog = new RegattaLeaderboardEditDialog(Collections
                                    .unmodifiableCollection(otherExistingLeaderboard), Collections.unmodifiableCollection(allRegattas),
                                    descriptor, stringMessages, errorReporter,
                                    new DialogCallback<LeaderboardDescriptor>() {
                                @Override
                                public void cancel() {
                                }

                                @Override
                                public void ok(LeaderboardDescriptor result) {
                                    updateLeaderboard(oldLeaderboardName, result);
                                }
                            });
                            dialog.show();
                        } else {
                            LeaderboardDescriptor descriptor = new LeaderboardDescriptor(leaderboardDTO.name, leaderboardDTO.displayName, leaderboardDTO.scoringScheme, leaderboardDTO.discardThresholds, leaderboardDTO.defaultCourseAreaId);
                            openUpdateFlexibleLeaderboardDialog(leaderboardDTO, otherExistingLeaderboard, leaderboardDTO.name, descriptor);
                        }
                    }
                } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT_SCORES.equals(value)) {
                    String debugParam = Window.Location.getParameter("gwt.codesvr");
                    Window.open("/gwt/LeaderboardEditing.html?name=" + leaderboardDTO.name
                            + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""), "_blank", null);
                } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT_COMPETITORS.equals(value)) {
                    EditCompetitorsDialog editCompetitorsDialog = new EditCompetitorsDialog(sailingService, leaderboardDTO.name, stringMessages, 
                            errorReporter, new DialogCallback<List<CompetitorDTO>>() {
                        @Override
                        public void cancel() {
                        }

                        @Override
                        public void ok(final List<CompetitorDTO> result) {
                        }
                    });
                    editCompetitorsDialog.show();

                } else if (LeaderboardConfigImagesBarCell.ACTION_CONFIGURE_URL.equals(value)) {
                    openLeaderboardUrlConfigDialog(leaderboardDTO, stringMessages);
                } else if (LeaderboardConfigImagesBarCell.ACTION_EXPORT_XML.equals(value)) {
                    Window.open("/export/xml?domain=leaderboard&name=" + leaderboardDTO.name, "", null);
                } else if (LeaderboardConfigImagesBarCell.ACTION_OPEN_COACH_DASHBOARD.equals(value)) {
                    Map<String, String> dashboardURLParameters = new HashMap<String, String>();
                    dashboardURLParameters.put("leaderboardName", leaderboardDTO.name);
                    Window.open(EntryPointLinkFactory.createDashboardLink(dashboardURLParameters), "", null);
                }
            }
        });
        leaderboardTable.addColumn(selectionCheckboxColumn, selectionCheckboxColumn.getHeader());
        leaderboardTable.addColumn(linkColumn, stringMessages.name());
        leaderboardTable.addColumn(leaderboardDisplayNameColumn, stringMessages.displayName());
        leaderboardTable.addColumn(discardingOptionsColumn, stringMessages.discarding());
        leaderboardTable.addColumn(leaderboardTypeColumn, stringMessages.type());
        leaderboardTable.addColumn(scoringSystemColumn, stringMessages.scoringSystem());
        leaderboardTable.addColumn(courseAreaColumn, stringMessages.courseArea());
        leaderboardTable.addColumn(leaderboardActionColumn, stringMessages.actions());
        leaderboardTable.addColumnSortHandler(leaderboardColumnListHandler);
        leaderboardTable.setSelectionModel(selectionCheckboxColumn.getSelectionModel(), selectionCheckboxColumn.getSelectionManager());
    }

    protected void addLeaderboardCreateControls(Panel createPanel) {
        Button createFlexibleLeaderboardBtn = new Button(stringMessages.createFlexibleLeaderboard() + "...");
        createFlexibleLeaderboardBtn.ensureDebugId("CreateFlexibleLeaderboardButton");
        createPanel.add(createFlexibleLeaderboardBtn);
        createFlexibleLeaderboardBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                createFlexibleLeaderboard();
            }
        });

        Button createRegattaLeaderboardBtn = new Button(stringMessages.createRegattaLeaderboard() + "...");
        createRegattaLeaderboardBtn.ensureDebugId("CreateRegattaLeaderboardButton");
        createPanel.add(createRegattaLeaderboardBtn);
        createRegattaLeaderboardBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                createRegattaLeaderboard();
            }
        });
    }
    
    @Override
    protected void addColumnsToRacesTable(CellTable<RaceColumnDTOAndFleetDTOWithNameBasedEquality> racesTable) {
        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> explicitFactorColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality object) {
                return object.getA().getExplicitFactor() == null ? "" : object.getA().getExplicitFactor().toString();
            }
        };

        Column<RaceColumnDTOAndFleetDTOWithNameBasedEquality, Boolean> isMedalRaceCheckboxColumn = new Column<RaceColumnDTOAndFleetDTOWithNameBasedEquality, Boolean>(
                new DisablableCheckboxCell(new IsEnabled() {
                    @Override
                    public boolean isEnabled() {
                        return getSelectedLeaderboard() != null && !getSelectedLeaderboard().type.isRegattaLeaderboard();
                    }
                })) {
            @Override
            public Boolean getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality race) {
                return race.getA().isMedalRace();
            }
        };
        isMedalRaceCheckboxColumn.setFieldUpdater(new FieldUpdater<RaceColumnDTOAndFleetDTOWithNameBasedEquality, Boolean>() {
            @Override
            public void update(int index, RaceColumnDTOAndFleetDTOWithNameBasedEquality object, Boolean value) {
                setIsMedalRace(getSelectedLeaderboard().name, object.getA(), value);
            }
        });
        isMedalRaceCheckboxColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality> isLinkedRaceColumn = new TextColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality>() {
            @Override
            public String getValue(RaceColumnDTOAndFleetDTOWithNameBasedEquality raceColumnAndFleetName) {
                boolean isTrackedRace = raceColumnAndFleetName.getA().isTrackedRace(raceColumnAndFleetName.getB());
                return isTrackedRace ? stringMessages.yes() : stringMessages.no();
            }
        };

        ImagesBarColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality, LeaderboardRaceConfigImagesBarCell> raceActionColumn =
                new ImagesBarColumn<RaceColumnDTOAndFleetDTOWithNameBasedEquality, LeaderboardRaceConfigImagesBarCell>(
                        new LeaderboardRaceConfigImagesBarCell(this, stringMessages));
        raceActionColumn.setFieldUpdater(new FieldUpdater<RaceColumnDTOAndFleetDTOWithNameBasedEquality, String>() {
            @Override
            public void update(int index, RaceColumnDTOAndFleetDTOWithNameBasedEquality object, String value) {
                if (LeaderboardRaceConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.reallyRemoveRace(object.getA().getRaceColumnName()))) {
                        removeRaceColumn(object.getA());
                    }
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    editRaceColumnOfLeaderboard(object);
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_UNLINK.equals(value)) {
                    unlinkRaceColumnFromTrackedRace(object.getA().getRaceColumnName(), object.getB());
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_REFRESH_RACELOG.equals(value)) {
                    refreshRaceLog(object.getA(), object.getB(), true);
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_SET_STARTTIME.equals(value)) {
                    setStartTime(object.getA(), object.getB());
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_SHOW_RACELOG.equals(value)) {
                    showRaceLog(object.getA(), object.getB());
                }
            }
        });
        
        racesTable.addColumn(isMedalRaceCheckboxColumn, stringMessages.medalRace());
        racesTable.addColumn(isLinkedRaceColumn, stringMessages.islinked());
        racesTable.addColumn(explicitFactorColumn, stringMessages.factor());
        racesTable.addColumn(raceActionColumn, stringMessages.actions());
        
        racesTable.ensureDebugId("RacesCellTable");
    }
    
    @Override
    protected void addSelectedLeaderboardRacesControls(Panel racesPanel) {
        addRaceColumnsButton = new Button(stringMessages.actionAddRaces() + "...");
        addRaceColumnsButton.ensureDebugId("AddRacesButton");
        racesPanel.add(addRaceColumnsButton);
        addRaceColumnsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (getSelectedLeaderboard().type.isRegattaLeaderboard()) {
                    Window.alert(stringMessages.cannotAddRacesToRegattaLeaderboardButOnlyToRegatta());
                } else {
                    addRaceColumnsToLeaderboard();
                }
            }
        });
        racesPanel.add(addRaceColumnsButton);

        columnMoveUpButton = new Button(stringMessages.columnMoveUp());
        racesPanel.add(columnMoveUpButton);
        columnMoveUpButton.ensureDebugId("MoveRaceUpButton");
        columnMoveUpButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelectedRaceColumnUp();
            }
        });
        racesPanel.add(columnMoveUpButton);

        columnMoveDownButton = new Button(stringMessages.columnMoveDown());
        racesPanel.add(columnMoveDownButton);
        columnMoveDownButton.ensureDebugId("MoveRaceDownButton");
        columnMoveDownButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelectedRaceColumnDown();
            }
        });
        racesPanel.add(columnMoveDownButton);
    }

    protected void openUpdateFlexibleLeaderboardDialog(final StrippedLeaderboardDTO leaderboardDTO, final List<StrippedLeaderboardDTO> otherExistingLeaderboard,
            final String oldLeaderboardName, final LeaderboardDescriptor descriptor) {
        sailingService.getEvents(new MarkedAsyncCallback<List<EventDTO>>(
                new AsyncCallback<List<EventDTO>>() {
                    @Override
                    public void onSuccess(List<EventDTO> result) {
                        openUpdateFlexibleLeaderboardDialog(leaderboardDTO, otherExistingLeaderboard, oldLeaderboardName,
                                descriptor, result);
                    }
        
                    @Override
                    public void onFailure(Throwable caught) {
                        openUpdateFlexibleLeaderboardDialog(leaderboardDTO, otherExistingLeaderboard, oldLeaderboardName,
                                descriptor, new ArrayList<EventDTO>());
                    }
                }));
    }

    protected void openUpdateFlexibleLeaderboardDialog(StrippedLeaderboardDTO leaderboardDTO, List<StrippedLeaderboardDTO> otherExistingLeaderboard, 
            final String oldLeaderboardName, LeaderboardDescriptor descriptor, List<EventDTO> existingEvents) {
        FlexibleLeaderboardEditDialog dialog = new FlexibleLeaderboardEditDialog(
                Collections.unmodifiableCollection(otherExistingLeaderboard), descriptor, stringMessages,
                Collections.unmodifiableList(existingEvents), errorReporter,
                new DialogCallback<LeaderboardDescriptor>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(LeaderboardDescriptor result) {
                        updateLeaderboard(oldLeaderboardName, result);
                    }
                });
        dialog.show();
    }

    /**
     * Allow the user to combine the various URL parameters that exist for the {@link LeaderboardEntryPoint} and obtain the
     * resulting URL in a link. The link's reference target is updated dynamically as the user adjusts the settings. Therefore,
     * the link can be clicked, bookmarked or copied to the clipboard at any time. The OK / Cancel actions for the dialog shown
     * are no-ops.
     */
    private void openLeaderboardUrlConfigDialog(AbstractLeaderboardDTO leaderboard, StringMessages stringMessages) {
        LeaderboardEntryPoint.getUrlConfigurationDialog(leaderboard, stringMessages).show();
    }

    private void setStartTime(RaceColumnDTO raceColumnDTO, FleetDTO fleetDTO) {
        new SetStartTimeDialog(sailingService, errorReporter, getSelectedLeaderboardName(), raceColumnDTO.getName(), 
                fleetDTO.getName(), stringMessages, new DialogCallback<RaceLogSetStartTimeAndProcedureDTO>() {
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
                        }
                    }
                });
            }

            @Override
            public void cancel() { }
        }).show();
    }

    private void removeRaceColumn(final RaceColumnDTO raceColumnDTO) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String raceColumnString = raceColumnDTO.getRaceColumnName();
        sailingService.removeLeaderboardColumn(getSelectedLeaderboardName(), raceColumnString,
                new MarkedAsyncCallback<Void>(
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable t) {
                                errorReporter.reportError("Error trying to remove leaderboard race column " + raceColumnDTO
                                        + " in leaderboard " + getSelectedLeaderboardName() + ": " + t.getMessage());
                            }
                
                            @Override
                            public void onSuccess(Void arg0) {
                                loadAndRefreshLeaderboard(selectedLeaderboardName, /* raceColumnNameToSelect */ null);
                            }
                        }));
    }

    /**
     * The selected row is potentially only one of several fleet-based rows of the same RaceColumn. In this case,
     * move all fleet-based rows of the same RaceColumn down.
     */
    private void moveSelectedRaceColumnDown() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String selectedRaceColumnName = getSelectedRaceColumnWithFleet().getA().getRaceColumnName();
        sailingService.moveLeaderboardColumnDown(getSelectedLeaderboardName(), selectedRaceColumnName,
                new MarkedAsyncCallback<Void>(
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to move leaderboard race column "
                                        + selectedRaceColumnName + " in leaderboard " + getSelectedLeaderboardName()
                                        + " down: " + caught.getMessage());
                            }
                
                            @Override
                            public void onSuccess(Void result) {
                                loadAndRefreshLeaderboard(selectedLeaderboardName, selectedRaceColumnName);
                            }
                        }));
    }

    /**
     * The selected row is potentially only one of several fleet-based rows of the same RaceColumn. In this case,
     * move all fleet-based rows of the same RaceColumn down.
     */
    private void moveSelectedRaceColumnUp() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String selectedRaceColumnName = getSelectedRaceColumnWithFleet().getA().getRaceColumnName();
        sailingService.moveLeaderboardColumnUp(getSelectedLeaderboardName(), selectedRaceColumnName,
                new MarkedAsyncCallback<Void>(
                        new AsyncCallback<Void>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to move leaderboard race column "
                                        + selectedRaceColumnName + " in leaderboard " + getSelectedLeaderboardName() + " up: "
                                        + caught.getMessage());
                            }
                
                            @Override
                            public void onSuccess(Void result) {
                                loadAndRefreshLeaderboard(selectedLeaderboardName, selectedRaceColumnName);
                            }
                        }));
    }

    @Override
    protected void leaderboardRaceColumnSelectionChanged() {
        selectedRaceInLeaderboard = getSelectedRaceColumnWithFleet();
        if (selectedRaceInLeaderboard != null) {
            columnMoveUpButton.setEnabled(true);
            columnMoveDownButton.setEnabled(true);
            selectTrackedRaceInRaceList();
        } else {
            columnMoveUpButton.setEnabled(false);
            columnMoveDownButton.setEnabled(false);
            trackedRacesListComposite.clearSelection();
        }
    }

    private void setIsMedalRace(String leaderboardName, final RaceColumnDTO raceInLeaderboard,
            final boolean isMedalRace) {
        sailingService.updateIsMedalRace(leaderboardName, raceInLeaderboard.getRaceColumnName(), isMedalRace,
                new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError(stringMessages.errorUpdatingIsMedalRace(caught.getMessage()));
            }

            @Override
            public void onSuccess(Void result) {
                getSelectedLeaderboard().setIsMedalRace(raceInLeaderboard.getRaceColumnName(), isMedalRace);
            }
        });
    }

    private void addRaceColumnsToLeaderboard() {
        final String leaderboardName = getSelectedLeaderboardName();
        final List<RaceColumnDTO> existingRaceColumns = new ArrayList<RaceColumnDTO>();
        for (RaceColumnDTOAndFleetDTOWithNameBasedEquality pair : raceColumnTable.getDataProvider().getList()) {
            existingRaceColumns.add(pair.getA());
        }
        final RaceColumnsInLeaderboardDialog raceDialog = new RaceColumnsInLeaderboardDialog(existingRaceColumns,
                stringMessages, new DialogCallback<List<RaceColumnDTO>>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(final List<RaceColumnDTO> result) {
                updateRaceColumnsOfLeaderboard(leaderboardName, existingRaceColumns, result);
            }
        });
        raceDialog.ensureDebugId("RaceColumnsInLeaderboardDialog");
        raceDialog.show();
    }

    private void updateRaceColumnsOfLeaderboard(final String leaderboardName, List<RaceColumnDTO> existingRaceColumns, List<RaceColumnDTO> newRaceColumns) {
        final List<Util.Pair<String, Boolean>> raceColumnsToAdd = new ArrayList<Util.Pair<String, Boolean>>();

        for (RaceColumnDTO newRaceColumn : newRaceColumns) {
            if (!existingRaceColumns.contains(newRaceColumn)) {
                raceColumnsToAdd.add(new Util.Pair<String, Boolean>(newRaceColumn.getName(), newRaceColumn.isMedalRace()));
            }
        }

        sailingService.addColumnsToLeaderboard(leaderboardName, raceColumnsToAdd, new MarkedAsyncCallback<Void>(
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to add race columns to leaderboard " + leaderboardName
                                + ": " + caught.getMessage());
                    }
        
                    @Override
                    public void onSuccess(Void v) {
                        loadAndRefreshLeaderboard(leaderboardName, /* nameOfRaceColumnToSelect */ null);
                    }
                }));
    }
    
    @Override
    protected void leaderboardSelectionChanged() {
        // make sure that clearing the selection doesn't cause an unlinking of the selected tracked race
        raceSelectionProvider.removeRaceSelectionChangeListener(this);
        trackedRacesListComposite.clearSelection();
        // add listener again using a scheduled command which is executed when the browser's event loop re-gains
        // control; we assume that at that point in time the selection updates have already been performed
        Scheduler.get().scheduleFinally(new ScheduledCommand() {
            @Override
            public void execute() {
                raceSelectionProvider.addRaceSelectionChangeListener(LeaderboardConfigPanel.this);
            }
        });
        leaderboardRemoveButton.setEnabled(!leaderboardSelectionModel.getSelectedSet().isEmpty());
        StrippedLeaderboardDTO selectedLeaderboard = getSelectedLeaderboard();
        if (leaderboardSelectionModel.getSelectedSet().size() == 1 && selectedLeaderboard != null) {
            raceColumnTable.getDataProvider().getList().clear();
            for (RaceColumnDTO raceColumn : selectedLeaderboard.getRaceList()) {
                for (FleetDTO fleet : raceColumn.getFleets()) {
                    raceColumnTable.getDataProvider().getList().add(new RaceColumnDTOAndFleetDTOWithNameBasedEquality(raceColumn, fleet));
                }
            }
            selectedLeaderBoardPanel.setVisible(true);
            selectedLeaderBoardPanel.setCaptionText("Details of leaderboard '" + selectedLeaderboard.name + "'");
            if (!selectedLeaderboard.type.isMetaLeaderboard()) {
                trackedRacesCaptionPanel.setVisible(true);
            }
            addRaceColumnsButton.setVisible(!selectedLeaderboard.type.isRegattaLeaderboard());
            columnMoveUpButton.setVisible(!selectedLeaderboard.type.isRegattaLeaderboard());
            columnMoveDownButton.setVisible(!selectedLeaderboard.type.isRegattaLeaderboard());
        } else {
            selectedLeaderBoardPanel.setVisible(false);
            trackedRacesCaptionPanel.setVisible(false);
            selectedRaceInLeaderboard = null;
        }
    }

    private void createFlexibleLeaderboard() {
        sailingService.getEvents(new MarkedAsyncCallback<List<EventDTO>>(
                new AsyncCallback<List<EventDTO>>() {
                    @Override
                    public void onSuccess(List<EventDTO> result) {
                        createFlexibleLeaderboard(result);
                    }
        
                    @Override
                    public void onFailure(Throwable caught) {
                        createFlexibleLeaderboard(new ArrayList<EventDTO>());
                    }
                }));
    }

    private void createFlexibleLeaderboard(List<EventDTO> existingEvents) {

        AbstractLeaderboardDialog dialog = new FlexibleLeaderboardCreateDialog(Collections.unmodifiableCollection(availableLeaderboardList),
                stringMessages, Collections.unmodifiableCollection(existingEvents), errorReporter, new DialogCallback<LeaderboardDescriptor>() {
            @Override
            public void cancel() {
            }
            
            @Override
            public void ok(final LeaderboardDescriptor newLeaderboard) {
                sailingService.createFlexibleLeaderboard(newLeaderboard.getName(), newLeaderboard.getDisplayName(),
                        newLeaderboard.getDiscardThresholds(), newLeaderboard.getScoringScheme(), newLeaderboard.getCourseAreaId(),
                        new MarkedAsyncCallback<StrippedLeaderboardDTO>(
                                new AsyncCallback<StrippedLeaderboardDTO>() {
                                    @Override
                                    public void onFailure(Throwable t) {
                                        errorReporter.reportError("Error trying to create the new flexible leaderboard " + newLeaderboard.getName()
                                                + ": " + t.getMessage());
                                    }
                                    
                                    @Override
                                    public void onSuccess(StrippedLeaderboardDTO result) {
                                        addLeaderboard(result);
                                    }
                                }));
            }
        });
        dialog.ensureDebugId("FlexibleLeaderboardCreateDialog");
        dialog.show();
    }

    private void createRegattaLeaderboard() {
        RegattaLeaderboardCreateDialog dialog = new RegattaLeaderboardCreateDialog(Collections.unmodifiableCollection(availableLeaderboardList),
                Collections.unmodifiableCollection(allRegattas), stringMessages, errorReporter, new DialogCallback<LeaderboardDescriptor>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(final LeaderboardDescriptor newLeaderboard) {
                RegattaIdentifier regattaIdentifier = new RegattaName(newLeaderboard.getRegattaName());
                sailingService.createRegattaLeaderboard(regattaIdentifier, newLeaderboard.getDisplayName(), newLeaderboard.getDiscardThresholds(),
                        new AsyncCallback<StrippedLeaderboardDTO>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to create the new regatta leaderboard " + newLeaderboard.getName()
                                + ": " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(StrippedLeaderboardDTO result) {
                        addLeaderboard(result);
                    }
                });
            }
        });
        dialog.ensureDebugId("RegattaLeaderboardCreateDialog");
        dialog.show();
    }

    private void addLeaderboard(StrippedLeaderboardDTO result) {
        leaderboardList.getList().add(result);
        availableLeaderboardList.add(result);
        leaderboardSelectionModel.clear();
        leaderboardSelectionModel.setSelected(result, true);
    }

    private void updateLeaderboard(final String oldLeaderboardName, final LeaderboardDescriptor leaderboardToUpdate) {
        sailingService.updateLeaderboard(oldLeaderboardName, leaderboardToUpdate.getName(), leaderboardToUpdate.getDisplayName(),
                leaderboardToUpdate.getDiscardThresholds(), leaderboardToUpdate.getCourseAreaId(), new AsyncCallback<StrippedLeaderboardDTO>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to update leaderboard " + oldLeaderboardName + ": "
                        + t.getMessage());
            }

            @Override
            public void onSuccess(StrippedLeaderboardDTO updatedLeaderboard) {
                int indexOfLeaderboard = 0;
                for (int i = 0; i < leaderboardList.getList().size(); i++) {
                    StrippedLeaderboardDTO dao = leaderboardList.getList().get(i);
                    if (dao.name.equals(oldLeaderboardName)) {
                        indexOfLeaderboard = i;
                        break;
                    }
                }
                leaderboardList.getList().set(indexOfLeaderboard, updatedLeaderboard);
                leaderboardList.refresh();
            }
        });
    }

    private void removeLeaderboards(final Collection<StrippedLeaderboardDTO> leaderboards) {
        if (!leaderboards.isEmpty()) {
            Set<String> leaderboardNames = new HashSet<String>();
            for (StrippedLeaderboardDTO leaderboard : leaderboards) {
                leaderboardNames.add(leaderboard.name);
            }
            sailingService.removeLeaderboards(leaderboardNames, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to remove the leaderboards:" + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    for (StrippedLeaderboardDTO leaderboard : leaderboards) {
                        removeLeaderboardFromTable(leaderboard);
                    }
                    getLeaderboardsRefresher().updateLeaderboards(availableLeaderboardList, LeaderboardConfigPanel.this);
                }
            });
        }
    }

    private void removeLeaderboard(final StrippedLeaderboardDTO leaderBoard) {
        sailingService.removeLeaderboard(leaderBoard.name, new MarkedAsyncCallback<Void>(
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to remove leaderboard " + leaderBoard.name + ": "
                                + caught.getMessage());
                    }
        
                    @Override
                    public void onSuccess(Void result) {
                        removeLeaderboardFromTable(leaderBoard);
                        getLeaderboardsRefresher().updateLeaderboards(availableLeaderboardList, LeaderboardConfigPanel.this);
                    }
                }));
    }

    private void removeLeaderboardFromTable(final StrippedLeaderboardDTO leaderBoard) {
        leaderboardList.getList().remove(leaderBoard);
        availableLeaderboardList.remove(leaderBoard);
        leaderboardSelectionModel.setSelected(leaderBoard, false);
    }
}