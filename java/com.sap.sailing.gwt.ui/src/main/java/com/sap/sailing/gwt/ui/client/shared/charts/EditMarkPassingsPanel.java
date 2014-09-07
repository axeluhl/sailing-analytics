package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IntegerBox;
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
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RaceCourseDTO;
import com.sap.sailing.gwt.ui.shared.WaypointDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.async.AsyncAction;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.Timer;

public class EditMarkPassingsPanel extends FlexTable implements RaceSelectionChangeListener, CompetitorSelectionChangeListener {

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
    private LeaderboardDTO leaderboard;
    private RaceColumnDTO column;

    private Map<Integer, Date> currentCompetitorEdits = new HashMap<>();
    private Map<Integer, Date> currentCompetitorChanges = new HashMap<>();

    private final Button editMarkPassingsButton;

    private final Button setTimeAsMarkPassingsButton;
    private final Button removeSetMarkPassingsButton;
    private final CellTable<Util.Pair<WaypointDTO, Date>> wayPointSelectionTable;
    private final ListDataProvider<Util.Pair<WaypointDTO, Date>> waypointList;
    private final SingleSelectionModel<Util.Pair<WaypointDTO, Date>> waypointSelectionModel;
    private List<WaypointDTO> currentWaypoints;

    private final IntegerBox suppressMarkPassings;
    private final CheckBox suppressMarkPassingsCheckBox;

    private final Button svButton;
    private final Button closeButton;

