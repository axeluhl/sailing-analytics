package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
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
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RaceInLeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;
import com.sap.sailing.server.api.EventNameAndRaceName;

public class LeaderboardConfigPanel extends FormPanel implements EventDisplayer, RaceSelectionChangeListener,
        TrackedRaceChangedListener {

    private final TrackedEventsComposite trackedEventsComposite;

    private final StringConstants stringConstants;

    private final SailingServiceAsync sailingService;

    private final ListDataProvider<LeaderboardDAO> leaderboardList;

    private final ListDataProvider<RaceInLeaderboardDAO> raceColumnList;

    private final ErrorReporter errorReporter;

    private final CellTable<LeaderboardDAO> leaderboardTable;

    private final CellTable<RaceInLeaderboardDAO> raceColumnTable;

    private LeaderboardDAO selectedLeaderboard;

    private RaceInLeaderboardDAO selectedRaceInLeaderboard;

    private final Button addColumnButton;

    private final Button columnMoveUpButton;

    private final Button columnMoveDownButton;

    private final CaptionPanel selectedLeaderBoardPanel;
    private final CaptionPanel trackedRacesCaptionPanel;

    private TextBox filterRacesTextbox;

    final SingleSelectionModel<RaceInLeaderboardDAO> raceTableSelectionModel;

    private List<LeaderboardDAO> availableLeaderboardList;

    private final SingleSelectionModel<LeaderboardDAO> tableSelectionModel;

    public LeaderboardConfigPanel(SailingServiceAsync sailingService, AdminConsole adminConsole,
            final ErrorReporter errorReporter, StringConstants theStringConstants) {
        this.stringConstants = theStringConstants;
        this.sailingService = sailingService;
        leaderboardList = new ListDataProvider<LeaderboardDAO>();
        raceColumnList = new ListDataProvider<RaceInLeaderboardDAO>();
        this.errorReporter = errorReporter;
        this.availableLeaderboardList = new ArrayList<LeaderboardDAO>();
        readAllLeaderbords();
        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);

        Label lblLeaderboards = new Label(stringConstants.leaderboards());
        lblLeaderboards.setStyleName("bold");
        mainPanel.add(lblLeaderboards);

        Label lblFilterEvents = new Label(stringConstants.filterLeaderboardsByName() + ": ");
        HorizontalPanel filterPanel = new HorizontalPanel();
        filterPanel.setSpacing(5);
        filterPanel.add(lblFilterEvents);
        filterPanel.setCellVerticalAlignment(lblFilterEvents, HasVerticalAlignment.ALIGN_MIDDLE);
        filterRacesTextbox = new TextBox();
        filterRacesTextbox.addKeyUpHandler(new KeyUpHandler() {
            @Override
            public void onKeyUp(KeyUpEvent event) {
                String text = filterRacesTextbox.getText();
                leaderboardList.getList().clear();
                if (text == null || text.isEmpty()) {
                    leaderboardList.getList().addAll(availableLeaderboardList);
                } else {
                    String textAsUppercase = text.toUpperCase();
                    for (LeaderboardDAO dao : availableLeaderboardList) {
                        if (dao.name != null) {
                            if (dao.name.toUpperCase().contains(textAsUppercase))
                                leaderboardList.getList().add(dao);
                        }
                    }
                }
                selectedLeaderboard = null;
                clearSelection();
            }
        });
        filterPanel.add(filterRacesTextbox);
        mainPanel.add(filterPanel);
        TextColumn<LeaderboardDAO> leaderboardNameColumn = new TextColumn<LeaderboardDAO>() {
            @Override
            public String getValue(LeaderboardDAO object) {
                return object.name;
            }
        };
        TextColumn<LeaderboardDAO> discardingOptionsColumn = new TextColumn<LeaderboardDAO>() {
            @Override
            public String getValue(LeaderboardDAO leaderboard) {
                String result = "";
                if (leaderboard.discardThresholds != null) {
                    for (int discardThreshold : leaderboard.discardThresholds) {
                        result += discardThreshold + " ";
                    }
                }
                return result;
            }
        };
        ImagesBarColumn<LeaderboardDAO, LeaderboardConfigImagesBarCell> leaderboardActionColumn = new ImagesBarColumn<LeaderboardDAO, LeaderboardConfigImagesBarCell>(
                new LeaderboardConfigImagesBarCell(stringConstants));
        leaderboardActionColumn.setFieldUpdater(new FieldUpdater<LeaderboardDAO, String>() {
            @Override
            public void update(int index, LeaderboardDAO object, String value) {
                if ("ACTION_REMOVE".equals(value)) {
                    if (Window.confirm("Do you really want to remove the leaderboard: '" + object.name + "' ?")) {
                        removeLeaderboard(object);
                    }
                } else if ("ACTION_OPEN_BROWSER".equals(value)) {
                    Window.open("/Leaderboard.html?name=" + object.name, "_blank", null);
                } else if ("ACTION_EDIT".equals(value)) {
                    final String oldLeaderboardName = object.name;
                    List<LeaderboardDAO> otherExistingLeaderboard = new ArrayList<LeaderboardDAO>();
                    otherExistingLeaderboard.addAll(leaderboardList.getList());
                    otherExistingLeaderboard.remove(object);
                    LeaderboardEditDialog dialog = new LeaderboardEditDialog(Collections
                            .unmodifiableCollection(otherExistingLeaderboard), object, stringConstants, errorReporter,
                            new AsyncCallback<LeaderboardDAO>() {
                                @Override
                                public void onFailure(Throwable arg0) {
                                }

                                @Override
                                public void onSuccess(LeaderboardDAO result) {
                                    updateLeaderboard(oldLeaderboardName, result);
                                }
                            });
                    dialog.show();
                } else if ("ACTION_EDIT_SCORES".equals(value)) {
                    Window.open("/LeaderboardEditing.html?name=" + object.name, "_blank", null);
                }
            }
        });
        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        leaderboardTable = new CellTable<LeaderboardDAO>(/* pageSize */200, tableRes);
        leaderboardTable.addColumn(leaderboardNameColumn, "Name");
        leaderboardTable.addColumn(discardingOptionsColumn, "Discarding");
        leaderboardTable.addColumn(leaderboardActionColumn, "Actions");
        leaderboardTable.setWidth("500px");
        tableSelectionModel = new SingleSelectionModel<LeaderboardDAO>();
        leaderboardTable.setSelectionModel(tableSelectionModel);
        tableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                selectedLeaderboard = tableSelectionModel.getSelectedObject();
                leaderboardSelectionChanged();
            }
        });
        leaderboardList.addDataDisplay(leaderboardTable);
        mainPanel.add(leaderboardTable);
        HorizontalPanel leaderboardButtonPanel = new HorizontalPanel();
        leaderboardButtonPanel.setSpacing(5);
        mainPanel.add(leaderboardButtonPanel);
        Button btnNew = new Button("Create Leaderboard..");
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

        selectedLeaderBoardPanel = new CaptionPanel("Leaderboard:");
        selectedLeaderBoardPanel.setWidth("50%");
        splitPanel.add(selectedLeaderBoardPanel);

        VerticalPanel vPanel = new VerticalPanel();
        vPanel.setWidth("100%");
        selectedLeaderBoardPanel.setContentWidget(vPanel);

        trackedRacesCaptionPanel = new CaptionPanel("Tracked Races");
        trackedRacesCaptionPanel.setWidth("50%");
        splitPanel.add(trackedRacesCaptionPanel);

        VerticalPanel trackedRacesPanel = new VerticalPanel();
        trackedRacesPanel.setWidth("100%");
        trackedRacesCaptionPanel.setContentWidget(trackedRacesPanel);
        trackedRacesCaptionPanel.setStyleName("bold");

        trackedEventsComposite = new TrackedEventsComposite(sailingService, errorReporter, adminConsole,
                stringConstants, /* multiselection */false);
        trackedRacesPanel.add(trackedEventsComposite);
        trackedEventsComposite.addTrackedRaceChangeListener(this);
        trackedEventsComposite.addRaceSelectionChangeListener(this);

        HorizontalPanel hPanel = new HorizontalPanel();
        hPanel.setSpacing(5);
        vPanel.add(hPanel);

        Button stressTestButton = new Button(stringConstants.stressTest());
        hPanel.add(stressTestButton);
        stressTestButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                performStressTestForSelectedLeaderboard();
            }
        });

        // ------------ races of the selected leaderboard ----------------
        TextColumn<RaceInLeaderboardDAO> raceNameColumn = new TextColumn<RaceInLeaderboardDAO>() {
            @Override
            public String getValue(RaceInLeaderboardDAO object) {
                return object.getRaceColumnName();
            }
        };

        Column<RaceInLeaderboardDAO, Boolean> isMedalRaceCheckboxColumn = new Column<RaceInLeaderboardDAO, Boolean>(
                new CheckboxCell()) {
            @Override
            public Boolean getValue(RaceInLeaderboardDAO race) {
                return race.isMedalRace();
            }
        };
        isMedalRaceCheckboxColumn.setFieldUpdater(new FieldUpdater<RaceInLeaderboardDAO, Boolean>() {
            @Override
            public void update(int index, RaceInLeaderboardDAO object, Boolean value) {
                setIsMedalRace(selectedLeaderboard.name, object, value);
            }
        });
        isMedalRaceCheckboxColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);

        TextColumn<RaceInLeaderboardDAO> isLinkedRaceColumn = new TextColumn<RaceInLeaderboardDAO>() {
            @Override
            public String getValue(RaceInLeaderboardDAO race) {
                boolean isTrackedRace = race.isTrackedRace();
                return isTrackedRace ? stringConstants.yes() : stringConstants.no();
            }
        };

        ImagesBarColumn<RaceInLeaderboardDAO, LeaderboardRaceConfigImagesBarCell> raceActionColumn = new ImagesBarColumn<RaceInLeaderboardDAO, LeaderboardRaceConfigImagesBarCell>(
                new LeaderboardRaceConfigImagesBarCell(stringConstants));

        raceActionColumn.setFieldUpdater(new FieldUpdater<RaceInLeaderboardDAO, String>() {
            @Override
            public void update(int index, RaceInLeaderboardDAO object, String value) {
                if ("ACTION_REMOVE".equals(value)) {
                    if (Window.confirm("Do you really want to remove the race: '" + object + "' ?")) {
                        removeRaceColumn(object);
                    }
                } else if ("ACTION_EDIT".equals(value)) {
                    editRaceColumnOfLeaderboard(object);
                } else if ("ACTION_UNLINK".equals(value)) {
                    unlinkRaceColumnFromTrackedRace(object.getRaceColumnName());
                }
            }
        });

        Label lblRaceNamesIn = new Label(stringConstants.races());
        vPanel.add(lblRaceNamesIn);

        raceColumnTable = new CellTable<RaceInLeaderboardDAO>(/* pageSize */200, tableRes);
        raceColumnTable.addColumn(raceNameColumn, stringConstants.name());
        raceColumnTable.addColumn(isMedalRaceCheckboxColumn, stringConstants.medalRace());
        raceColumnTable.addColumn(isLinkedRaceColumn, stringConstants.islinked());
        raceColumnTable.addColumn(raceActionColumn, stringConstants.actions());
        raceColumnList.addDataDisplay(raceColumnTable);
        raceColumnTable.setWidth("500px");
        raceTableSelectionModel = new SingleSelectionModel<RaceInLeaderboardDAO>();

        raceColumnTable.setSelectionModel(raceTableSelectionModel);

        raceTableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
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

        columnMoveUpButton = new Button(stringConstants.columnMoveUp());
        selectedLeaderboardRaceButtonPanel.add(columnMoveUpButton);
        columnMoveUpButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelectedRaceColumnUp();
            }
        });
        columnMoveDownButton = new Button(stringConstants.columnMoveDown());
        selectedLeaderboardRaceButtonPanel.add(columnMoveDownButton);
        columnMoveDownButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                moveSelectedRaceColumnDown();
            }
        });

        leaderboardSelectionChanged();
        leaderboardRaceColumnSelectionChanged();
    }

    private void readAllLeaderbords() {
        sailingService.getLeaderboards(new AsyncCallback<List<LeaderboardDAO>>() {
            @Override
            public void onSuccess(List<LeaderboardDAO> leaderboards) {
                leaderboardList.getList().clear();
                availableLeaderboardList.clear();
                leaderboardList.getList().addAll(leaderboards);
                availableLeaderboardList.addAll(leaderboards);
            }

            @Override
            public void onFailure(Throwable t) {
                LeaderboardConfigPanel.this.errorReporter.reportError("Error trying to obtain list of leaderboards: "
                        + t.getMessage());
            }
        });
    }

    private void performStressTestForSelectedLeaderboard() {
        if (selectedLeaderboard != null) {
            final String leaderboardName = selectedLeaderboard.name;
            sailingService.stressTestLeaderboardByName(leaderboardName, 100, new AsyncCallback<Void>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error during leaderboard stress test for " + leaderboardName + ": "
                            + caught.getMessage());
                }

                @Override
                public void onSuccess(Void result) {
                    errorReporter.reportError("Stress test for " + leaderboardName + " finished successfully ");
                }
            });
        }
    }

    private void unlinkRaceColumnFromTrackedRace(final String raceColumn) {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        sailingService.disconnectLeaderboardColumnFromTrackedRace(selectedLeaderboardName, raceColumn,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to unlink tracked race from column " + raceColumn
                                + " from leaderboard " + selectedLeaderboardName + ": " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(Void arg0) {
                        trackedEventsComposite.clearSelection();
                        getSelectedRaceInLeaderboard().setTrackedRace(false);
                        raceColumnList.refresh();
                    }
                });
    }

    private void removeRaceColumn(final RaceInLeaderboardDAO raceColumn) {
        final String raceColumnString = raceColumn.getRaceColumnName();
        sailingService.removeLeaderboardColumn(getSelectedLeaderboardName(), raceColumnString,
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to remove leaderboard race column " + raceColumn
                                + " in leaderboard " + getSelectedLeaderboardName() + ": " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(Void arg0) {
                        raceColumnList.getList().remove(raceColumn);
                        // selectedLeaderboard.raceNamesAndMedalRaceAndTracked.remove(raceColumn);
                        selectedLeaderboard.removeRace(raceColumnString);
                        selectedLeaderboard.invalidateCompetitorOrdering();
                    }
                });
    }

    private void moveSelectedRaceColumnDown() {
        final String selectedRaceColumnName = raceTableSelectionModel.getSelectedObject().getRaceColumnName();

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
                        int rowIndex = getRowIndexOfSelectedRaceColumn();
                        if (rowIndex < raceColumnList.getList().size() - 1) {
                            Collections.swap(raceColumnList.getList(), rowIndex, rowIndex + 1);
                            selectedLeaderboard.moveRaceDown(selectedRaceColumnName);
                        }
                    }
                });
    }

    private int getRowIndexOfSelectedRaceColumn() {
        final RaceInLeaderboardDAO selectedRaceColumnName = raceTableSelectionModel.getSelectedObject();
        int rowIndex = -1;

        if (selectedRaceColumnName != null) {
            int listSize = raceColumnList.getList().size();
            for (int i = 0; i < listSize; i++) {
                if (selectedRaceColumnName.equals(raceColumnList.getList().get(i))) {
                    rowIndex = i;
                    break;
                }
            }
        }
        return rowIndex;
    }

    private void moveSelectedRaceColumnUp() {
        final String selectedRaceColumnName = raceTableSelectionModel.getSelectedObject().getRaceColumnName();

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
                        int rowIndex = getRowIndexOfSelectedRaceColumn();
                        if (rowIndex > 0) {
                            Collections.swap(raceColumnList.getList(), rowIndex, rowIndex - 1);
                            selectedLeaderboard.moveRaceUp(selectedRaceColumnName);
                        }
                    }
                });
    }

    private void leaderboardRaceColumnSelectionChanged() {
        selectedRaceInLeaderboard = getSelectedRaceInLeaderboard();
        if (selectedRaceInLeaderboard != null) {
            columnMoveUpButton.setEnabled(true);
            columnMoveDownButton.setEnabled(true);
            reloadRaceInLeaderboardRow(selectedRaceInLeaderboard.getRaceColumnName(), selectedRaceInLeaderboard);
            selectTrackedRaceInRaceTree();
        } else {
            columnMoveUpButton.setEnabled(false);
            columnMoveDownButton.setEnabled(false);
            trackedEventsComposite.clearSelection();
        }
    }

    private void selectTrackedRaceInRaceTree() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String selectedRaceColumnName = getSelectedRaceInLeaderboard().getRaceColumnName();
        sailingService.getEventAndRaceNameOfTrackedRaceConnectedToLeaderboardColumn(selectedLeaderboardName,
                selectedRaceColumnName, new AsyncCallback<Pair<String, String>>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to determine tracked race linked to race column "
                                + selectedRaceColumnName + " in leaderboard " + selectedLeaderboardName + ": "
                                + t.getMessage());
                    }

                    @Override
                    public void onSuccess(Pair<String, String> eventAndRaceName) {
                        if (eventAndRaceName != null) {
                            selectRaceInTree(eventAndRaceName.getA(), eventAndRaceName.getB());
                        } else {
                            trackedEventsComposite.clearSelection();
                        }
                    }
                });
    }

    private void selectRaceInTree(String eventName, String raceName) {
        trackedEventsComposite.selectRaceByName(eventName, raceName);
    }

    private RaceInLeaderboardDAO getSelectedRaceInLeaderboard() {
        RaceInLeaderboardDAO raceInLeaderboard = raceTableSelectionModel.getSelectedObject();
        return raceInLeaderboard;
    }

    private void editRaceColumnOfLeaderboard(final RaceInLeaderboardDAO raceInLeaderboard) {
        final String oldRaceName = raceInLeaderboard.getRaceColumnName();
        List<RaceInLeaderboardDAO> existingRacesWithoutThisRace = new ArrayList<RaceInLeaderboardDAO>();
        existingRacesWithoutThisRace.addAll(raceColumnList.getList());
        existingRacesWithoutThisRace.remove(raceInLeaderboard);
        final RaceInLeaderboardDialog raceDialog = new RaceInLeaderboardDialog(existingRacesWithoutThisRace,
                raceInLeaderboard, stringConstants, new AsyncCallback<RaceInLeaderboardDAO>() {
                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(final RaceInLeaderboardDAO result) {
                        sailingService.renameLeaderboardColumn(getSelectedLeaderboardName(), oldRaceName,
                                result.getRaceColumnName(), new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                    }

                                    @Override
                                    public void onSuccess(Void v) {
                                        sailingService.updateIsMedalRace(getSelectedLeaderboardName(),
                                                result.getRaceColumnName(), result.isMedalRace(),
                                                new AsyncCallback<Void>() {

                                                    @Override
                                                    public void onFailure(Throwable caught) {
                                                    }

                                                    @Override
                                                    public void onSuccess(Void v) {
                                                        raceInLeaderboard.setMedalRace(result.isMedalRace());
                                                        reloadRaceInLeaderboardRow(oldRaceName, result);
                                                    }
                                                });
                                    }
                                });
                    }

                });
        raceDialog.show();
    }

    private void reloadRaceInLeaderboardRow(String oldRaceName, RaceInLeaderboardDAO raceInLeaderboard) {
        int index = -1;
        for (int i = 0; i < raceColumnList.getList().size(); i++) {
            RaceInLeaderboardDAO race = raceColumnList.getList().get(i);
            if (race.getRaceColumnName().equals(oldRaceName)) {
                index = i;
                break;
            }
        }
        if (index != -1) {
            raceColumnList.getList().set(index, raceInLeaderboard);
        }
        raceColumnList.refresh();
    }

    private void setIsMedalRace(String leaderboardName, final RaceInLeaderboardDAO raceInLeaderboard,
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
        final RaceInLeaderboardDAO raceInLeaderboard = new RaceInLeaderboardDAO();
        final String leaderboardName = getSelectedLeaderboardName();
        final RaceInLeaderboardDialog raceDialog = new RaceInLeaderboardDialog(raceColumnList.getList(),
                raceInLeaderboard, stringConstants, new AsyncCallback<RaceInLeaderboardDAO>() {

                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(final RaceInLeaderboardDAO result) {
                        sailingService.addColumnToLeaderboard(result.getRaceColumnName(), leaderboardName,
                                result.isMedalRace(), new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError("Error trying to add column "
                                                + result.getRaceColumnName() + " to leaderboard " + leaderboardName
                                                + ": " + caught.getMessage());

                                    }

                                    @Override
                                    public void onSuccess(Void v) {
                                        // columnNamesInSelectedLeaderboardListBox.addItem(columnNameAndMedalRace.getA());
                                        raceColumnList.getList().add(result);
                                        // selectedLeaderboard.raceNamesAndMedalRaceAndTracked.put(columnNameAndMedalRace.getA(),
                                        // new Pair<Boolean, Boolean>(/* medal race */ columnNameAndMedalRace.getB(),
                                        // /* tracked */ false));
                                        selectedLeaderboard.addRace(result.getRaceColumnName(), result.isMedalRace(),
                                                false);
                                        selectedLeaderboard.invalidateCompetitorOrdering();
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
        final String leaderboardName = getSelectedLeaderboardName();
        trackedEventsComposite.clearSelection();
        if (leaderboardName != null) {
            sailingService.getLeaderboardByName(leaderboardName, new Date(),
            /* namesOfRacesForWhichToLoadLegDetails */null, new AsyncCallback<LeaderboardDAO>() {
                @Override
                public void onFailure(Throwable caught) {
                    errorReporter.reportError("Error trying to fetch leaderboard " + leaderboardName
                            + " from the server: " + caught.getMessage());
                }

                @Override
                public void onSuccess(LeaderboardDAO result) {
                    selectedLeaderboard = result;
                    raceColumnList.getList().clear();
                    raceColumnList.getList().addAll(result.getRaceInLeaderboardList());
                    selectedLeaderBoardPanel.setVisible(true);
                    trackedRacesCaptionPanel.setVisible(true);
                    selectedLeaderBoardPanel.setCaptionText("Details of leaderboard '" + result.name + "'");
                    addColumnButton.setEnabled(true);
                }
            });
        } else {
            selectedLeaderBoardPanel.setVisible(false);
            trackedRacesCaptionPanel.setVisible(false);
            selectedLeaderboard = null;
            addColumnButton.setEnabled(false);
            leaderboardRaceColumnSelectionChanged();
        }
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        trackedEventsComposite.fillEvents(result);
    }

    @Override
    public void changeTrackingRace(EventNameAndRaceName eventNameAndRaceName, boolean isTracked) {
        for (RaceInLeaderboardDAO race : raceColumnList.getList()) {
            if (race.getRaceColumnName().equals(eventNameAndRaceName.getRaceName())) {
                race.setTrackedRace(isTracked);
            }
        }
        raceColumnList.refresh();
    }

    private void addNewLeaderboard() {
        List<String> leaderboardNames = new ArrayList<String>();
        for (LeaderboardDAO dao : leaderboardList.getList())
            leaderboardNames.add(dao.name);
        LeaderboardCreateDialog dialog = new LeaderboardCreateDialog(Collections.unmodifiableCollection(leaderboardList
                .getList()), stringConstants, errorReporter, new AsyncCallback<LeaderboardDAO>() {
            @Override
            public void onFailure(Throwable arg0) {
            }

            @Override
            public void onSuccess(LeaderboardDAO result) {
                createNewLeaderboard(result);
            }
        });
        dialog.show();
    }

    private void createNewLeaderboard(final LeaderboardDAO newLeaderboard) {
        sailingService.createLeaderboard(newLeaderboard.name, newLeaderboard.discardThresholds,
                new AsyncCallback<LeaderboardDAO>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to create new leaderboard " + newLeaderboard.name
                                + ": " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(LeaderboardDAO result) {
                        leaderboardList.getList().add(result);
                        availableLeaderboardList.add(result);
                        selectedLeaderboard = result;
                        leaderboardSelectionChanged();
                    }
        });
    }

    private void updateLeaderboard(final String oldLeaderboardName, final LeaderboardDAO leaderboardToUdate) {
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
                            LeaderboardDAO dao = leaderboardList.getList().get(i);
                            if (dao.name.equals(oldLeaderboardName)) {
                                leaderboardList.getList().set(i, leaderboardToUdate);
                                availableLeaderboardList.set(i, leaderboardToUdate);
                            }
                        }
                        leaderboardList.refresh();
                    }
                });
    }

    private void removeLeaderboard(final LeaderboardDAO leaderBoard) {
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
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        // if no leaderboard column is selected, ignore the race selection change
        RaceInLeaderboardDAO selectedRaceInLeaderboard = getSelectedRaceInLeaderboard();
        if (selectedRaceInLeaderboard != null) {
            if (selectedRaces.isEmpty()) {
                unlinkRaceColumnFromTrackedRace(selectedRaceInLeaderboard.getRaceColumnName());
            } else {
                linkTrackedRaceToSelectedRaceColumn(selectedRaceInLeaderboard, selectedRaces.iterator().next());
            }
        }
    }

    private void linkTrackedRaceToSelectedRaceColumn(final RaceInLeaderboardDAO selectedRaceInLeaderboard,
            final Triple<EventDAO, RegattaDAO, RaceDAO> selectedRace) {
        sailingService.connectTrackedRaceToLeaderboardColumn(getSelectedLeaderboardName(), selectedRaceInLeaderboard
                .getRaceColumnName(), new EventNameAndRaceName(selectedRace.getA().name, selectedRace.getC().name),
                new AsyncCallback<Boolean>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to link tracked race " + selectedRace.getC().name
                                + " of event " + selectedRace.getA().name + " to race column named "
                                + selectedRaceInLeaderboard.getRaceColumnName() + " of leaderboard "
                                + getSelectedLeaderboardName() + ": " + t.getMessage());
                        trackedEventsComposite.clearSelection();
                    }

                    @Override
                    public void onSuccess(Boolean success) {
                        if (success) {
                            // TODO consider enabling the Unlink button
                            selectedRaceInLeaderboard.setTrackedRace(true);
                            raceColumnList.refresh();
                        }
                    }
                });
    }

    private void clearSelection() {
        if (leaderboardList != null) {
            for (LeaderboardDAO leaderboard : leaderboardList.getList()) {
                tableSelectionModel.setSelected(leaderboard, false);
            }
        }
    }
}