package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTree;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.Pair;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDAO;

public class WindPanel extends FormPanel implements EventDisplayer, RaceSelectionProvider, WindShower {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private EventRefresher eventRefresher;
    private final Grid grid;
    private final StringConstants stringConstants;
    private final WindSettingPanel windSettingPanel;
    private Pair<EventDAO, RaceDAO> selectedEventAndRace;
    private ColumnSortList columnSortList;
    private final TextColumn<WindDAO> timeColumn;
    private final TextColumn<WindDAO> speedInKnotsColumn;
    private final TextColumn<WindDAO> windDirectionInDegColumn;
    private final TextColumn<WindDAO> dampenedSpeedInKnotsColumn;
    private final TextColumn<WindDAO> dampenedWindDirectionInDegColumn;

    public WindPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter, EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.eventRefresher = eventRefresher;
        this.stringConstants = stringConstants;
        timeColumn = new TextColumn<WindDAO>() {
            @Override
            public String getValue(WindDAO object) {
                return new Date(object.timepoint).toString();
            }
        };
        speedInKnotsColumn = new TextColumn<WindDAO>() {
            @Override
            public String getValue(WindDAO object) {
                return ""+object.trueWindSpeedInKnots;
            }
        };
        windDirectionInDegColumn = new TextColumn<WindDAO>() {
            @Override
            public String getValue(WindDAO object) {
                return ""+object.trueWindFromDeg;
            }
        };
        dampenedSpeedInKnotsColumn = new TextColumn<WindDAO>() {
            @Override
            public String getValue(WindDAO object) {
                return ""+object.dampenedTrueWindSpeedInKnots;
            }
        };
        dampenedWindDirectionInDegColumn = new TextColumn<WindDAO>() {
            @Override
            public String getValue(WindDAO object) {
                return ""+object.dampenedTrueWindFromDeg;
            }
        };
        grid = new Grid(2, 2); // first row: event/race selection; second row: wind display
        Button btnRefresh = new Button(stringConstants.refresh());
        btnRefresh.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                WindPanel.this.eventRefresher.fillEvents();
            }
        });
        grid.setWidget(0, 1, btnRefresh);
        windSettingPanel = new WindSettingPanel(sailingService, errorReporter, this, this);
        grid.setWidget(1, 1, windSettingPanel);
        grid.getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
        this.setWidget(grid);
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        grid.setWidget(0, 0, null); // remove tree view; important in case event list is empty
        grid.getCellFormatter().setVerticalAlignment(0, 1, HasVerticalAlignment.ALIGN_TOP);
        if (!result.isEmpty()) {
            final ListDataProvider<EventDAO> eventsList = new ListDataProvider<EventDAO>(result);
            final TrackedEventsTreeModel trackedEventsModel = new TrackedEventsTreeModel(eventsList, /* multiSelection */ false);
            CellTree eventsCellTree = new CellTree(trackedEventsModel, /* root */null);
            grid.setWidget(0, 0, eventsCellTree);

            trackedEventsModel.getSelectionModel().addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
                @Override
                public void onSelectionChange(SelectionChangeEvent selectionChangeEvent) {
                    selectedEventAndRace = null;
                    for (EventDAO event : eventsList.getList()) {
                        for (RegattaDAO regatta : event.regattas) {
                            for (RaceDAO race : regatta.races) {
                                if (trackedEventsModel.getSelectionModel().isSelected(race)) {
                                    selectedEventAndRace = new Pair<EventDAO, RaceDAO>(event, race);
                                    if (race.currentlyTracked) {
                                        showWind(event, race);
                                    } else {
                                        clearWindDisplay(); // no wind known for untracked race
                                    }
                                }
                            }
                        }
                    }
                    if (selectedEventAndRace == null) {
                        clearWindDisplay();
                    }
                }
            });
        }
    }

    @Override
    public void showWind(final EventDAO event, final RaceDAO race) {
        sailingService.getWindInfo(event.name, race.name,
                // TODO what about the time interval?
                                  new Date(/* fromAsMilliseconds */0), new Date(/* toAsMilliseconds */),
                new AsyncCallback<WindInfoForRaceDAO>() {
                    @Override
                    public void onSuccess(WindInfoForRaceDAO result) {
                        showWindForRace(result);
                        windSettingPanel.setEnabled(true);
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError("Error fetching wind information for race " + race.name + ": "
                                + caught.getMessage());
                    }
                });
    }
    
    private void clearWindDisplay() {
        grid.setWidget(1, 0, null);
        windSettingPanel.setEnabled(false);
    }

    private void showWindForRace(WindInfoForRaceDAO result) {
        grid.setWidget(1, 0, null);
        VerticalPanel windDisplay = new VerticalPanel();
        grid.setWidget(1, 0, windDisplay);
        for (Map.Entry<String, WindTrackInfoDAO> e : result.windTrackInfoByWindSourceName.entrySet()) {
            Label windSourceLabel = new Label(stringConstants.windSource()+": "+e.getKey()+
                    ", "+stringConstants.dampeningInterval()+" "+e.getValue().dampeningIntervalInMilliseconds+"ms");
            windDisplay.add(windSourceLabel);
            timeColumn.setSortable(true);
            speedInKnotsColumn.setSortable(true);
            windDirectionInDegColumn.setSortable(true);
            dampenedSpeedInKnotsColumn.setSortable(true);
            dampenedWindDirectionInDegColumn.setSortable(true);
            CellTable<WindDAO> windTable = new CellTable<WindDAO>(/* pageSize */ 100);
            windTable.addColumn(timeColumn, "Time");
            windTable.addColumn(speedInKnotsColumn, "Speed (kn)");
            windTable.addColumn(windDirectionInDegColumn, "From (deg)");
            windTable.addColumn(dampenedSpeedInKnotsColumn, "Avg Speed (kn)");
            windTable.addColumn(dampenedWindDirectionInDegColumn, "Avg From (deg)");
            ListDataProvider<WindDAO> windList = new ListDataProvider<WindDAO>(e.getValue().windFixes);
            windList.addDataDisplay(windTable);
            Handler columnSortHandler = getWindTableColumnSortHandler(windList.getList(), timeColumn,
                    speedInKnotsColumn, windDirectionInDegColumn, dampenedSpeedInKnotsColumn, dampenedWindDirectionInDegColumn);
            windTable.addColumnSortHandler(columnSortHandler);
            List<ColumnSortInfo> sortedColumnList = new ArrayList<ColumnSortInfo>();
            if (columnSortList != null) {
                for (int i=0; i<columnSortList.size(); i++) {
                    sortedColumnList.add(columnSortList.get(i));
                }
            }
            columnSortList = windTable.getColumnSortList();
            if (sortedColumnList.isEmpty()) {
                columnSortList.push(timeColumn);
            } else {
                for (ColumnSortInfo sortInfo : sortedColumnList) {
                    columnSortList.push(sortInfo);
                }
                ColumnSortEvent.fire(windTable, columnSortList);
            }
            windDisplay.add(windTable);
        }
    }
    
    private Handler getWindTableColumnSortHandler(List<WindDAO> list, TextColumn<WindDAO> timeColumn,
            TextColumn<WindDAO> speedInKnotsColumn, TextColumn<WindDAO> windDirectionInDegColumn,
            TextColumn<WindDAO> dampenedSpeedInKnotsColumn, TextColumn<WindDAO> dampenedWindDirectionInDegColumn) {
        ListHandler<WindDAO> result = new ListHandler<WindDAO>(list);
        result.setComparator(timeColumn, new Comparator<WindDAO>() {
            @Override
            public int compare(WindDAO o1, WindDAO o2) {
                return o1.timepoint < o2.timepoint ? -1 : o1.timepoint == o2.timepoint ? 0 : 1;
            }
        });
        result.setComparator(speedInKnotsColumn, new Comparator<WindDAO>() {
            @Override
            public int compare(WindDAO o1, WindDAO o2) {
                return o1.trueWindSpeedInKnots < o2.trueWindSpeedInKnots ? -1 :
                    o1.trueWindSpeedInKnots == o2.trueWindSpeedInKnots ? 0 : 1;
            }
        });
        result.setComparator(windDirectionInDegColumn, new Comparator<WindDAO>() {
            @Override
            public int compare(WindDAO o1, WindDAO o2) {
                return o1.trueWindFromDeg < o2.trueWindFromDeg ? -1 :
                    o1.trueWindFromDeg == o2.trueWindFromDeg ? 0 : 1;
            }
        });
        result.setComparator(dampenedSpeedInKnotsColumn, new Comparator<WindDAO>() {
            @Override
            public int compare(WindDAO o1, WindDAO o2) {
                return o1.dampenedTrueWindSpeedInKnots < o2.dampenedTrueWindSpeedInKnots ? -1 :
                    o1.dampenedTrueWindSpeedInKnots == o2.dampenedTrueWindSpeedInKnots ? 0 : 1;
            }
        });
        result.setComparator(dampenedWindDirectionInDegColumn, new Comparator<WindDAO>() {
            @Override
            public int compare(WindDAO o1, WindDAO o2) {
                return o1.dampenedTrueWindFromDeg < o2.dampenedTrueWindFromDeg ? -1 :
                    o1.dampenedTrueWindFromDeg == o2.dampenedTrueWindFromDeg ? 0 : 1;
            }
        });
        return result;
    }

    @Override
    public Pair<EventDAO, RaceDAO> getSelectedEventAndRace() {
        return selectedEventAndRace;
    }

}