    public EditMarkPassingsPanel(final SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            final RegattaAndRaceIdentifier raceIdentifier, StringMessages stringMessages, final Button editMarkPassingsButton,
            final CompetitorSelectionProvider competitorSelectionModel, final ErrorReporter errorReporter, final Timer timer) {

        this.sailingService = sailingService;
        this.raceIdentifier = raceIdentifier;
        this.asyncExecutor = asyncActionsExecutor;
        this.errorReporter = errorReporter;

        this.editMarkPassingsButton = editMarkPassingsButton;
        this.competitorSelectionModel = competitorSelectionModel;

        editMarkPassingsButton.setEnabled(false);

        competitorSelectionModel.addCompetitorSelectionChangeListener(this);
        editMarkPassingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setVisible(true);
                editMarkPassingsButton.setVisible(false);
            }
        });

        removeSetMarkPassingsButton = new Button("Remove fixed mark passing");
        removeSetMarkPassingsButton.setEnabled(false);
        removeSetMarkPassingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                currentCompetitorChanges.put(currentWaypoints.indexOf(waypointSelectionModel.getSelectedObject().getA()), null);
                wayPointSelectionTable.redraw();
            }
        });
        setTimeAsMarkPassingsButton = new Button("Set time as mark passing");
        setTimeAsMarkPassingsButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Pair<WaypointDTO, Date> selectedObject = waypointSelectionModel.getSelectedObject();
                currentCompetitorChanges.put(currentWaypoints.indexOf(selectedObject.getA()), timer.getTime());
                wayPointSelectionTable.redraw();
            }
        });
        currentWaypoints = new ArrayList<>();
        waypointList = new ListDataProvider<>();
        waypointSelectionModel = new SingleSelectionModel<Util.Pair<WaypointDTO, Date>>();
        waypointSelectionModel.addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                Pair<WaypointDTO, Date> selectedObject = waypointSelectionModel.getSelectedObject();
                WaypointDTO waypoint = selectedObject.getA();
                removeSetMarkPassingsButton.setEnabled(currentCompetitorEdits.get(waypoint) != null);
                Date timePoint = selectedObject.getB();
                if (timePoint != null) {
                    timer.setTime(timePoint.getTime());
                }
            }
        });
        wayPointSelectionTable = new CellTable<Util.Pair<WaypointDTO, Date>>();
        wayPointSelectionTable.addColumn(new Column<Util.Pair<WaypointDTO, Date>, SafeHtml>(new AnchorCell()) {
            @Override
            public SafeHtml getValue(final Util.Pair<WaypointDTO, Date> object) {
                return new SafeHtml() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String asString() {
                        return object.getA().getName();
                    }
                };
            }
        }, "Waypoint");
        wayPointSelectionTable.addColumn(new Column<Util.Pair<WaypointDTO, Date>, SafeHtml>(new AnchorCell()) {
            @Override
            public SafeHtml getValue(final Pair<WaypointDTO, Date> object) {
                final Date date;
                Date editedDate = currentCompetitorChanges.get(currentWaypoints.indexOf(object.getA()));
                if (editedDate != null) {
                    date = editedDate;
                } else {
                    date = object.getB();
                }
                return new SafeHtml() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public String asString() {
                        // TODO this is really unclean (Problem: no calendar)
                        // Oh and time zones are scary
                        String string = date.toString();
                        string = string.substring(10, 20);
                        return string;
                    }
                };
            }
        }, "Mark passing");

        waypointList.addDataDisplay(wayPointSelectionTable);
        wayPointSelectionTable.setSelectionModel(waypointSelectionModel);

        suppressMarkPassings = new IntegerBox();
        suppressMarkPassings.setWidth("1cm");
        suppressMarkPassings.setEnabled(false);
        suppressMarkPassingsCheckBox = new CheckBox("Suppress mark passings starting at Waypoint:");
        suppressMarkPassingsCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                Boolean value = event.getValue();
                suppressMarkPassings.setEnabled(value);
                if (value == false) {
                    suppressMarkPassings.setText("");
                }
            }
        });

        svButton = new Button(stringMessages.save());
        svButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                asyncExecutor.execute(new AsyncAction<Void>() {
                    @Override
                    public void execute(AsyncCallback<Void> callback) {
                        sailingService.updateRaceLogMarkPassingData(leaderboard.name, column, column.getFleet(raceIdentifier),
                                currentCompetitorChanges, suppressMarkPassings.getValue(), competitorSelectionModel
                                        .getSelectedCompetitors().iterator().next(), callback);
                    }

                }, new AsyncCallback<Void>() {

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error submitting new changes");
                    }

                    @Override
                    public void onSuccess(Void result) {
                        refillList();
                    }

                });
            }
        });
        closeButton = new Button(stringMessages.close());
        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                setVisible(false);
                editMarkPassingsButton.setVisible(true);
            }
        });
        refreshWaypoints();

        setWidget(1, 0, setTimeAsMarkPassingsButton);
        setWidget(2, 0, wayPointSelectionTable);
        setWidget(3, 0, removeSetMarkPassingsButton);

        setWidget(4, 0, suppressMarkPassingsCheckBox);
        setWidget(4, 1, suppressMarkPassings);

        setWidget(6, 1, svButton);
        setWidget(6, 2, closeButton);
        setVisible(false);

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
        if (Util.size(competitorSelectionModel.getSelectedCompetitors()) == 1) {
            refillList();
        } else {
            disableEditing();
        }
    }

    private void disableEditing() {
        // TODO clear all current info
        currentCompetitorEdits.clear();
        suppressMarkPassingsCheckBox.setValue(false);
        waypointList.setList(new ArrayList<Util.Pair<WaypointDTO, Date>>());
        editMarkPassingsButton.setEnabled(false);
    }

    private void refillList() {
        editMarkPassingsButton.setEnabled(true);
        removeSetMarkPassingsButton.setEnabled(false);
        final CompetitorDTO competitor = competitorSelectionModel.getSelectedCompetitors().iterator().next();
        currentCompetitorEdits = new HashMap<>();
        suppressMarkPassingsCheckBox.setValue(false);
        suppressMarkPassings.setText("");

        // Get current edits
        asyncExecutor.execute(new AsyncAction<Map<Integer, Date>>() {
            @Override
            public void execute(AsyncCallback<Map<Integer, Date>> callback) {
                sailingService.getCompetitorRaceLogMarkPassingData(leaderboard.name, column, column.getFleet(raceIdentifier), competitor,
                        callback);

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
                        if (data.getValue() == null) {
                            setSuppressedWaypoint(data.getKey());
                        } else {
                            currentCompetitorEdits.put(data.getKey(), data.getValue());
                        }
                    }
                }
            }
        });

        // Get current mark passings
        asyncExecutor.execute(new AsyncAction<Set<Pair<String, Date>>>() {
            @Override
            public void execute(AsyncCallback<Set<Pair<String, Date>>> callback) {
                sailingService.getCompetitorMarkPassings(raceIdentifier, competitor, callback);
            }
        }, new AsyncCallback<Set<Util.Pair<String, Date>>>() {
            @Override
            public void onFailure(Throwable caught) {
                errorReporter.reportError("Error obtaining mark passings", /* silent Mode */true);
            }

            @Override
            public void onSuccess(Set<Util.Pair<String, Date>> result) {
                Set<Util.Pair<String, Date>> sortedPassings = new TreeSet<>(new Comparator<Util.Pair<String, Date>>() {
                    @Override
                    public int compare(Pair<String, Date> o1, Pair<String, Date> o2) {
                        return o1.getB().compareTo(o2.getB());
                    }
                });
                sortedPassings.addAll(result);
                List<Util.Pair<WaypointDTO, Date>> newMarkPassings = new ArrayList<>();
                for (WaypointDTO waypoint : currentWaypoints) {
                    Util.Pair<String, Date> toAdd = null;
                    for (Iterator<Util.Pair<String, Date>> it = sortedPassings.iterator(); it.hasNext() && toAdd == null;) {
                        Util.Pair<String, Date> markPassing = it.next();
                        if (markPassing.getA().equals(waypoint.getName())) {
                            toAdd = markPassing;
                        }
                    }
                    if (toAdd != null) {
                        newMarkPassings.add(new Util.Pair<WaypointDTO, Date>(waypoint, toAdd.getB()));
                        sortedPassings.remove(toAdd);
                    }
                }
                waypointList.setList(newMarkPassings);
            }
        });
    }

    private void setSuppressedWaypoint(Integer oneBasedIndex) {
        suppressMarkPassingsCheckBox.setValue(true);
        suppressMarkPassings.setValue(oneBasedIndex);
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
                errorReporter.reportError("Error obtaining course", true);
            }

            @Override
            public void onSuccess(RaceCourseDTO result) {
                currentWaypoints = result.waypoints;
            }
        });
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        raceIdentifier = selectedRaces.iterator().next();
        refreshWaypoints();
    }

    @Override
    public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
    }

    @Override
    public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet,
            FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {
    }

    public void setLeaderboard(LeaderboardDTO leaderboard) {
        this.leaderboard = leaderboard;
        for (RaceColumnDTO columnDTO : leaderboard.getRaceList()) {
            if (columnDTO.containsRace(raceIdentifier)) {
                column = columnDTO;
            }
        }
    }
}
