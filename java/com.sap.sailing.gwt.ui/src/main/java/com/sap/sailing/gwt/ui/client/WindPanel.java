package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortEvent.Handler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.IdentityColumn;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.EventNameAndRaceName;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDAO;

public class WindPanel extends FormPanel implements EventDisplayer, RaceSelectionProvider, WindShower {
    private static final String WEB_WIND_SOURCE_NAME = "WEB";
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private EventRefresher eventRefresher;
    private final Grid grid;
    private final StringConstants stringConstants;
    private final WindSettingPanel windSettingPanel;
    private ColumnSortList columnSortList;
    private final IdentityColumn<WindDAO> removeColumn;
    private final TextColumn<WindDAO> timeColumn;
    private final TextColumn<WindDAO> speedInKnotsColumn;
    private final TextColumn<WindDAO> windDirectionInDegColumn;
    private final TextColumn<WindDAO> dampenedSpeedInKnotsColumn;
    private final TextColumn<WindDAO> dampenedWindDirectionInDegColumn;
    private final Set<RaceSelectionChangeListener> raceSelectionChangeListeners;
    private final RaceTreeView trackedRacesTree;
    private final ListBox windSourceSelection;
    private final Map<String, ListDataProvider<WindDAO>> windLists;
    private final CheckBox showEstimatedWindBox;

