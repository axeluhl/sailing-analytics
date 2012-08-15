package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
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
import com.sap.sailing.domain.common.ScoringSchemeType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.URLFactory;
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

    private final Button addColumnButton;

    private final Button columnMoveUpButton;

    private final Button columnMoveDownButton;

    private final CaptionPanel selectedLeaderBoardPanel;
    private final CaptionPanel trackedRacesCaptionPanel;
    private final List<RegattaDTO> allRegattas;

    private TextBox filterLeaderboardTextbox;

    final SingleSelectionModel<Pair<RaceColumnDTO, FleetDTO>> raceColumnTableSelectionModel;

    private List<StrippedLeaderboardDTO> availableLeaderboardList;

    private final SingleSelectionModel<StrippedLeaderboardDTO> tableSelectionModel;

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
        this.setWidget(mainPanel);

        //Create leaderboards list and functionality
        CaptionPanel leaderboardsCaptionPanel = new CaptionPanel(stringMessages.leaderboards());
        leaderboardsCaptionPanel.setStyleName("bold");
        leaderboardsCaptionPanel.setWidth("50%");
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
        ImagesBarColumn<StrippedLeaderboardDTO, LeaderboardConfigImagesBarCell> leaderboardActionColumn = new ImagesBarColumn<StrippedLeaderboardDTO, LeaderboardConfigImagesBarCell>(
                new LeaderboardConfigImagesBarCell(stringMessages));
        leaderboardActionColumn.setFieldUpdater(new FieldUpdater<StrippedLeaderboardDTO, String>() {
            @Override
            public void update(int index, StrippedLeaderboardDTO object, String value) {
                if ("ACTION_REMOVE".equals(value)) {
                    if (Window.confirm("Do you really want to remove the leaderboard: '" + object.name + "' ?")) {
                        removeLeaderboard(object);
                    }
                } else if ("ACTION_EDIT".equals(value)) {
                    final String oldLeaderboardName = object.name;
                    List<StrippedLeaderboardDTO> otherExistingLeaderboard = new ArrayList<StrippedLeaderboardDTO>();
                    otherExistingLeaderboard.addAll(availableLeaderboardList);
                    otherExistingLeaderboard.remove(object);
                    LeaderboardEditDialog dialog = new LeaderboardEditDialog(Collections
                            .unmodifiableCollection(otherExistingLeaderboard), Collections.unmodifiableCollection(allRegattas),
                            object, stringMessages, errorReporter,
                            new AsyncCallback<StrippedLeaderboardDTO>() {
                                @Override
                                public void onFailure(Throwable arg0) {
                                }

                                @Override
                                public void onSuccess(StrippedLeaderboardDTO result) {
                                    updateLeaderboard(oldLeaderboardName, result);
                                }
                            });
                    dialog.show();
                } else if ("ACTION_EDIT_SCORES".equals(value)) {
                    String debugParam = Window.Location.getParameter("gwt.codesvr");
                    Window.open("/gwt/LeaderboardEditing.html?name=" + object.name
                            + (debugParam != null && !debugParam.isEmpty() ? "&gwt.codesvr=" + debugParam : ""), "_blank", null);
                }
            }
        });
        leaderboardTable.addColumn(linkColumn, "Name");
        leaderboardTable.addColumn(discardingOptionsColumn, "Discarding");
        leaderboardTable.addColumn(leaderboardActionColumn, "Actions");
        leaderboardTable.addColumnSortHandler(leaderboardColumnListHandler);
        leaderboardTable.setWidth("500px");
        tableSelectionModel = new SingleSelectionModel<StrippedLeaderboardDTO>();
        leaderboardTable.setSelectionModel(tableSelectionModel);
        tableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                selectedLeaderboard = tableSelectionModel.getSelectedObject();
                leaderboardSelectionChanged();
            }
        });
        leaderboardList.addDataDisplay(leaderboardTable);
        leaderboardsPanel.add(leaderboardTable);
        HorizontalPanel leaderboardButtonPanel = new HorizontalPanel();
        leaderboardButtonPanel.setSpacing(5);
        leaderboardsPanel.add(leaderboardButtonPanel);
        Button btnNew = new Button("Create Leaderboard...");
        leaderboardButtonPanel.add(btnNew);
        btnNew.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                addNewLeaderboard();
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
                if ("ACTION_REMOVE".equals(value)) {
                    if (Window.confirm(stringMessages.reallyRemoveRace(object.getA().getRaceColumnName()))) {
                        removeRaceColumn(object.getA());
                    }
                } else if ("ACTION_EDIT".equals(value)) {
                    editRaceColumnOfLeaderboard(object);
                } else if ("ACTION_UNLINK".equals(value)) {
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

        addColumnButton = new Button("Add race...");
        selectedLeaderboardRaceButtonPanel.add(addColumnButton);
        addColumnButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                addRaceColumnToLeaderboard();
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

        loadAndRefreshAllData();
    }

    public void loadAndRefreshAllData() {
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
                        getSelectedRaceColumnAndFleetName().getA().setRaceIdentifier(fleet, null);
                        raceColumnAndFleetList.refresh();
                    }
                });
    }

    private void removeRaceColumn(final RaceColumnDTO raceColumnDTO) {
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
                        Iterator<Pair<RaceColumnDTO, FleetDTO>> rowIter = raceColumnAndFleetList.getList().iterator();
                        while (rowIter.hasNext()) {
                            Pair<RaceColumnDTO, FleetDTO> next = rowIter.next();
                            if (raceColumnTableSelectionModel.isSelected(next)) {
                                raceColumnTableSelectionModel.setSelected(next, false);
                            }
                            if (next.getA() == raceColumnDTO) {
                                rowIter.remove();
                            }
                        }
                        selectedLeaderboard.removeRace(raceColumnString);
                    }
                });
    }

    /**
     * The selected row is potentially only one of several fleet-based rows of the same RaceColumn. In this case,
     * move all fleet-based rows of the same RaceColumn down.
     */
    private void moveSelectedRaceColumnDown() {
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
                        leaderboardSelectionChanged();
                    }
                });
    }

    /**
     * The selected row is potentially only one of several fleet-based rows of the same RaceColumn. In this case,
     * move all fleet-based rows of the same RaceColumn down.
     */
    private void moveSelectedRaceColumnUp() {
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
                        leaderboardSelectionChanged();
                    }
                });
    }

    private void leaderboardRaceColumnSelectionChanged() {
        selectedRaceInLeaderboard = getSelectedRaceColumnAndFleetName();
        if (selectedRaceInLeaderboard != null) {
            columnMoveUpButton.setEnabled(true);
            columnMoveDownButton.setEnabled(true);
            reloadRaceInLeaderboardRow(selectedRaceInLeaderboard.getA().getRaceColumnName(), /* fleet name */ selectedRaceInLeaderboard.getB(),
                    selectedRaceInLeaderboard);
            selectTrackedRaceInRaceList();
        } else {
            columnMoveUpButton.setEnabled(false);
            columnMoveDownButton.setEnabled(false);
            trackedRacesListComposite.clearSelection();
        }
    }

    private void selectTrackedRaceInRaceList() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final Pair<RaceColumnDTO, FleetDTO> selectedRaceColumnAndFleetNameInLeaderboard = getSelectedRaceColumnAndFleetName();
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

    private Pair<RaceColumnDTO, FleetDTO> getSelectedRaceColumnAndFleetName() {
        Pair<RaceColumnDTO, FleetDTO> raceInLeaderboardAndFleetName = raceColumnTableSelectionModel.getSelectedObject();
        return raceInLeaderboardAndFleetName;
    }

    private void editRaceColumnOfLeaderboard(final Pair<RaceColumnDTO, FleetDTO> object) {
        final String oldRaceName = object.getA().getRaceColumnName();
        List<Pair<RaceColumnDTO, FleetDTO>> existingRacesWithoutThisRace = new ArrayList<Pair<RaceColumnDTO, FleetDTO>>();
        existingRacesWithoutThisRace.addAll(raceColumnAndFleetList.getList());
        existingRacesWithoutThisRace.remove(object);
        final RaceColumnInLeaderboardDialog raceDialog = new RaceColumnInLeaderboardDialog(existingRacesWithoutThisRace,
                object.getA(), stringMessages, new AsyncCallback<Pair<RaceColumnDTO, FleetDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(final Pair<RaceColumnDTO, FleetDTO> result) {
                        sailingService.renameLeaderboardColumn(getSelectedLeaderboardName(), oldRaceName,
                                result.getA().getRaceColumnName(), new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                    }

                                    @Override
                                    public void onSuccess(Void v) {
                                        sailingService.updateIsMedalRace(getSelectedLeaderboardName(),
                                                result.getA().getRaceColumnName(), result.getA().isMedalRace(),
                                                new AsyncCallback<Void>() {

                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                    }

                                                    @Override
                                                    public void onSuccess(Void v) {
                                                        object.getA().setMedalRace(result.getA().isMedalRace());
                                                        reloadRaceInLeaderboardRow(oldRaceName, /* fleet name */ result.getB(), result);
                                                    }
                                                });
                                    }
                                });
                    }

                });
        raceDialog.show();
    }

    private void reloadRaceInLeaderboardRow(String oldRaceName, FleetDTO oldFleet, Pair<RaceColumnDTO, FleetDTO> newRaceColumnAndFleet) {
        int index = -1;
        for (int i = 0; i < raceColumnAndFleetList.getList().size(); i++) {
            Pair<RaceColumnDTO, FleetDTO> raceColumnAndFleetName = raceColumnAndFleetList.getList().get(i);
            if (raceColumnAndFleetName.getA().getRaceColumnName().equals(oldRaceName) && raceColumnAndFleetName.getB().equals(oldFleet)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            raceColumnAndFleetList.getList().set(index, newRaceColumnAndFleet);
        }
        raceColumnAndFleetList.refresh();
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

    private void addRaceColumnToLeaderboard() {
        final RaceColumnDTO raceInLeaderboard = new RaceColumnDTO();
        final String leaderboardName = getSelectedLeaderboardName();
        final RaceColumnInLeaderboardDialog raceDialog = new RaceColumnInLeaderboardDialog(raceColumnAndFleetList.getList(),
                raceInLeaderboard, stringMessages, new AsyncCallback<Pair<RaceColumnDTO, FleetDTO>>() {
                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(final Pair<RaceColumnDTO, FleetDTO> result) {
                        sailingService.addColumnToLeaderboard(result.getA().getRaceColumnName(), leaderboardName,
                                result.getA().isMedalRace(), new AsyncCallback<Void>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError("Error trying to add column "
                                                + result.getA().getRaceColumnName() + " to leaderboard " + leaderboardName
                                                + ": " + caught.getMessage());

                                    }

                                    @Override
                                    public void onSuccess(Void v) {
                                        // columnNamesInSelectedLeaderboardListBox.addItem(columnNameAndMedalRace.getA());
                                        raceColumnAndFleetList.getList().add(result);
                                        // selectedLeaderboard.raceNamesAndMedalRaceAndTracked.put(columnNameAndMedalRace.getA(),
                                        // new Pair<Boolean, Boolean>(/* medal race */ columnNameAndMedalRace.getB(),
                                        // /* tracked */ false));
                                        selectedLeaderboard.addRace(result.getA().getRaceColumnName(), /* fleet name */ result.getB(),
                                                result.getA().isMedalRace(), /* tracked race identifier */ null, /* StrippedRaceDTO */ null);
                                    }
                                });
                    }
                });
        raceDialog.show();
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
        if (selectedLeaderboard != null) {
            raceColumnAndFleetList.getList().clear();
            for (RaceColumnDTO raceColumn : selectedLeaderboard.getRaceList()) {
                for (FleetDTO fleet : raceColumn.getFleets()) {
                    raceColumnAndFleetList.getList().add(new Pair<RaceColumnDTO, FleetDTO>(raceColumn, fleet));
                }
            }
            selectedLeaderBoardPanel.setVisible(true);
            trackedRacesCaptionPanel.setVisible(true);
            selectedLeaderBoardPanel.setCaptionText("Details of leaderboard '" + selectedLeaderboard.name + "'");
            addColumnButton.setEnabled(true);
        } else {
            selectedLeaderBoardPanel.setVisible(false);
            trackedRacesCaptionPanel.setVisible(false);
            selectedLeaderboard = null;
            addColumnButton.setEnabled(false);
            leaderboardRaceColumnSelectionChanged();
        }
    }

    @Override
    public void fillRegattas(List<RegattaDTO> regattas) {
        trackedRacesListComposite.fillRegattas(regattas);
        
        allRegattas.clear();
        allRegattas.addAll(regattas);
    }

    @Override
    public void changeTrackingRace(RegattaNameAndRaceName regattaNameAndRaceName, boolean isTracked) {
        for (Pair<RaceColumnDTO, FleetDTO> raceColumnAndFleetName : raceColumnAndFleetList.getList()) {
            if (raceColumnAndFleetName.getA().getRaceColumnName().equals(regattaNameAndRaceName.getRaceName())) {
                raceColumnAndFleetName.getA().setRaceIdentifier(raceColumnAndFleetName.getB(), regattaNameAndRaceName);
            }
        }
        raceColumnAndFleetList.refresh();
    }

    private void addNewLeaderboard() {
        LeaderboardCreateDialog dialog = new LeaderboardCreateDialog(Collections.unmodifiableCollection(availableLeaderboardList),
                Collections.unmodifiableCollection(allRegattas), stringMessages, errorReporter, new AsyncCallback<StrippedLeaderboardDTO>() {
            @Override
            public void onFailure(Throwable arg0) {
            }

            @Override
            public void onSuccess(StrippedLeaderboardDTO result) {
                createNewLeaderboard(result);
            }
        });
        dialog.show();
    }

    private void createNewLeaderboard(final StrippedLeaderboardDTO newLeaderboard) {
        if(newLeaderboard.regatta == null) {
            sailingService.createFlexibleLeaderboard(newLeaderboard.name, newLeaderboard.discardThresholds,
                    ScoringSchemeType.LOW_POINT,
                    new AsyncCallback<StrippedLeaderboardDTO>() {
                        @Override
                        public void onFailure(Throwable t) {
                            errorReporter.reportError("Error trying to create new flexible leaderboard " + newLeaderboard.name
                                    + ": " + t.getMessage());
                        }

                        @Override
                        public void onSuccess(StrippedLeaderboardDTO result) {
                            addLeaderboard(result);
                        }
                    });
        } else {
            RegattaIdentifier regattaIdentifier = new RegattaName(newLeaderboard.regatta.name); 
            sailingService.createRegattaLeaderboard(regattaIdentifier, newLeaderboard.discardThresholds,
                    new AsyncCallback<StrippedLeaderboardDTO>() {
                        @Override
                        public void onFailure(Throwable t) {
                            errorReporter.reportError("Error trying to create new regatta leaderboard " + newLeaderboard.name
                                    + ": " + t.getMessage());
                        }

                        @Override
                        public void onSuccess(StrippedLeaderboardDTO result) {
                            addLeaderboard(result);
                        }
                    });
        }
    }

    private void addLeaderboard(StrippedLeaderboardDTO result) {
        leaderboardList.getList().add(result);
        availableLeaderboardList.add(result);
        selectedLeaderboard = result;
        leaderboardSelectionChanged();
    }
    
    private void updateLeaderboard(final String oldLeaderboardName, final StrippedLeaderboardDTO leaderboardToUdate) {
        sailingService.updateLeaderboard(oldLeaderboardName, leaderboardToUdate.name,
                leaderboardToUdate.discardThresholds, new AsyncCallback<Void>() {
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
                                leaderboardList.getList().set(i, leaderboardToUdate);
                                availableLeaderboardList.set(i, leaderboardToUdate);
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
        Pair<RaceColumnDTO, FleetDTO> selectedRaceColumnAndFleetName = getSelectedRaceColumnAndFleetName();
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