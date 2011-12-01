package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CaptionPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.EventNameAndRaceName;
import com.sap.sailing.gwt.ui.shared.LeaderboardDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;

public class LeaderboardConfigPanel extends FormPanel implements EventDisplayer, RaceSelectionChangeListener {

    // AXEL: DON'T DELETE!!!
    // private static final int MAX_NUMBER_OF_DISCARDED_RESULTS = 4;

    private final TrackedEventsComposite trackedEventsComposite;

    private final StringConstants stringConstants;

    private final SailingServiceAsync sailingService;

    private final ListDataProvider<LeaderboardDAO> leaderboardList;

    private final ListDataProvider<String> raceColumnList;

    private final ErrorReporter errorReporter;

    private final CellTable<LeaderboardDAO> leaderboardTable;

    private final CellTable<String> raceColumnTable;

    private LeaderboardDAO selectedLeaderboard;

    private final Button addColumnButton;

    private final Button columnMoveUpButton;

    private final Button columnMoveDownButton;

    private final Anchor editLeaderboardScoresLink;

    private final CaptionPanel selectedLeaderBoardPanel;
    private final CaptionPanel trackedRacesCaptionPanel;

    final SingleSelectionModel<String> raceTableSelectionModel;

    public LeaderboardConfigPanel(SailingServiceAsync sailingService, AdminConsole adminConsole,
            final ErrorReporter errorReporter, StringConstants theStringConstants) {
        this.stringConstants = theStringConstants;
        this.sailingService = sailingService;
        leaderboardList = new ListDataProvider<LeaderboardDAO>();
        raceColumnList = new ListDataProvider<String>();
        this.errorReporter = errorReporter;

        readAllLeaderbords();

        VerticalPanel mainPanel = new VerticalPanel();
        this.setWidget(mainPanel);

        Label lblLeaderboards = new Label("Leaderboards");
        lblLeaderboards.setStyleName("bold");
        mainPanel.add(lblLeaderboards);

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
                new LeaderboardConfigImagesBarCell());

