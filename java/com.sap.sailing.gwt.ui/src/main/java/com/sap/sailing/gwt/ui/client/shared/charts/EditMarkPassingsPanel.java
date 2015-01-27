package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.dto.LeaderboardDTO;
import com.sap.sailing.domain.common.dto.RaceColumnDTO;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.components.Component;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialogComponent;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncAction;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;

public class EditMarkPassingsPanel extends FlexTable implements Component<Void>, RaceSelectionChangeListener,
        CompetitorSelectionChangeListener {

    private static class AnchorCell extends AbstractCell<SafeHtml> {
        @Override
        public void render(com.google.gwt.cell.client.Cell.Context context, SafeHtml safeHtml, SafeHtmlBuilder sb) {
            sb.append(safeHtml);
        }
    }

    private final SailingServiceAsync sailingService;
    private RegattaAndRaceIdentifier raceIdentifier;
    private final AsyncActionsExecutor asyncExecutor;
    private final ErrorReporter errorReporter;
    private final CompetitorSelectionProvider competitorSelectionModel;
    private String leaderboardName;
    private RaceColumnDTO column;

    private CompetitorDTO competitor;
    private Map<Integer, Date> currentCompetitorEdits = new HashMap<>();
    private Integer zeroBasedIndexOfFirstSuppressedWaypoint;

    private final CellTable<Util.Pair<Integer, Date>> wayPointSelectionTable;
    private final ListDataProvider<Util.Pair<Integer, Date>> waypointList;
    private final SingleSelectionModel<Util.Pair<Integer, Date>> waypointSelectionModel;
    private List<WaypointDTO> currentWaypoints;

    private final Button setTimeAsMarkPassingsButton;
    private final Button removeFixedMarkPassingsButton;
    private final Button suppressPassingsButton;
    private final Button removeSuppressedPassingButton;

    public EditMarkPassingsPanel(final SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            final RegattaAndRaceIdentifier raceIdentifier, StringMessages stringMessages,
            final CompetitorSelectionProvider competitorSelectionModel, final ErrorReporter errorReporter,
            final Timer timer) {
        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.asyncExecutor = asyncActionsExecutor;
        this.errorReporter = errorReporter;
        this.competitorSelectionModel = competitorSelectionModel;
        competitorSelectionModel.addCompetitorSelectionChangeListener(this);

        setVisible(false);

        // Waypoint list
        currentWaypoints = new ArrayList<>();
        waypointList = new ListDataProvider<>();
        waypointSelectionModel = new SingleSelectionModel<Util.Pair<Integer, Date>>();
        waypointSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Pair<Integer, Date> selectedObject = waypointSelectionModel.getSelectedObject();
                Date timePoint = selectedObject.getB();
                if (timePoint != null) {
                    timer.setTime(timePoint.getTime());
                }
                enableButtons();
            }
        });
        wayPointSelectionTable = new CellTable<Util.Pair<Integer, Date>>();
        wayPointSelectionTable.addColumn(new Column<Util.Pair<Integer, Date>, SafeHtml>(new AnchorCell()) {
            @Override
            public SafeHtml getValue(final Util.Pair<Integer, Date> object) {
                return new SafeHtml() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String asString() {
                        return currentWaypoints.get(object.getA()).getName();
                    }
                };
            }
        }, "Waypoint");
        wayPointSelectionTable.addColumn(new Column<Util.Pair<Integer, Date>, SafeHtml>(new AnchorCell()) {
            @Override
            public SafeHtml getValue(final Pair<Integer, Date> object) {
                final Date date;
                date = object.getB();
                return new SafeHtml() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String asString() {
                        // TODO this is really unclean (Problem: no
                        // calendar)
                        String string = "";
                        if (date != null) {
                            string = date.toString();
                            string = string.substring(10, 20);
                            if (currentCompetitorEdits.containsKey(object.getA())) {
                                string = string + " (f)";
                            }
                        } else if (zeroBasedIndexOfFirstSuppressedWaypoint != null
                                && !(object.getA() < zeroBasedIndexOfFirstSuppressedWaypoint)) {
                            string = "(s)";
                        }
                        return string;
                    }
                };
            }
        }, "Mark passing");

        waypointList.addDataDisplay(wayPointSelectionTable);
        wayPointSelectionTable.setSelectionModel(waypointSelectionModel);

        // Buttons for fixing
        removeFixedMarkPassingsButton = new Button("Remove fixed mark passing");
        removeFixedMarkPassingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                asyncExecutor.execute(new AsyncAction<Void>() {
                    @Override
                    public void execute(AsyncCallback<Void> callback) {
                        sailingService.updateFixedMarkPassing(leaderboardName, column, column.getFleet(raceIdentifier),
                                waypointSelectionModel.getSelectedObject().getA(), null, competitor, callback);
                    }
                }, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error removing fixed mark passing");
                    }

                    @Override
                    public void onSuccess(Void result) {
                        refillList();
                    }
                });
            }
        });
        setTimeAsMarkPassingsButton = new Button("Set time as mark passing");
        setTimeAsMarkPassingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                asyncExecutor.execute(new AsyncAction<Void>() {
                    @Override
                    public void execute(AsyncCallback<Void> callback) {
                        sailingService.updateFixedMarkPassing(leaderboardName, column, column.getFleet(raceIdentifier),
                                waypointSelectionModel.getSelectedObject().getA(), timer.getTime(), competitor,
                                callback);
                    }
                }, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error setting fixed mark passing");
                    }

                    @Override
                    public void onSuccess(Void result) {
                        refillList();
                    }
                });
            }
        });

        // Button for suppressing
        suppressPassingsButton = new Button("Suppress after selected");
        suppressPassingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                asyncExecutor.execute(new AsyncAction<Void>() {
                    @Override
                    public void execute(AsyncCallback<Void> callback) {
                        sailingService.updateSuppressedMarkPassings(leaderboardName, column,
                                column.getFleet(raceIdentifier), waypointSelectionModel.getSelectedObject().getA(),
                                competitor, callback);
                    }
                }, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error suppressing mark passings");
                    }

                    @Override
                    public void onSuccess(Void result) {
                        refillList();
                    }
                });
            }
        });

        removeSuppressedPassingButton = new Button("Remove suppressed passing");
        removeSuppressedPassingButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                asyncExecutor.execute(new AsyncAction<Void>() {
                    @Override
                    public void execute(AsyncCallback<Void> callback) {
                        sailingService.updateSuppressedMarkPassings(leaderboardName, column,
                                column.getFleet(raceIdentifier), null, competitor, callback);
                    }
                }, new AsyncCallback<Void>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error suppressing mark passings");
                    }

                    @Override
                    public void onSuccess(Void result) {
                        refillList();
                    }
                });
            }
        });

        refreshWaypoints();
        setWidget(1, 0, wayPointSelectionTable);
        setWidget(2, 0, setTimeAsMarkPassingsButton);
        setWidget(2, 1, removeFixedMarkPassingsButton);
        setWidget(3, 0, suppressPassingsButton);
        setWidget(3, 1, removeSuppressedPassingButton);
        enableButtons();
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        processCompetitorSelectionChange();
    }

    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        processCompetitorSelectionChange();
    }

    private void processCompetitorSelectionChange() {
        waypointSelectionModel.clear();
        if (Util.size(competitorSelectionModel.getSelectedCompetitors()) == 1) {
            refillList();
        } else {
            disableEditing();
        }
    }

    private void disableEditing() {
        setVisible(false);
        waypointList.setList(new ArrayList<Util.Pair<Integer, Date>>());
        clearInfo();
    }

    private void refillList() {
        clearInfo();

        competitor = competitorSelectionModel.getSelectedCompetitors().iterator().next();
        
        // Get current mark passings
        asyncExecutor.execute(new AsyncAction<Map<Integer, Date>>() {
            @Override
            public void execute(AsyncCallback<Map<Integer, Date>> callback) {
                sailingService.getCompetitorMarkPassings(raceIdentifier, competitor, callback);
            }
        }, new AsyncCallback<Map<Integer, Date>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error obtaining mark passings");
            }

            @Override
            public void onSuccess(Map<Integer, Date> result) {
                List<Util.Pair<Integer, Date>> newMarkPassings = new ArrayList<>();
                for (WaypointDTO waypoint : currentWaypoints) {
                    int index = currentWaypoints.indexOf(waypoint);
                    newMarkPassings.add(new Util.Pair<Integer, Date>(index, result.get(index)));
                }
                waypointList.setList(newMarkPassings);
                wayPointSelectionTable.redraw();
            }
        });
        
        // Get current edits
        
        asyncExecutor.execute(new AsyncAction<Map<Integer, Date>>() {
            @Override
            public void execute(AsyncCallback<Map<Integer, Date>> callback) {
                sailingService.getCompetitorRaceLogMarkPassingData(leaderboardName, column,
                        column.getFleet(raceIdentifier), competitor, callback);

            }
        }, new AsyncCallback<Map<Integer, Date>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error retrieving race log mark passing data");
            }

            @Override
            public void onSuccess(Map<Integer, Date> result) {
                for (Entry<Integer, Date> data : result.entrySet()) {
                    if (data.getValue() == null) {
                        zeroBasedIndexOfFirstSuppressedWaypoint = data.getKey();
                    } else {
                        currentCompetitorEdits.put(data.getKey(), data.getValue());
                    }
                }
                enableButtons();
                wayPointSelectionTable.redraw();
            }
        });
    }

    private void clearInfo() {
        currentCompetitorEdits.clear();
        competitor = null;
        zeroBasedIndexOfFirstSuppressedWaypoint = null;
    }

    private void refreshWaypoints() {
        asyncExecutor.execute(new AsyncAction<RaceCourseDTO>() {
            @Override
            public void execute(AsyncCallback<RaceCourseDTO> callback) {
                sailingService.getRaceCourse(raceIdentifier, new Date(), callback);
            }
        }, new AsyncCallback<RaceCourseDTO>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error obtaining course");
            }

            @Override
            public void onSuccess(RaceCourseDTO result) {
                currentWaypoints = result.waypoints;
            }
        });
    }

    private void enableButtons() {
        setTimeAsMarkPassingsButton.setEnabled(false);
        removeFixedMarkPassingsButton.setEnabled(false);
        suppressPassingsButton.setEnabled(false);
        removeSuppressedPassingButton.setEnabled(false);
        if (Util.size(competitorSelectionModel.getSelectedCompetitors()) == 1) {
            if (zeroBasedIndexOfFirstSuppressedWaypoint != null) {
                removeSuppressedPassingButton.setEnabled(true);
            }
            Pair<Integer, Date> selectedWaypoint = waypointSelectionModel.getSelectedObject();
            if (selectedWaypoint != null) {
                setTimeAsMarkPassingsButton.setEnabled(true);
                suppressPassingsButton.setEnabled(true);
                if(currentCompetitorEdits.containsKey(selectedWaypoint.getA())){
                    removeFixedMarkPassingsButton.setEnabled(true);
                }
            }
        }
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        raceIdentifier = selectedRaces.iterator().next();
        refreshWaypoints();
    }

    public void setLeaderboardNameAndColumn(LeaderboardDTO leaderboard) {
        if (leaderboard != null) {
            leaderboardName = leaderboard.name;
            for (RaceColumnDTO columnDTO : leaderboard.getRaceList()) {
                if (columnDTO.containsRace(raceIdentifier)) {
                    column = columnDTO;
                }
            }
        }
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    @Override
    public boolean hasSettings() {
        return false;
    }

    @Override
    public SettingsDialogComponent<Void> getSettingsDialogComponent() {
        return null;
    }

    @Override
    public void updateSettings(Void newSettings) {
    }

    @Override
    public String getLocalizedShortName() {
        return null;
    }

    @Override
    public String getDependentCssClassName() {
        return null;
    }

    @Override
    public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet,
            FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
    }

    @Override
    public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {
    }

}
