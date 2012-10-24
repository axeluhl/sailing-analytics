package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.RegattaIdentifier;
import com.sap.sailing.domain.common.RegattaName;
import com.sap.sailing.domain.common.RegattaNameAndRaceName;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.DataEntryDialog.DialogCallback;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.ParallelExecutionCallback;
import com.sap.sailing.gwt.ui.client.ParallelExecutionHolder;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLFactory;
import com.sap.sailing.gwt.ui.leaderboard.LeaderboardEntryPoint;
import com.sap.sailing.gwt.ui.leaderboard.ScoringSchemeTypeFormatter;
import com.sap.sailing.gwt.ui.shared.FleetDTO;
import com.sap.sailing.gwt.ui.shared.RaceColumnDTO;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.StrippedLeaderboardDTO;

public class LeaderboardConfigPanel extends FormPanel implements RegattaDisplayer, RaceSelectionChangeListener,
        TrackedRaceChangedListener {

    private final TrackedRacesListComposite trackedRacesListComposite;

    private final StringMessages stringMessages;

    private final SailingServiceAsync sailingService;

    private final ListDataProvider<StrippedLeaderboardDTO> leaderboardList;

    private final ListDataProvider<Pair<RaceColumnDTO, FleetDTO>> raceColumnAndFleetList;

    private final ErrorReporter errorReporter;

    private final CellTable<StrippedLeaderboardDTO> leaderboardTable;

    private final CellTable<Pair<RaceColumnDTO, FleetDTO>> raceColumnTable;

    private StrippedLeaderboardDTO selectedLeaderboard;

    private Pair<RaceColumnDTO, FleetDTO> selectedRaceInLeaderboard;

    private final Button addRaceColumnsButton;

    private final Button columnMoveUpButton;
    private final Button columnMoveDownButton;

    private final CaptionPanel selectedLeaderBoardPanel;
    private final CaptionPanel trackedRacesCaptionPanel;
    private final List<RegattaDTO> allRegattas;

    private TextBox filterLeaderboardTextbox;

    final SingleSelectionModel<Pair<RaceColumnDTO, FleetDTO>> raceColumnTableSelectionModel;

    private List<StrippedLeaderboardDTO> availableLeaderboardList;

    private final SingleSelectionModel<StrippedLeaderboardDTO> leaderboardSelectionModel;

    private final RaceSelectionProvider raceSelectionProvider;

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

    public LeaderboardConfigPanel(SailingServiceAsync sailingService, AdminConsoleEntryPoint adminConsole,
            final ErrorReporter errorReporter, StringMessages theStringConstants, final boolean showRaceDetails) {
        this.stringMessages = theStringConstants;
        this.sailingService = sailingService;
        leaderboardList = new ListDataProvider<StrippedLeaderboardDTO>();
        allRegattas = new ArrayList<RegattaDTO>(); 
        raceColumnAndFleetList = new ListDataProvider<Pair<RaceColumnDTO, FleetDTO>>();
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

        Label lblFilterEvents = new Label(stringMessages.filterLeaderboardsByName() + ": ");
        HorizontalPanel filterPanel = new HorizontalPanel();
        filterPanel.setSpacing(5);
        filterPanel.add(lblFilterEvents);
        filterPanel.setCellVerticalAlignment(lblFilterEvents, HasVerticalAlignment.ALIGN_MIDDLE);
        filterLeaderboardTextbox = new TextBox();
        filterLeaderboardTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                fillRaceListFromAvailableLeaderboardsApplyingFilter();
            }
        });
        filterPanel.add(filterLeaderboardTextbox);
        leaderboardsPanel.add(filterPanel);

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        leaderboardTable = new CellTable<StrippedLeaderboardDTO>(/* pageSize */10000, tableRes);
        ListHandler<StrippedLeaderboardDTO> leaderboardColumnListHandler = new ListHandler<StrippedLeaderboardDTO>(
                leaderboardList.getList());

        AnchorCell anchorCell = new AnchorCell();
        Column<StrippedLeaderboardDTO, SafeHtml> linkColumn = new Column<StrippedLeaderboardDTO, SafeHtml>(anchorCell) {
            @Override
            public SafeHtml getValue(StrippedLeaderboardDTO object) {
                String debugParam = Window.Location.getParameter("gwt.codesvr");
                String link = URLFactory.INSTANCE.encode("/gwt/Leaderboard.html?name=" + object.name
                        + (showRaceDetails ? "&showRaceDetails=true" : "")
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
                String result = leaderboard.isRegattaLeaderboard ? "Regatta" : "Flexible";
                if(leaderboard.isMetaLeaderboard) {
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
        
        ImagesBarColumn<StrippedLeaderboardDTO, LeaderboardConfigImagesBarCell> leaderboardActionColumn = new ImagesBarColumn<StrippedLeaderboardDTO, LeaderboardConfigImagesBarCell>(
                new LeaderboardConfigImagesBarCell(stringMessages));
        leaderboardActionColumn.setFieldUpdater(new FieldUpdater<StrippedLeaderboardDTO, String>() {
            @Override
            public void update(int index, StrippedLeaderboardDTO leaderboardDTO, String value) {
                if (LeaderboardConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm("Do you really want to remove the leaderboard: '" + leaderboardDTO.name + "' ?")) {
                        removeLeaderboard(leaderboardDTO);
                    }
                } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    final String oldLeaderboardName = leaderboardDTO.name;
                    List<StrippedLeaderboardDTO> otherExistingLeaderboard = new ArrayList<StrippedLeaderboardDTO>();
                    otherExistingLeaderboard.addAll(availableLeaderboardList);
                    otherExistingLeaderboard.remove(leaderboardDTO);
                    if(leaderboardDTO.isMetaLeaderboard) {
                        Window.alert("This is a meta leaderboard. It can't be changed here.");
                    } else {
                        if (leaderboardDTO.isRegattaLeaderboard) {
                            LeaderboardDescriptor descriptor = new LeaderboardDescriptor(leaderboardDTO.name, 
                                    null, leaderboardDTO.discardThresholds, leaderboardDTO.regattaName);
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
                            LeaderboardDescriptor descriptor = new LeaderboardDescriptor(leaderboardDTO.name, leaderboardDTO.scoringScheme, leaderboardDTO.discardThresholds);
                            FlexibleLeaderboardEditDialog dialog = new FlexibleLeaderboardEditDialog(Collections
                                    .unmodifiableCollection(otherExistingLeaderboard),
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
                        }
                    }
                } else if (LeaderboardConfigImagesBarCell.ACTION_EDIT_SCORES.equals(value)) {
                    String debugParam = Window.Location.getParameter("gwt.codesvr");
                    Window.open("/gwt/LeaderboardEditing.html?name=" + leaderboardDTO.name
                            + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""), "_blank", null);
                } else if (LeaderboardConfigImagesBarCell.ACTION_CONFIGURE_URL.equals(value)) {
                    openLeaderboardUrlConfigDialog();
                }
            }
        });
        leaderboardTable.addColumn(linkColumn, stringMessages.name());
        leaderboardTable.addColumn(discardingOptionsColumn, stringMessages.discarding());
        leaderboardTable.addColumn(leaderboardTypeColumn, stringMessages.type());
        leaderboardTable.addColumn(scoringSystemColumn, stringMessages.scoringSystem());
        leaderboardTable.addColumn(leaderboardActionColumn, stringMessages.actions());
        leaderboardTable.addColumnSortHandler(leaderboardColumnListHandler);
        leaderboardTable.setWidth("100%");
        leaderboardSelectionModel = new SingleSelectionModel<StrippedLeaderboardDTO>();
        leaderboardTable.setSelectionModel(leaderboardSelectionModel);
        leaderboardSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                selectedLeaderboard = leaderboardSelectionModel.getSelectedObject();
                leaderboardSelectionChanged();
            }
        });
        leaderboardList.addDataDisplay(leaderboardTable);
        leaderboardsPanel.add(leaderboardTable);
        HorizontalPanel leaderboardButtonPanel = new HorizontalPanel();
        leaderboardButtonPanel.setSpacing(5);
        leaderboardsPanel.add(leaderboardButtonPanel);
        Button createFlexibleLeaderboardBtn = new Button(stringMessages.createFlexibleLeaderboard() + "...");
        leaderboardButtonPanel.add(createFlexibleLeaderboardBtn);
        createFlexibleLeaderboardBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                createFlexibleLeaderboard();
            }
        });
 
        Button createRegattaLeaderboardBtn = new Button(stringMessages.createRegattaLeaderboard() + "...");
        leaderboardButtonPanel.add(createRegattaLeaderboardBtn);
        createRegattaLeaderboardBtn.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                createRegattaLeaderboard();
            }
        });

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

        raceSelectionProvider = new RaceSelectionModel();
        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, adminConsole,
                raceSelectionProvider, stringMessages, /* multiselection */false);
        trackedRacesPanel.add(trackedRacesListComposite);
        trackedRacesListComposite.addTrackedRaceChangeListener(this);
        raceSelectionProvider.addRaceSelectionChangeListener(this);

        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.setSpacing(5);
        vPanel.add(hPanel);

        // ------------ races of the selected leaderboard ----------------
        AnchorCell raceAnchorCell = new AnchorCell();
        Column<Pair<RaceColumnDTO, FleetDTO>, SafeHtml> raceLinkColumn = new Column<Pair<RaceColumnDTO, FleetDTO>, SafeHtml>(raceAnchorCell) {
            @Override
            public SafeHtml getValue(Pair<RaceColumnDTO, FleetDTO> raceInLeaderboardDTOAndFleetName) {
                if (raceInLeaderboardDTOAndFleetName.getA().getRaceIdentifier(raceInLeaderboardDTOAndFleetName.getB()) != null) {
                    RegattaNameAndRaceName raceIdentifier = (RegattaNameAndRaceName) raceInLeaderboardDTOAndFleetName
                            .getA().getRaceIdentifier(raceInLeaderboardDTOAndFleetName.getB());
                    String debugParam = Window.Location.getParameter("gwt.codesvr");
                    String link = URLFactory.INSTANCE.encode("/gwt/RaceBoard.html?leaderboardName="
                            + selectedLeaderboard.name + "&raceName=" + raceIdentifier.getRaceName() + "&regattaName="
                            + raceIdentifier.getRegattaName()
                            + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""));
                    return ANCHORTEMPLATE.cell(link, raceInLeaderboardDTOAndFleetName.getA().getRaceColumnName());
                } else {
                    return SafeHtmlUtils.fromString(raceInLeaderboardDTOAndFleetName.getA().getRaceColumnName());
                }
            }
        };
        TextColumn<Pair<RaceColumnDTO, FleetDTO>> fleetNameColumn = new TextColumn<Pair<RaceColumnDTO, FleetDTO>>() {
            @Override
            public String getValue(Pair<RaceColumnDTO, FleetDTO> object) {
                return object.getB().name;
            }
        };

        Column<Pair<RaceColumnDTO, FleetDTO>, Boolean> isMedalRaceCheckboxColumn = new Column<Pair<RaceColumnDTO, FleetDTO>, Boolean>(
                new CheckboxCell()) {
            @Override
            public Boolean getValue(Pair<RaceColumnDTO, FleetDTO> race) {
                return race.getA().isMedalRace();
            }
        };
        isMedalRaceCheckboxColumn.setFieldUpdater(new FieldUpdater<Pair<RaceColumnDTO, FleetDTO>, Boolean>() {
            @Override
            public void update(int index, Pair<RaceColumnDTO, FleetDTO> object, Boolean value) {
                setIsMedalRace(selectedLeaderboard.name, object.getA(), value);
            }
        });
        isMedalRaceCheckboxColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        TextColumn<Pair<RaceColumnDTO, FleetDTO>> isLinkedRaceColumn = new TextColumn<Pair<RaceColumnDTO, FleetDTO>>() {
            @Override
            public String getValue(Pair<RaceColumnDTO, FleetDTO> raceColumnAndFleetName) {
                boolean isTrackedRace = raceColumnAndFleetName.getA().isTrackedRace(raceColumnAndFleetName.getB());
                return isTrackedRace ? stringMessages.yes() : stringMessages.no();
            }
        };
        ImagesBarColumn<Pair<RaceColumnDTO, FleetDTO>, LeaderboardRaceConfigImagesBarCell> raceActionColumn =
                new ImagesBarColumn<Pair<RaceColumnDTO, FleetDTO>, LeaderboardRaceConfigImagesBarCell>(
                        new LeaderboardRaceConfigImagesBarCell(stringMessages));
        raceActionColumn.setFieldUpdater(new FieldUpdater<Pair<RaceColumnDTO, FleetDTO>, String>() {
            @Override
            public void update(int index, Pair<RaceColumnDTO, FleetDTO> object, String value) {
                if (LeaderboardRaceConfigImagesBarCell.ACTION_REMOVE.equals(value)) {
                    if (Window.confirm(stringMessages.reallyRemoveRace(object.getA().getRaceColumnName()))) {
                        removeRaceColumn(object.getA());
                    }
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_EDIT.equals(value)) {
                    editRaceColumnOfLeaderboard(object);
                } else if (LeaderboardRaceConfigImagesBarCell.ACTION_UNLINK.equals(value)) {
                    unlinkRaceColumnFromTrackedRace(object.getA().getRaceColumnName(), object.getB());
                }
            }
        });

        Label lblRaceNamesIn = new Label(stringMessages.races());
        vPanel.add(lblRaceNamesIn);

        raceColumnTable = new CellTable<Pair<RaceColumnDTO, FleetDTO>>(/* pageSize */200, tableRes);
        raceColumnTable.addColumn(raceLinkColumn, stringMessages.name());
        raceColumnTable.addColumn(fleetNameColumn, stringMessages.fleet());
        raceColumnTable.addColumn(isMedalRaceCheckboxColumn, stringMessages.medalRace());
        raceColumnTable.addColumn(isLinkedRaceColumn, stringMessages.islinked());
        raceColumnTable.addColumn(raceActionColumn, stringMessages.actions());
        raceColumnAndFleetList.addDataDisplay(raceColumnTable);
        raceColumnTable.setWidth("500px");
        raceColumnTableSelectionModel = new SingleSelectionModel<Pair<RaceColumnDTO, FleetDTO>>();

        raceColumnTable.setSelectionModel(raceColumnTableSelectionModel);

        raceColumnTableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                leaderboardRaceColumnSelectionChanged();
            }
        });

        vPanel.add(raceColumnTable);

        HorizontalPanel selectedLeaderboardRaceButtonPanel = new HorizontalPanel();
        selectedLeaderboardRaceButtonPanel.setSpacing(5);
        vPanel.add(selectedLeaderboardRaceButtonPanel);

        addRaceColumnsButton = new Button(stringMessages.actionAddRaces() + "...");
        selectedLeaderboardRaceButtonPanel.add(addRaceColumnsButton);
        addRaceColumnsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if(selectedLeaderboard.isRegattaLeaderboard) {
                    Window.alert("This is a regatta leaderboard. You can only add races to this leaderboard directly in the regatta definition.");
                } else {
                    addRaceColumnsToLeaderboard();
                }
            }
        });
        
        columnMoveUpButton = new Button(stringMessages.columnMoveUp());
        selectedLeaderboardRaceButtonPanel.add(columnMoveUpButton);
        columnMoveUpButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelectedRaceColumnUp();
            }
        });
        columnMoveDownButton = new Button(stringMessages.columnMoveDown());
        selectedLeaderboardRaceButtonPanel.add(columnMoveDownButton);
        columnMoveDownButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelectedRaceColumnDown();
            }
        });

        loadAndRefreshLeaderboards();
    }

    /**
     * Allow the user to combine the various URL parameters that exist for the {@link LeaderboardEntryPoint} and obtain the
     * resulting URL in a link.
     */
    private void openLeaderboardUrlConfigDialog() {
        // TODO Auto-generated method stub
        
    }

    public void loadAndRefreshLeaderboards() {
        sailingService.getLeaderboards(new AsyncCallback<List<StrippedLeaderboardDTO>>() {
            @Override
            public void onSuccess(List<StrippedLeaderboardDTO> leaderboards) {
                leaderboardList.getList().clear();
                availableLeaderboardList.clear();
                leaderboardList.getList().addAll(leaderboards);
                availableLeaderboardList.addAll(leaderboards);
                fillRaceListFromAvailableLeaderboardsApplyingFilter();
                leaderboardSelectionChanged();
                leaderboardRaceColumnSelectionChanged();
            }

            @Override
            public void onFailure(Throwable t) {
                LeaderboardConfigPanel.this.errorReporter.reportError("Error trying to obtain list of leaderboards: "
                        + t.getMessage());
            }
        });
    }

    public void loadAndRefreshLeaderboard(final String leaderboardName) {
        leaderboardSelectionModel.setSelected(null, true);
        
        sailingService.getLeaderboard(leaderboardName, new AsyncCallback<StrippedLeaderboardDTO>() {
            @Override
            public void onSuccess(StrippedLeaderboardDTO leaderboard) {
                replaceLeaderboardInList(leaderboardList.getList(), leaderboardName, leaderboard);
                replaceLeaderboardInList(availableLeaderboardList, leaderboardName, leaderboard);
                
                leaderboardSelectionModel.setSelected(leaderboard, true);
            }

            @Override
            public void onFailure(Throwable t) {
                LeaderboardConfigPanel.this.errorReporter.reportError("Error trying to update leaderboard with name " +  leaderboardName +" : "
                        + t.getMessage());
            }
        });
    }

    private void replaceLeaderboardInList(List<StrippedLeaderboardDTO> leaderboardList, String leaderboardToReplace, StrippedLeaderboardDTO newLeaderboard) {
        int index = -1;
        for(StrippedLeaderboardDTO existingLeaderboard: leaderboardList) {
            index++;
            if(existingLeaderboard.name.equals(leaderboardToReplace)) {
                break;
            }
        }
        if(index >= 0) {
            leaderboardList.set(index, newLeaderboard);
        }
    }
    
    private void unlinkRaceColumnFromTrackedRace(final String raceColumnName, final FleetDTO fleet) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        sailingService.disconnectLeaderboardColumnFromTrackedRace(selectedLeaderboardName, raceColumnName, fleet.name,
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
                        raceColumnAndFleetList.refresh();
                    }
                });
    }

    private void removeRaceColumn(final RaceColumnDTO raceColumnDTO) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String raceColumnString = raceColumnDTO.getRaceColumnName();
        sailingService.removeLeaderboardColumn(getSelectedLeaderboardName(), raceColumnString,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to remove leaderboard race column " + raceColumnDTO
                                + " in leaderboard " + getSelectedLeaderboardName() + ": " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(Void arg0) {
                        loadAndRefreshLeaderboard(selectedLeaderboardName);
                    }
                });
    }

    /**
     * The selected row is potentially only one of several fleet-based rows of the same RaceColumn. In this case,
     * move all fleet-based rows of the same RaceColumn down.
     */
    private void moveSelectedRaceColumnDown() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String selectedRaceColumnName = raceColumnTableSelectionModel.getSelectedObject().getA().getRaceColumnName();
        sailingService.moveLeaderboardColumnDown(getSelectedLeaderboardName(), selectedRaceColumnName,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to move leaderboard race column "
                                + selectedRaceColumnName + " in leaderboard " + getSelectedLeaderboardName()
                                + " down: " + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        loadAndRefreshLeaderboard(selectedLeaderboardName);
                        selectRaceColumn(selectedRaceColumnName);
                    }
                });
    }

    /**
     * The selected row is potentially only one of several fleet-based rows of the same RaceColumn. In this case,
     * move all fleet-based rows of the same RaceColumn down.
     */
    private void moveSelectedRaceColumnUp() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String selectedRaceColumnName = raceColumnTableSelectionModel.getSelectedObject().getA().getRaceColumnName();
        sailingService.moveLeaderboardColumnUp(getSelectedLeaderboardName(), selectedRaceColumnName,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error trying to move leaderboard race column "
                                + selectedRaceColumnName + " in leaderboard " + getSelectedLeaderboardName() + " up: "
                                + caught.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        loadAndRefreshLeaderboard(selectedLeaderboardName);
                        selectRaceColumn(selectedRaceColumnName);
                    }
                });
    }

    private void leaderboardRaceColumnSelectionChanged() {
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

    private void selectRaceColumn(String raceCoumnName) {
        List<Pair<RaceColumnDTO, FleetDTO>> list = raceColumnAndFleetList.getList();
        for (Pair<RaceColumnDTO, FleetDTO> pair : list) {
            if(pair.getA().name.equals(raceCoumnName)) {
                raceColumnTableSelectionModel.setSelected(pair, true);
                break;
            }
        }
    }
    
    private void selectTrackedRaceInRaceList() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final Pair<RaceColumnDTO, FleetDTO> selectedRaceColumnAndFleetNameInLeaderboard = getSelectedRaceColumnWithFleet();
        final String selectedRaceColumnName = selectedRaceColumnAndFleetNameInLeaderboard.getA().getRaceColumnName();
        final String selectedFleetName = selectedRaceColumnAndFleetNameInLeaderboard.getB().name;
        sailingService.getRegattaAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(selectedLeaderboardName,
                selectedRaceColumnName, new AsyncCallback<Map<String, RegattaAndRaceIdentifier>>() {
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
                });
    }

    private void selectRaceInList(String regattaName, String raceName) {
        RegattaNameAndRaceName raceIdentifier = new RegattaNameAndRaceName(regattaName, raceName);  
        trackedRacesListComposite.selectRaceByIdentifier(raceIdentifier);
    }

    private Pair<RaceColumnDTO, FleetDTO> getSelectedRaceColumnWithFleet() {
        Pair<RaceColumnDTO, FleetDTO> raceInLeaderboardAndFleetName = raceColumnTableSelectionModel.getSelectedObject();
        return raceInLeaderboardAndFleetName;
    }

    private void editRaceColumnOfLeaderboard(final Pair<RaceColumnDTO, FleetDTO> raceColumnWithFleet) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String oldRaceColumnName = raceColumnWithFleet.getA().getRaceColumnName();
        List<RaceColumnDTO> existingRacesWithoutThisRace = new ArrayList<RaceColumnDTO>();
        for(Pair<RaceColumnDTO, FleetDTO> pair: raceColumnAndFleetList.getList()) {
            existingRacesWithoutThisRace.add(pair.getA());
        }
        existingRacesWithoutThisRace.remove(raceColumnWithFleet.getA());
        final RaceColumnInLeaderboardDialog raceDialog = new RaceColumnInLeaderboardDialog(existingRacesWithoutThisRace,
                raceColumnWithFleet.getA(), stringMessages, new DialogCallback<RaceColumnDTO>() {
                    @Override
                    public void cancel() {
                    }

                    @Override
                    public void ok(final RaceColumnDTO result) {
                        final ParallelExecutionCallback<Void> renameLeaderboardColumnCallback = new ParallelExecutionCallback<Void>();  
                        final ParallelExecutionCallback<Void> updateIsMedalRaceCallback = new ParallelExecutionCallback<Void>();  
                        new ParallelExecutionHolder(renameLeaderboardColumnCallback, updateIsMedalRaceCallback) {
                            @Override
                            public void handleSuccess() {
                                loadAndRefreshLeaderboard(selectedLeaderboardName);
                                selectRaceColumn(result.getRaceColumnName());
                            }
                            @Override
                            public void handleFailure(Throwable t) {
                                errorReporter.reportError("Error trying to update data of race column "
                                        + oldRaceColumnName + " in leaderboard " + selectedLeaderboardName + ": "
                                        + t.getMessage());
                            }
                        };
                        sailingService.renameLeaderboardColumn(selectedLeaderboardName, oldRaceColumnName,
                                result.getRaceColumnName(), renameLeaderboardColumnCallback);
                        sailingService.updateIsMedalRace(selectedLeaderboardName, result.getRaceColumnName(),
                                result.isMedalRace(), updateIsMedalRaceCallback);
                    }
            });
        raceDialog.show();
    }

    private void setIsMedalRace(String leaderboardName, final RaceColumnDTO raceInLeaderboard,
            final boolean isMedalRace) {
        sailingService.updateIsMedalRace(leaderboardName, raceInLeaderboard.getRaceColumnName(), isMedalRace,
                new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(Void result) {
                        selectedLeaderboard.setIsMedalRace(raceInLeaderboard.getRaceColumnName(), isMedalRace);
                    }
                });
    }

    private void addRaceColumnsToLeaderboard() {
        final String leaderboardName = getSelectedLeaderboardName();
        final List<RaceColumnDTO> existingRaceColumns = new ArrayList<RaceColumnDTO>();
        for(Pair<RaceColumnDTO, FleetDTO> pair: raceColumnAndFleetList.getList()) {
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
        raceDialog.show();
    }

    private void updateRaceColumnsOfLeaderboard(final String leaderboardName, List<RaceColumnDTO> existingRaceColumns, List<RaceColumnDTO> newRaceColumns) {
        final List<Pair<String, Boolean>> raceColumnsToAdd = new ArrayList<Pair<String, Boolean>>();
        
        for(RaceColumnDTO newRaceColumn: newRaceColumns) {
            if(!existingRaceColumns.contains(newRaceColumn)) {
                raceColumnsToAdd.add(new Pair<String, Boolean>(newRaceColumn.name, newRaceColumn.isMedalRace()));
            }
        }
        
        sailingService.addColumnsToLeaderboard(leaderboardName, raceColumnsToAdd, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to add race columns to leaderboard " + leaderboardName
                        + ": " + caught.getMessage());
            }

            @Override
            public void onSuccess(Void v) {
                loadAndRefreshLeaderboard(leaderboardName);
            }
        });
    }

    private String getSelectedLeaderboardName() {
        return selectedLeaderboard != null ? selectedLeaderboard.name : null;
    }

    private void leaderboardSelectionChanged() {
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
        if (selectedLeaderboard != null && !selectedLeaderboard.isMetaLeaderboard) {
            raceColumnAndFleetList.getList().clear();
            for (RaceColumnDTO raceColumn : selectedLeaderboard.getRaceList()) {
                for (FleetDTO fleet : raceColumn.getFleets()) {
                    raceColumnAndFleetList.getList().add(new Pair<RaceColumnDTO, FleetDTO>(raceColumn, fleet));
                }
            }
            selectedLeaderBoardPanel.setVisible(true);
            trackedRacesCaptionPanel.setVisible(true);
            selectedLeaderBoardPanel.setCaptionText("Details of leaderboard '" + selectedLeaderboard.name + "'");
        } else {
            selectedLeaderBoardPanel.setVisible(false);
            trackedRacesCaptionPanel.setVisible(false);
            selectedLeaderboard = null;
            selectedRaceInLeaderboard = null;
        }
    }

    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        trackedRacesListComposite.fillRegattas(regattas);
        
        allRegattas.clear();
        allRegattas.addAll(regattas);
    }

    @Override
    public void changeTrackingRace(RegattaAndRaceIdentifier regattaAndRaceIdentifier, boolean isTracked) {
        for (Pair<RaceColumnDTO, FleetDTO> raceColumnAndFleetName : raceColumnAndFleetList.getList()) {
            if (raceColumnAndFleetName.getA().getRaceColumnName().equals(regattaAndRaceIdentifier.getRaceName())) {
                raceColumnAndFleetName.getA().setRaceIdentifier(raceColumnAndFleetName.getB(), regattaAndRaceIdentifier);
            }
        }
        raceColumnAndFleetList.refresh();
    }

    private void createFlexibleLeaderboard() {
        AbstractLeaderboardDialog dialog = new FlexibleLeaderboardCreateDialog(Collections.unmodifiableCollection(availableLeaderboardList),
                stringMessages, errorReporter, new DialogCallback<LeaderboardDescriptor>() {
            @Override
            public void cancel() {
            }

            @Override
            public void ok(final LeaderboardDescriptor newLeaderboard) {
                sailingService.createFlexibleLeaderboard(newLeaderboard.getName(), newLeaderboard.getDiscardThresholds(),
                        newLeaderboard.getScoringScheme(),
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
                        });
            }
        });
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
                sailingService.createRegattaLeaderboard(regattaIdentifier, newLeaderboard.getDiscardThresholds(),
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
        dialog.show();
    }

    private void addLeaderboard(StrippedLeaderboardDTO result) {
        leaderboardList.getList().add(result);
        availableLeaderboardList.add(result);
        selectedLeaderboard = result;
        leaderboardSelectionChanged();
    }
    
    private void updateLeaderboard(final String oldLeaderboardName, final LeaderboardDescriptor leaderboardToUdate) {
        sailingService.updateLeaderboard(oldLeaderboardName, leaderboardToUdate.getName(),
                leaderboardToUdate.getDiscardThresholds(), new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to update leaderboard " + oldLeaderboardName + ": "
                                + t.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {
                        for (int i = 0; i < leaderboardList.getList().size(); i++) {
                            StrippedLeaderboardDTO dao = leaderboardList.getList().get(i);
                            if (dao.name.equals(oldLeaderboardName)) {
                                dao.name = leaderboardToUdate.getName();
                                dao.discardThresholds = leaderboardToUdate.getDiscardThresholds();
                                break;
                            }
                        }
                        leaderboardList.refresh();
                    }
                });
    }

    private void removeLeaderboard(final StrippedLeaderboardDTO leaderBoard) {
        sailingService.removeLeaderboard(leaderBoard.name, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error trying to remove leaderboard " + leaderBoard.name + ": "
                        + caught.getMessage());
            }

            @Override
            public void onSuccess(Void result) {
                // check if the removed leaderboard was the selected one
                leaderboardList.getList().remove(leaderBoard);
                availableLeaderboardList.remove(leaderBoard);

                if (selectedLeaderboard != null && selectedLeaderboard.name.equals(leaderBoard.name)) {
                    selectedLeaderboard = null;
                    leaderboardSelectionChanged();
                }
            }
        });
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        // if no leaderboard column is selected, ignore the race selection change
        Pair<RaceColumnDTO, FleetDTO> selectedRaceColumnAndFleetName = getSelectedRaceColumnWithFleet();
        if (selectedRaceColumnAndFleetName != null) {
            if (selectedRaces.isEmpty()) {
                unlinkRaceColumnFromTrackedRace(selectedRaceColumnAndFleetName.getA().getRaceColumnName(), selectedRaceColumnAndFleetName.getB());
            } else {
                linkTrackedRaceToSelectedRaceColumn(selectedRaceColumnAndFleetName.getA(), selectedRaceColumnAndFleetName.getB(),
                        selectedRaces.iterator().next());
            }
        }
    }

    private void linkTrackedRaceToSelectedRaceColumn(final RaceColumnDTO selectedRaceInLeaderboard,
            final FleetDTO fleet, final RegattaAndRaceIdentifier selectedRace) {
        sailingService.connectTrackedRaceToLeaderboardColumn(getSelectedLeaderboardName(), selectedRaceInLeaderboard
                .getRaceColumnName(), fleet.name, selectedRace,
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
                            raceColumnAndFleetList.refresh();
                        }
                    }
                });
    }

    private void fillRaceListFromAvailableLeaderboardsApplyingFilter() {
        String text = filterLeaderboardTextbox.getText();
        List<String> wordsToFilter = Arrays.asList(text.split(" "));
        leaderboardList.getList().clear();
        if (text != null && !text.isEmpty()) {
            for (StrippedLeaderboardDTO dao : availableLeaderboardList) {
                boolean failed = false;
                for (String word : wordsToFilter) {
                    String textAsUppercase = word.toUpperCase().trim();
                    if (!dao.name.toUpperCase().contains(textAsUppercase)) {
                        failed = true;
                        break;
                    }
                }
                if (!failed) {
                    leaderboardList.getList().add(dao);
                }
            }
        } else {
            leaderboardList.getList().addAll(availableLeaderboardList);
        }
        // now sort again according to selected criterion
        ColumnSortEvent.fire(leaderboardTable, leaderboardTable.getColumnSortList());
    }
}