        leaderboardActionColumn.setFieldUpdater(new FieldUpdater<LeaderboardDAO, String>() {
            @Override
            public void update(int index, LeaderboardDAO object, String value) {
                if ("ACTION_REMOVE".equals(value)) {
                    if (Window.confirm("Do you really want to remove the leaderboard: '" + object.name + "' ?")) {
                        removeLeaderboard(object);
                    }
                } else if ("ACTION_OPEN_BROWSER".equals(value)) {
                    Window.open("/Leaderboard.html?name=" + object.name, "Leaderboard", null);
                } else if ("ACTION_EDIT".equals(value)) {

                    final String oldLeaderboardName = object.name;

                    LeaderboardEditDialog dialog = new LeaderboardEditDialog(object, stringConstants, errorReporter,
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
                }
            }
        });

        AdminConsoleTableResources tableRes = GWT.create(AdminConsoleTableResources.class);
        leaderboardTable = new CellTable<LeaderboardDAO>(/* pageSize */200, tableRes);
        leaderboardTable.addColumn(leaderboardNameColumn, "Name");
        leaderboardTable.addColumn(discardingOptionsColumn, "Discarding");
        leaderboardTable.addColumn(leaderboardActionColumn, "Actions");
        leaderboardTable.setWidth("500px");
        final SingleSelectionModel<LeaderboardDAO> tableSelectionModel = new SingleSelectionModel<LeaderboardDAO>();
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
        trackedEventsComposite.addRaceSelectionChangeListener(this);

        /*
         * discardThresholdLabelsForSelectedLeaderboard = new Label[MAX_NUMBER_OF_DISCARDED_RESULTS];
         * discardThresholdsGrid.setWidget(0, 0, new Label(stringConstants.discarding() + ":"));
         * discardThresholdsGrid.setWidget(1, 0, new Label(stringConstants.startingFromNumberOfRaces() + ":")); for (int
         * i=0; i<MAX_NUMBER_OF_DISCARDED_RESULTS; i++) { discardThresholdsGrid.setWidget(0, i+1, new Label(""+(i+1)));
         * discardThresholdLabelsForSelectedLeaderboard[i] = new Label(); discardThresholdsGrid.setWidget(1, i+1,
         * discardThresholdLabelsForSelectedLeaderboard[i]); }
         */
        editLeaderboardScoresLink = new Anchor(stringConstants.editScores());
        editLeaderboardScoresLink.setEnabled(false);
        vPanel.add(editLeaderboardScoresLink);

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
        TextColumn<String> raceNameColumn = new TextColumn<String>() {
            @Override
            public String getValue(String object) {
                return object;
            }
        };

        TextColumn<String> isMedalRaceColumn = new TextColumn<String>() {
            @Override
            public String getValue(String race) {

                boolean isMedalRace = selectedLeaderboard.raceIsMedalRace(race);
                return isMedalRace ? "Yes" : "No";
            }
        };

        TextColumn<String> isTrackedRaceColumn = new TextColumn<String>() {
            @Override
            public String getValue(String race) {

                boolean isTrackedRace = selectedLeaderboard.raceIsTracked(race);
                return isTrackedRace ? "Yes" : "No";
            }
        };

        ImagesBarColumn<String, LeaderboardRaceConfigImagesBarCell> raceActionColumn = new ImagesBarColumn<String, LeaderboardRaceConfigImagesBarCell>(
                new LeaderboardRaceConfigImagesBarCell());

        raceActionColumn.setFieldUpdater(new FieldUpdater<String, String>() {
            @Override
            public void update(int index, String object, String value) {
                if ("ACTION_REMOVE".equals(value)) {
                    if (Window.confirm("Do you really want to remove the race: '" + object + "' ?")) {
                        removeRaceColumn(object);
                    }
                } else if ("ACTION_EDIT".equals(value)) {
                    editRaceColumnOfLeaderboard();
                } else if ("ACTION_UNLINK".equals(value)) {
                    unlinkRaceColumnFromTrackedRace(object);
                }
            }
        });

        Label lblRaceNamesIn = new Label(stringConstants.races());
        vPanel.add(lblRaceNamesIn);

        raceColumnTable = new CellTable<String>(/* pageSize */200, tableRes);
        raceColumnTable.addColumn(raceNameColumn, "Name");
        raceColumnTable.addColumn(isMedalRaceColumn, "Medal race");
        raceColumnTable.addColumn(isTrackedRaceColumn, "Tracked");
        raceColumnTable.addColumn(raceActionColumn, "Actions");
        raceColumnTable.setWidth("500px");
        raceTableSelectionModel = new SingleSelectionModel<String>();
        raceColumnTable.setSelectionModel(raceTableSelectionModel);

        raceTableSelectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
            public void onSelectionChange(SelectionChangeEvent event) {
                leaderboardRaceColumnSelectionChanged();
            }
        });

        raceColumnList.addDataDisplay(raceColumnTable);

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
                leaderboardList.getList().addAll(leaderboards);
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
                    }
                });
    }

    private void removeRaceColumn(final String raceColumn) {
        sailingService.removeLeaderboardColumn(getSelectedLeaderboardName(), raceColumn, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable t) {
                errorReporter.reportError("Error trying to remove leaderboard race column " + raceColumn
                        + " in leaderboard " + getSelectedLeaderboardName() + ": " + t.getMessage());
            }

            @Override
            public void onSuccess(Void arg0) {
                raceColumnList.getList().remove(raceColumn);
                // selectedLeaderboard.raceNamesAndMedalRaceAndTracked.remove(raceColumn);
                selectedLeaderboard.removeRace(raceColumn);
                selectedLeaderboard.invalidateCompetitorOrdering();
            }
        });
    }

    private void moveSelectedRaceColumnDown() {
        final String selectedRaceColumnName = raceTableSelectionModel.getSelectedObject();

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
        final String selectedRaceColumnName = raceTableSelectionModel.getSelectedObject();
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
        final String selectedRaceColumnName = raceTableSelectionModel.getSelectedObject();

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
        String selectedRaceColumnName = getSelectedRaceColumnName();
        if (selectedRaceColumnName != null) {
            columnMoveUpButton.setEnabled(true);
            columnMoveDownButton.setEnabled(true);
            selectTrackedRaceInRaceTree();
        } else {
            columnMoveUpButton.setEnabled(false);
            columnMoveDownButton.setEnabled(false);
            trackedEventsComposite.clearSelection();
        }
    }

    private void selectTrackedRaceInRaceTree() {
        final String selectedLeaderboardName = getSelectedLeaderboardName();
        final String selectedRaceColumnName = getSelectedRaceColumnName();
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

    private String getSelectedRaceColumnName() {
        return raceTableSelectionModel.getSelectedObject();
    }
    
    private void editRaceColumnOfLeaderboard(){
        final String leaderboardName = getSelectedLeaderboardName();
        final String raceName = getSelectedLeaderboardName();
        boolean raceIsMedalRace = false;
        if(selectedLeaderboard.getRaceList().contains(raceName)){
            raceIsMedalRace = selectedLeaderboard.raceIsMedalRace(raceName);
        }
        Pair<String, Boolean> raceDaoAndIsMedalRace = new Pair<String, Boolean>(raceName,
                raceIsMedalRace);
        final RaceDialog raceDialog = new RaceDialog(raceDaoAndIsMedalRace, stringConstants,
                new AsyncCallback<Pair<String, Boolean>>() {

                    @Override
                    public void onFailure(Throwable caught) {}

                    @Override
                    public void onSuccess(Pair<String, Boolean> result) {
                        sailingService.editLeaderboardColumnName(raceName, result.getA(), leaderboardName, result.getB(), new AsyncCallback<Void>() {

                            @Override
                            public void onFailure(Throwable caught) {}

                            @Override
                            public void onSuccess(Void result) {
                                // TODO liste aktualiesieren
                            }
                        });
                    }
            
        });
        raceDialog.show();
    }

    private void addRaceColumnToLeaderboard() {
        final String leaderboardName = getSelectedLeaderboardName();
        Pair<String, Boolean> raceDaoAndIsMedalRace = new Pair<String, Boolean>("",
                false);
        final RaceDialog raceDialog = new RaceDialog(raceDaoAndIsMedalRace, stringConstants,
                new AsyncCallback<Pair<String, Boolean>>() {

                    @Override
                    public void onFailure(Throwable caught) {
                    }

                    @Override
                    public void onSuccess(final Pair<String, Boolean> result) {
                        sailingService.addColumnToLeaderboard(result.getA(), leaderboardName, result.getB(),
                                new AsyncCallback<Void>() {

                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError("Error trying to add column " + result.getA()
                                                + " to leaderboard " + leaderboardName + ": " + caught.getMessage());

                                    }

                                    @Override
                                    public void onSuccess(Void v) {
                                        // columnNamesInSelectedLeaderboardListBox.addItem(columnNameAndMedalRace.getA());
                                        raceColumnList.getList().add(result.getA());
                                        // selectedLeaderboard.raceNamesAndMedalRaceAndTracked.put(columnNameAndMedalRace.getA(),
                                        // new Pair<Boolean, Boolean>(/* medal race */ columnNameAndMedalRace.getB(),
                                        // /* tracked */ false));
                                        selectedLeaderboard.addRace(result.getA(), result.getB(), false);
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
                    for (String race : result.getRaceList())
                        raceColumnList.getList().add(race);

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

    private void addNewLeaderboard() {

        List<String> leaderboardNames = new ArrayList<String>();
        for (LeaderboardDAO dao : leaderboardList.getList())
            leaderboardNames.add(dao.name);

        LeaderboardCreateDialog dialog = new LeaderboardCreateDialog(
                Collections.unmodifiableCollection(leaderboardNames), stringConstants, errorReporter,
                new AsyncCallback<LeaderboardDAO>() {
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
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to create new leaderboard " + newLeaderboard.name
                                + ": " + t.getMessage());
                    }

                    @Override
                    public void onSuccess(Void result) {

                        sailingService.getLeaderboardByName(newLeaderboard.name, new Date(),
                        /* namesOfRacesForWhichToLoadLegDetails */null, new AsyncCallback<LeaderboardDAO>() {
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError("Error trying to fetch the new leaderboard "
                                        + newLeaderboard.name + " from the server: " + caught.getMessage());
                            }

                            @Override
                            public void onSuccess(LeaderboardDAO result) {

                                leaderboardList.getList().add(result);
                                selectedLeaderboard = result;
                                leaderboardSelectionChanged();
                            }
                        });

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

                        final int[] index = new int[1];
                        index[0] = 0;

                        for (LeaderboardDAO dao : leaderboardList.getList()) {
                            if (dao.name.equals(oldLeaderboardName)) {

                                sailingService.getLeaderboardByName(leaderboardToUdate.name, new Date(),
                                /* namesOfRacesForWhichToLoadLegDetails */null, new AsyncCallback<LeaderboardDAO>() {
                                    @Override
                                    public void onFailure(Throwable caught) {
                                        errorReporter.reportError("Error trying to fetch the new leaderboard "
                                                + leaderboardToUdate.name + " from the server: " + caught.getMessage());
                                    }

                                    @Override
                                    public void onSuccess(LeaderboardDAO result) {
                                        leaderboardList.getList().set(index[0], result);
                                    }
                                });
                            }
                            index[0] = index[0] + 1;
                        }
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

                if (selectedLeaderboard != null && selectedLeaderboard.name.equals(leaderBoard.name)) {
                    selectedLeaderboard = null;
                    leaderboardSelectionChanged();
                }
            }
        });
    }

    @Override
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        if (selectedRaces.isEmpty()) {
            // unlinkRaceColumnFromTrackedRaceButton.setEnabled(false);
        } else {
            if (getSelectedRaceColumnName() != null) {
                linkTrackedRaceForRaceColumn(selectedRaces.iterator().next());
            } else {
                // unlinkRaceColumnFromTrackedRaceButton.setEnabled(false);
            }
        }
    }

    private void linkTrackedRaceForRaceColumn(final Triple<EventDAO, RegattaDAO, RaceDAO> selectedRace) {
        sailingService.connectTrackedRaceToLeaderboardColumn(getSelectedLeaderboardName(), getSelectedRaceColumnName(),
                new EventNameAndRaceName(selectedRace.getA().name, selectedRace.getC().name),
                new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable t) {
                        errorReporter.reportError("Error trying to link tracked race " + selectedRace.getC().name
                                + " of event " + selectedRace.getA().name + " to race column named "
                                + getSelectedRaceColumnName() + " of leaderboard " + getSelectedLeaderboardName()
                                + ": " + t.getMessage());
                        trackedEventsComposite.clearSelection();
                    }

                    @Override
                    public void onSuccess(Void arg0) {
                        // unlinkRaceColumnFromTrackedRaceButton.setEnabled(true);
                    }
                });
    }
}