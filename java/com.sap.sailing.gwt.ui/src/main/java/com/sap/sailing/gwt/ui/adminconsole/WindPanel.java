package com.sap.sailing.gwt.ui.adminconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
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
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionModel;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RegattaDisplayer;
import com.sap.sailing.gwt.ui.client.RegattaRefresher;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.RegattaDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.shared.charts.WindChart;
import com.sap.sailing.gwt.ui.shared.charts.WindChartSettings;

/**
 * Displays a {@link WindChart} and a table of currently tracked races. The user can configure whether a race
 * is assumed to start with an upwind leg, show the wind data for the race selected in a chart, and exclude specific
 * wind sources from the overall (combined) wind computation, e.g., for performance reasons.
 * 
 * @author Axel Uhl (d043530)
 *
 */
public class WindPanel extends FormPanel implements RegattaDisplayer, WindShower, RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
    private final Grid grid;
    private final StringMessages stringMessages;
    private final WindSettingPanel windSettingPanel;
    private ColumnSortList columnSortList;
    private final IdentityColumn<WindDTO> removeColumn;
    private final TextColumn<WindDTO> timeColumn;
    private final TextColumn<WindDTO> speedInKnotsColumn;
    private final TextColumn<WindDTO> windDirectionInDegColumn;
    private final TextColumn<WindDTO> dampenedSpeedInKnotsColumn;
    private final TextColumn<WindDTO> dampenedWindDirectionInDegColumn;
    private final TrackedRacesListComposite trackedRacesListComposite;
    private final RaceSelectionProvider raceSelectionProvider;
    private final WindSourcesToExcludeSelector windSourcesToExcludeSelector;
    private final Map<WindSource, ListDataProvider<WindDTO>> windLists;
    private final CheckBox raceIsKnownToStartUpwindBox;

    public WindPanel(final SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            ErrorReporter errorReporter, RegattaRefresher regattaRefresher, final StringMessages stringMessages) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringMessages = stringMessages;
        this.raceSelectionProvider = new RaceSelectionModel();
        windLists = new HashMap<WindSource, ListDataProvider<WindDTO>>();
        windSourcesToExcludeSelector = new WindSourcesToExcludeSelector(sailingService, stringMessages, errorReporter);
        removeColumn = new IdentityColumn<WindDTO>(new ActionCell<WindDTO>(stringMessages.remove(), new Delegate<WindDTO>() {
            @Override
            public void execute(final WindDTO wind) {
                List<RegattaAndRaceIdentifier> selectedRaces = raceSelectionProvider.getSelectedRaces();
                final RegattaAndRaceIdentifier raceIdentifier = selectedRaces.get(selectedRaces.size()-1);
                sailingService.removeWind(raceIdentifier, wind, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // remove row from underlying list:
                        for (Map.Entry<WindSource, ListDataProvider<WindDTO>> e : windLists.entrySet()) {
                            if (e.getKey().getType() == WindSourceType.WEB) {
                                e.getValue().getList().remove(wind);
                            }
                        }
                    }
                    @Override
                    public void onFailure(Throwable caught) {
                                        WindPanel.this.errorReporter.reportError(
                                                WindPanel.this.stringMessages.errorSettingWindForRace()+ " "+raceIdentifier
                                                + ": "+ caught.getMessage());
                                    }
                });
            }
        }));
        timeColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return new Date(object.timepoint).toString();
            }
        };
        speedInKnotsColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return ""+object.trueWindSpeedInKnots;
            }
        };
        windDirectionInDegColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return ""+object.trueWindFromDeg;
            }
        };
        dampenedSpeedInKnotsColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return ""+object.dampenedTrueWindSpeedInKnots;
            }
        };
        dampenedWindDirectionInDegColumn = new TextColumn<WindDTO>() {
            @Override
            public String getValue(WindDTO object) {
                return ""+object.dampenedTrueWindFromDeg;
            }
        };
        grid = new Grid(4, 2); // first row: event/race selection; second row: wind source selection; third row: wind display
        trackedRacesListComposite = new TrackedRacesListComposite(sailingService, errorReporter, regattaRefresher,
                raceSelectionProvider, stringMessages, false);
        raceSelectionProvider.addRaceSelectionChangeListener(this);
        grid.setWidget(0, 0, trackedRacesListComposite);
        windSettingPanel = new WindSettingPanel(sailingService, errorReporter, raceSelectionProvider, this);
        grid.setWidget(0, 1, windSettingPanel);
        HorizontalPanel windSourceSelectionPanel = new HorizontalPanel();
        windSourceSelectionPanel.setSpacing(10);
        windSourceSelectionPanel.add(windSourcesToExcludeSelector);
        raceIsKnownToStartUpwindBox = new CheckBox(stringMessages.raceIsKnownToStartUpwind());
        windSourceSelectionPanel.add(raceIsKnownToStartUpwindBox);
        raceIsKnownToStartUpwindBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                setRaceIsKnownToStartUpwind(/* runOnSuccess */ new Runnable() {
                    public void run() {
                        clearOrShowWindBasedOnRaceSelection(raceSelectionProvider.getSelectedRaces());
                    }
                });
            }
        });
        grid.setWidget(1, 0, windSourceSelectionPanel);
        grid.getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
        this.setWidget(grid);
    }

    private void clearOrShowWindBasedOnRaceSelection(List<RegattaAndRaceIdentifier> selectedRaces) {
        if (selectedRaces.isEmpty()) {
            clearWindDisplay(); // no wind known for untracked race
        } else {
            showWind(selectedRaces.get(0));
        }
    }

    @Override
    public void fillRegattas(List<RegattaDTO> result) {
        trackedRacesListComposite.fillRegattas(result);
    }

    @Override
    public void showWind(final RegattaAndRaceIdentifier raceIdentifier) {
        sailingService.getAveragedWindInfo(raceIdentifier,
        // TODO Time interval should be determined by a selection in the chart but be at most 60s. See bug #121. Consider incremental updates for new data only.
                null, null, // use race start and time of newest event as default time period
                WindChartSettings.DEFAULT_RESOLUTION_IN_MILLISECONDS,
                null, // retrieve data on all wind sources
                new AsyncCallback<WindInfoForRaceDTO>() {
                    @Override
                    public void onSuccess(WindInfoForRaceDTO result) {
                        if (result != null) {
                            updateWindSourcesToExclude(result, raceIdentifier);
                            showWindForRace(result);
                            windSettingPanel.setEnabled(true);
                        } else {
                            clearWindDisplay(); // no wind known for untracked race
                        }
                    }

                    @Override
                    public void onFailure(Throwable caught) {
                        errorReporter.reportError(WindPanel.this.stringMessages.errorFetchingWindInformationForRace()+" " + raceIdentifier + ": "
                                + caught.getMessage());
                    }
                });
    }
    
    private void updateWindSourcesToExclude(WindInfoForRaceDTO result, RegattaAndRaceIdentifier raceIdentifier) {
        windSourcesToExcludeSelector.update(raceIdentifier, result.windTrackInfoByWindSource.keySet(), result.windSourcesToExclude);
    }

    private void clearWindDisplay() {
        grid.setWidget(2, 0, null);
        windSettingPanel.setEnabled(false);
        windLists.clear();
        final Set<WindSource> emptySet = Collections.emptySet();
        windSourcesToExcludeSelector.update(null, emptySet, emptySet);
    }

    private void showWindForRace(WindInfoForRaceDTO result) {
        raceIsKnownToStartUpwindBox.setValue(result.raceIsKnownToStartUpwind);
        grid.setWidget(3, 0, null);
        VerticalPanel windDisplay = new VerticalPanel();
        grid.setWidget(3, 0, windDisplay);
        // restrict tabular display to WEB sources; there, the REMOVE button is relevant; for all others, the chart has to do
        for (Map.Entry<WindSource, WindTrackInfoDTO> e : result.windTrackInfoByWindSource.entrySet()) {
            if (e.getKey().getType() == WindSourceType.WEB) {
                Label windSourceLabel = new Label(stringMessages.windSource() + ": " + e.getKey() + ", "
                        + stringMessages.dampeningInterval() + " " + e.getValue().dampeningIntervalInMilliseconds
                        + "ms");
                windDisplay.add(windSourceLabel);
                timeColumn.setSortable(true);
                speedInKnotsColumn.setSortable(true);
                windDirectionInDegColumn.setSortable(true);
                dampenedSpeedInKnotsColumn.setSortable(true);
                dampenedWindDirectionInDegColumn.setSortable(true);
                CellTable<WindDTO> windTable = new CellTable<WindDTO>(/* pageSize */10000);
                if (e.getKey().getType() == WindSourceType.WEB) {
                    // only the WEB wind source is editable, hence has a "Remove" column
                    windTable.addColumn(removeColumn, "Remove");
                }
                windTable.addColumn(timeColumn, "Time");
                windTable.addColumn(speedInKnotsColumn, "Speed (kn)");
                windTable.addColumn(windDirectionInDegColumn, "From (deg)");
                windTable.addColumn(dampenedSpeedInKnotsColumn, "Avg Speed (kn)");
                windTable.addColumn(dampenedWindDirectionInDegColumn, "Avg From (deg)");
                ListDataProvider<WindDTO> windList = new ListDataProvider<WindDTO>(e.getValue().windFixes);
                windLists.put(e.getKey(), windList);
                windList.addDataDisplay(windTable);
                Handler columnSortHandler = getWindTableColumnSortHandler(windList.getList(), timeColumn,
                        speedInKnotsColumn, windDirectionInDegColumn, dampenedSpeedInKnotsColumn,
                        dampenedWindDirectionInDegColumn);
                windTable.addColumnSortHandler(columnSortHandler);
                List<ColumnSortInfo> sortedColumnList = new ArrayList<ColumnSortInfo>();
                if (columnSortList != null) {
                    for (int i = 0; i < columnSortList.size(); i++) {
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
    }
    
    private Handler getWindTableColumnSortHandler(List<WindDTO> list, TextColumn<WindDTO> timeColumn,
            TextColumn<WindDTO> speedInKnotsColumn, TextColumn<WindDTO> windDirectionInDegColumn,
            TextColumn<WindDTO> dampenedSpeedInKnotsColumn, TextColumn<WindDTO> dampenedWindDirectionInDegColumn) {
        ListHandler<WindDTO> result = new ListHandler<WindDTO>(list);
        result.setComparator(timeColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.timepoint < o2.timepoint ? -1 : o1.timepoint == o2.timepoint ? 0 : 1;
            }
        });
        result.setComparator(speedInKnotsColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.trueWindSpeedInKnots < o2.trueWindSpeedInKnots ? -1 :
                    o1.trueWindSpeedInKnots == o2.trueWindSpeedInKnots ? 0 : 1;
            }
        });
        result.setComparator(windDirectionInDegColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.trueWindFromDeg < o2.trueWindFromDeg ? -1 :
                    o1.trueWindFromDeg == o2.trueWindFromDeg ? 0 : 1;
            }
        });
        result.setComparator(dampenedSpeedInKnotsColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.dampenedTrueWindSpeedInKnots < o2.dampenedTrueWindSpeedInKnots ? -1 :
                    o1.dampenedTrueWindSpeedInKnots == o2.dampenedTrueWindSpeedInKnots ? 0 : 1;
            }
        });
        result.setComparator(dampenedWindDirectionInDegColumn, new Comparator<WindDTO>() {
            @Override
            public int compare(WindDTO o1, WindDTO o2) {
                return o1.dampenedTrueWindFromDeg < o2.dampenedTrueWindFromDeg ? -1 :
                    o1.dampenedTrueWindFromDeg == o2.dampenedTrueWindFromDeg ? 0 : 1;
            }
        });
        return result;
    }

    private void setRaceIsKnownToStartUpwind(final Runnable runOnSuccess) {
        List<RegattaAndRaceIdentifier> selection = raceSelectionProvider.getSelectedRaces();
        if (selection != null && !selection.isEmpty()) {
            final RegattaAndRaceIdentifier selectedRace = selection.get(0);
            sailingService.setRaceIsKnownToStartUpwind(selectedRace,
                    raceIsKnownToStartUpwindBox.getValue(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(WindPanel.this.stringMessages.errorWhileTryingToSetWindSourceForRace()+
                                    " "+selectedRace+": "+caught.getMessage());
                        }
                        @Override
                        public void onSuccess(Void result) {
                            if (runOnSuccess != null) {
                                runOnSuccess.run();
                            }
                        }
                    });
        }
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        clearOrShowWindBasedOnRaceSelection(selectedRaces);
    }
}