    public WindPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter, EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.eventRefresher = eventRefresher;
        this.stringConstants = stringConstants;
        raceSelectionChangeListeners = new HashSet<RaceSelectionChangeListener>();
        windLists = new HashMap<String, ListDataProvider<WindDAO>>();
        windSourceSelection = new ListBox();
        windSourceSelection.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                setWindSource();
            }
        });
        removeColumn = new IdentityColumn<WindDAO>(new ActionCell<WindDAO>(stringConstants.remove(), new Delegate<WindDAO>() {
            @Override
            public void execute(final WindDAO wind) {
                List<Triple<EventDAO, RegattaDAO, RaceDAO>> eventAndRaces = getSelectedEventAndRace();
                final Triple<EventDAO, RegattaDAO, RaceDAO> eventAndRace = eventAndRaces.get(eventAndRaces.size()-1);
                sailingService.removeWind(new EventNameAndRaceName(eventAndRace.getA().name, eventAndRace.getC().name), wind, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // remove row from underlying list:
                        windLists.get(WEB_WIND_SOURCE_NAME).getList().remove(wind);
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                                        WindPanel.this.errorReporter.reportError(
                                                WindPanel.this.stringConstants.errorSettingWindForRace()+ " "+eventAndRace.getC().name
                                                + ": "+ caught.getMessage());
                                    }
                });
            }
        }));
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
        grid = new Grid(3, 2); // first row: event/race selection; second row: wind source selection; third row: wind display
        trackedRacesTree = new RaceTreeView(stringConstants, /* multiselection */ false);
        trackedRacesTree.addRaceSelectionChangeListener(new RaceSelectionChangeListener() {
            @Override
            public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
                clearOrShowWindBasedOnRaceSelection(selectedRaces);
                fireRaceSelectionChanged(selectedRaces);
            }

        });
        grid.setWidget(0, 0, trackedRacesTree);
        Button btnRefresh = new Button(stringConstants.refresh());
        btnRefresh.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                WindPanel.this.eventRefresher.fillEvents();
            }
        });
        grid.setWidget(0, 1, btnRefresh);
        windSettingPanel = new WindSettingPanel(sailingService, errorReporter, this, this);
        HorizontalPanel windSourceSelectionPanel = new HorizontalPanel();
        windSourceSelectionPanel.setSpacing(10);
        windSourceSelectionPanel.add(new Label(stringConstants.windSource()));
        windSourceSelectionPanel.add(windSourceSelection);
        showEstimatedWindBox = new CheckBox(stringConstants.showEstimatedWind());
        windSourceSelectionPanel.add(showEstimatedWindBox);
        showEstimatedWindBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                clearOrShowWindBasedOnRaceSelection(trackedRacesTree.getSelectedEventAndRace());
            }
        });
        grid.setWidget(1, 0, windSourceSelectionPanel);
        grid.setWidget(2, 1, windSettingPanel);
        grid.getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
        this.setWidget(grid);
    }

    private void clearOrShowWindBasedOnRaceSelection(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        if (selectedRaces.isEmpty()) {
            clearWindDisplay(); // no wind known for untracked race
        } else {
            showWind(selectedRaces.get(0).getA(), selectedRaces.get(0).getC());
        }
    }

    @Override
    public void fillEvents(List<EventDAO> result) {
        trackedRacesTree.fillEvents(result);
    }

    @Override
    public void showWind(final EventDAO event, final RaceDAO race) {
        Date now = new Date();
        sailingService.getWindInfo(new EventNameAndRaceName(event.name, race.name),
                // TODO what about the time interval?
                                  new Date(now.getTime()-60000 /* one minute */), new Date(/* toAsMilliseconds */),
                showEstimatedWindBox.getValue(), new AsyncCallback<WindInfoForRaceDAO>() {
                    @Override
                    public void onSuccess(WindInfoForRaceDAO result) {
                        if (result != null) {
                            showWindForRace(result);
                            windSettingPanel.setEnabled(true);
                            updateWindSources(result);
                        } else {
                            clearWindDisplay(); // no wind known for untracked race
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(WindPanel.this.stringConstants.errorFetchingWindInformationForRace()+" " + race.name + ": "
                                + caught.getMessage());
                    }
                });
    }
    
    private void updateWindSources(WindInfoForRaceDAO result) {
        int selectedIndex = -1;
        for (String windSourceName : result.windTrackInfoByWindSourceName.keySet()) {
            boolean found = false;
            int i=0;
            while (!found && i<windSourceSelection.getItemCount()) {
                if (windSourceName.equals(windSourceSelection.getItemText(i))) {
                    found = true;
                } else {
                    i++;
                }
            }
            if (!found) {
                windSourceSelection.addItem(windSourceName);
            }
            if (windSourceName.equals(result.selectedWindSourceName)) {
                selectedIndex = i;
            }
        }
        windSourceSelection.setSelectedIndex(selectedIndex);
    }

    private void clearWindDisplay() {
        grid.setWidget(2, 0, null);
        windSettingPanel.setEnabled(false);
        windLists.clear();
    }

    private void showWindForRace(WindInfoForRaceDAO result) {
        grid.setWidget(2, 0, null);
        VerticalPanel windDisplay = new VerticalPanel();
        grid.setWidget(2, 0, windDisplay);
        for (Map.Entry<String, WindTrackInfoDAO> e : result.windTrackInfoByWindSourceName.entrySet()) {
            Label windSourceLabel = new Label(stringConstants.windSource()+": "+e.getKey()+
                    ", "+stringConstants.dampeningInterval()+" "+e.getValue().dampeningIntervalInMilliseconds+"ms");
            windDisplay.add(windSourceLabel);
            timeColumn.setSortable(true);
            speedInKnotsColumn.setSortable(true);
            windDirectionInDegColumn.setSortable(true);
            dampenedSpeedInKnotsColumn.setSortable(true);
            dampenedWindDirectionInDegColumn.setSortable(true);
            CellTable<WindDAO> windTable = new CellTable<WindDAO>(/* pageSize */ 10000);
            if (e.getKey().equals(WEB_WIND_SOURCE_NAME)) {
                // only the WEB wind source is editable, hence has a "Remove" column
                windTable.addColumn(removeColumn, "Remove");
            }
            windTable.addColumn(timeColumn, "Time");
            windTable.addColumn(speedInKnotsColumn, "Speed (kn)");
            windTable.addColumn(windDirectionInDegColumn, "From (deg)");
            windTable.addColumn(dampenedSpeedInKnotsColumn, "Avg Speed (kn)");
            windTable.addColumn(dampenedWindDirectionInDegColumn, "Avg From (deg)");
            ListDataProvider<WindDAO> windList = new ListDataProvider<WindDAO>(e.getValue().windFixes);
            windLists.put(e.getKey(), windList);
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
    public List<Triple<EventDAO, RegattaDAO, RaceDAO>> getSelectedEventAndRace() {
        return trackedRacesTree.getSelectedEventAndRace();
    }

    @Override
    public void addRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        raceSelectionChangeListeners.add(listener);
    }

    @Override
    public void removeRaceSelectionChangeListener(RaceSelectionChangeListener listener) {
        raceSelectionChangeListeners.remove(listener);
    }

    private void fireRaceSelectionChanged(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        for (RaceSelectionChangeListener listener : raceSelectionChangeListeners) {
            listener.onRaceSelectionChange(selectedRaces);
        }
    }

    private void setWindSource() {
        List<Triple<EventDAO, RegattaDAO, RaceDAO>> selection = trackedRacesTree.getSelectedEventAndRace();
        if (selection != null && !selection.isEmpty()) {
            final Triple<EventDAO, RegattaDAO, RaceDAO> selectedRace = selection.get(0);
            final String windSourceName = windSourceSelection.getItemText(windSourceSelection.getSelectedIndex());
            sailingService.setWindSource(new EventNameAndRaceName(selectedRace.getA().name, selectedRace.getC().name),
                    windSourceName, new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(WindPanel.this.stringConstants.errorWhileTryingToSetWindSourceForRace()+
                                    " "+selectedRace.getC().name+" "+WindPanel.this.stringConstants.inEvent()+" "+selectedRace.getA().name+
                                    " "+WindPanel.this.stringConstants.to()+" "+
                                    windSourceName+": "+caught.getMessage());
                        }
                        @Override
                        public void onSuccess(Void result) {
                        }
                    });
        }
    }
}
