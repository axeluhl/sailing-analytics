package com.sap.sailing.gwt.ui.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ca.nanometrics.gflot.client.Axis;
import ca.nanometrics.gflot.client.PlotItem;
import ca.nanometrics.gflot.client.PlotModelStrategy;
import ca.nanometrics.gflot.client.PlotPosition;
import ca.nanometrics.gflot.client.PlotWithOverview;
import ca.nanometrics.gflot.client.PlotWithOverviewModel;
import ca.nanometrics.gflot.client.event.PlotHoverListener;
import ca.nanometrics.gflot.client.event.SelectionListener;
import ca.nanometrics.gflot.client.jsni.Plot;
import ca.nanometrics.gflot.client.options.AxisOptions;
import ca.nanometrics.gflot.client.options.GridOptions;
import ca.nanometrics.gflot.client.options.LegendOptions;
import ca.nanometrics.gflot.client.options.LineSeriesOptions;
import ca.nanometrics.gflot.client.options.PlotOptions;
import ca.nanometrics.gflot.client.options.PointsSeriesOptions;
import ca.nanometrics.gflot.client.options.SelectionOptions;
import ca.nanometrics.gflot.client.options.TickFormatter;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
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
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.gwt.ui.shared.EventDAO;
import com.sap.sailing.gwt.ui.shared.RaceDAO;
import com.sap.sailing.gwt.ui.shared.RegattaDAO;
import com.sap.sailing.gwt.ui.shared.Triple;
import com.sap.sailing.gwt.ui.shared.WindDAO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDAO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDAO;
import com.sap.sailing.server.api.EventNameAndRaceName;

public class WindPanel extends FormPanel implements EventDisplayer, WindShower, RaceSelectionChangeListener {
    private final SailingServiceAsync sailingService;
    private final ErrorReporter errorReporter;
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
    private final TrackedEventsComposite trackedEventsComposite;
    private final ListBox windSourceSelection;
    private final Map<WindSource, ListDataProvider<WindDAO>> windLists;
    private final CheckBox showEstimatedWindBox;
    private final CheckBox raceIsKnownToStartUpwindBox;
    private final Widget stripChart;
    private final DateTimeFormat dateFormat;

    public WindPanel(final SailingServiceAsync sailingService, ErrorReporter errorReporter,
            EventRefresher eventRefresher, StringConstants stringConstants) {
        this.sailingService = sailingService;
        this.errorReporter = errorReporter;
        this.stringConstants = stringConstants;
        dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
        windLists = new HashMap<WindSource, ListDataProvider<WindDAO>>();
        windSourceSelection = new ListBox();
        windSourceSelection.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                setWindSource(/* runOnSuccess */ null);
            }
        });
        removeColumn = new IdentityColumn<WindDAO>(new ActionCell<WindDAO>(stringConstants.remove(), new Delegate<WindDAO>() {
            @Override
            public void execute(final WindDAO wind) {
                List<Triple<EventDAO, RegattaDAO, RaceDAO>> eventAndRaces = trackedEventsComposite.getSelectedEventAndRace();
                final Triple<EventDAO, RegattaDAO, RaceDAO> eventAndRace = eventAndRaces.get(eventAndRaces.size()-1);
                sailingService.removeWind(new EventNameAndRaceName(eventAndRace.getA().name, eventAndRace.getC().name), wind, new AsyncCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // remove row from underlying list:
                        windLists.get(WindSource.WEB).getList().remove(wind);
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
        grid = new Grid(4, 2); // first row: event/race selection; second row: wind source selection; third row: wind display
        trackedEventsComposite = new TrackedEventsComposite(sailingService, errorReporter, eventRefresher, stringConstants, false);
        trackedEventsComposite.addRaceSelectionChangeListener(this);
        grid.setWidget(0, 0, trackedEventsComposite);
        windSettingPanel = new WindSettingPanel(sailingService, errorReporter, trackedEventsComposite, this);
        grid.setWidget(0, 1, windSettingPanel);
        HorizontalPanel windSourceSelectionPanel = new HorizontalPanel();
        windSourceSelectionPanel.setSpacing(10);
        windSourceSelectionPanel.add(new Label(stringConstants.windSource()));
        windSourceSelectionPanel.add(windSourceSelection);
        raceIsKnownToStartUpwindBox = new CheckBox(stringConstants.raceIsKnownToStartUpwind());
        windSourceSelectionPanel.add(raceIsKnownToStartUpwindBox);
        raceIsKnownToStartUpwindBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                setWindSource(/* runOnSuccess */ new Runnable() {
                    public void run() {
                        clearOrShowWindBasedOnRaceSelection(trackedEventsComposite.getSelectedEventAndRace());
                    }
                });
            }
        });
        showEstimatedWindBox = new CheckBox(stringConstants.showEstimatedWind());
        windSourceSelectionPanel.add(showEstimatedWindBox);
        showEstimatedWindBox.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
                clearOrShowWindBasedOnRaceSelection(trackedEventsComposite.getSelectedEventAndRace());
            }
        });
        grid.setWidget(1, 0, windSourceSelectionPanel);
        stripChart = createStripChart();
        grid.setWidget(2, 0, stripChart);
        grid.getCellFormatter().setVerticalAlignment(1, 1, HasVerticalAlignment.ALIGN_TOP);
        this.setWidget(grid);
    }

    private Widget createStripChart() {
        PlotWithOverviewModel model = new PlotWithOverviewModel(PlotModelStrategy.defaultStrategy());
        PlotOptions plotOptions = new PlotOptions();
        plotOptions.setDefaultLineSeriesOptions(new LineSeriesOptions().setLineWidth(1).setShow(true));
        plotOptions.setDefaultPointsOptions(new PointsSeriesOptions().setShow(false));
        plotOptions.setDefaultShadowSize(2);
        AxisOptions hAxisOptions = new AxisOptions();
        hAxisOptions.setTickFormatter(new TickFormatter() {
            @Override
            public String formatTickValue(double tickValue, Axis axis) {
                return dateFormat.format(new Date((long) tickValue));
            }
        });
        plotOptions.setXAxisOptions(hAxisOptions);
        plotOptions.setLegendOptions(new LegendOptions().setShow(false));
        plotOptions.setGridOptions(new GridOptions().setHoverable(true).setMouseActiveRadius(5).setAutoHighlight(true));
        plotOptions.setSelectionOptions(new SelectionOptions().setDragging(true).setMode("x")); // select along x-axis only
        /*
        for (int i = 0; i <  competitorsAndTimePointsDAO.getCompetitor().length; i++){
                SeriesHandler series = model.addSeries(""+i, getColorByID(i));
                series.setOptions(SeriesType.LINES, new LineSeriesOptions().setLineWidth(2.5).setShow(true));
                series.setOptions(SeriesType.POINTS, new PointsSeriesOptions().setLineWidth(0).setShow(false));
                series.setVisible(false);
                seriesID.add(series);
                series = model.addSeries(i + " passed mark", getColorByID(i));
                series.setOptions(SeriesType.LINES, new LineSeriesOptions().setLineWidth(0).setShow(false));
                series.setOptions(SeriesType.POINTS, new PointsSeriesOptions().setLineWidth(3).setShow(true));
                series.setVisible(false);
                markSeriesID.add(series);
        }
        */
        final PlotWithOverview plot = new PlotWithOverview(model, plotOptions) {
            @Override
            protected void onLoad() {
                super.onLoad();
                // onLoad is called immediately after a widget becomes attached to the browser's document.
                // Use this time point to ask the chart to redraw
                redraw();
            }
        };
        // add hover listener
        plot.addHoverListener(new PlotHoverListener() {
            public void onPlotHover(Plot plot, PlotPosition position, PlotItem item) {
                /*
                CompetitorDAO competitor = competitorID.get(seriesID.indexOf(item.getSeries()));
                if (item != null && competitor != null) {
                        if (item.getSeries().getLabel().toLowerCase().contains("mark")){
                                selectedPointLabel.setText(competitor.name + " passed " + markPassingBuoyName.get(competitor.id + (long) item.getDataPoint().getX()) +" at " + dateFormat.format(new Date((long) item.getDataPoint().getX())));
                        }
                        else {
                                String unit = "";
                                switch (dataToShow){
                                case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
                                        unit = stringConstants.currentSpeedOverGroundInKnotsUnit();
                                        break;
                                case DISTANCE_TRAVELED:
                                        unit = stringConstants.distanceInMetersUnit();
                                        break;
                                case GAP_TO_LEADER_IN_SECONDS:
                                        unit = stringConstants.gapToLeaderInSecondsUnit();
                                        break;
                                case VELOCITY_MADE_GOOD_IN_KNOTS:
                                        unit = stringConstants.velocityMadeGoodInKnotsUnit();
                                        break;
                                case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
                                        unit = stringConstants.windwardDistanceToGoInMetersUnit();
                                }
                                String decimalPlaces = "";
                                for (int i = 0; i < dataToShow.getPrecision(); i++){
                                        if (i == 0){
                                                decimalPlaces += ".";
                                        }
                                        decimalPlaces += "0";
                                }
                                NumberFormat numberFormat = NumberFormat.getFormat("0" +decimalPlaces);
                                selectedPointLabel.setText(competitor.name + " at " + dateFormat.format(new Date((long) item.getDataPoint().getX()))
                                + ": " + numberFormat.format(item.getDataPoint().getY()) + unit);
                        }
                } else {
                    selectedPointLabel.setText(stringConstants.noSelection());
                }
                */
            }
        }, true);
        plot.addSelectionListener(new SelectionListener() {

            public void selected(double x1, double y1, double x2, double y2) {
                /* TODO Remove not visible buoys from the series when user is zooming in or add them if he is zooming out.
                for (CompetitorDAO competitor : competitorsAndTimePointsDAO.getCompetitor()){
                        long[] markPassingTimes = competitorsAndTimePointsDAO.getMarkPassings(competitor);
                    Double[] markPassingValues = chartData.getMarkPassings(competitor);
                    SeriesHandler markSeries = getCompetitorMarkPassingSeries(competitor);
                    markSeries.clear();
                    int visibleMarkPassings = 0;
                    for (int j = 0; j < markPassingTimes.length; j++){
                        if (markPassingValues[j] != null && markPassingTimes[j] > x1 && markPassingTimes[j] < x2) {
                            markSeries.add(new DataPoint(markPassingTimes[j],markPassingValues[j]));
                            visibleMarkPassings++;
                        }
                    }
                    if (visibleMarkPassings == 0){
                        markSeries.setVisible(false);
                    }
                }*/
                plot.setLinearSelection(x1, x2);
            }
        });
//        plot.setHeight(height);
//        plot.setWidth(width);
        plot.setOverviewHeight(60);
        return plot;
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
        trackedEventsComposite.fillEvents(result);
    }

    @Override
    public void showWind(final EventDAO event, final RaceDAO race) {
        sailingService.getWindInfo(new EventNameAndRaceName(event.name, race.name),
        // TODO Time interval should be determined by a selection in the chart but be at most 60s. See bug #121.
                null, null, // use race start and time of newest event as default time period
                null, // retrieve data on all wind sources
                new AsyncCallback<WindInfoForRaceDAO>() {
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
        for (WindSource windSource : result.windTrackInfoByWindSourceName.keySet()) {
            boolean found = false;
            int i=0;
            while (!found && i<windSourceSelection.getItemCount()) {
                if (windSource.name().equals(windSourceSelection.getItemText(i))) {
                    found = true;
                } else {
                    i++;
                }
            }
            if (!found) {
                windSourceSelection.addItem(windSource.name());
            }
            if (windSource == result.selectedWindSource) {
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
        raceIsKnownToStartUpwindBox.setValue(result.raceIsKnownToStartUpwind);
        grid.setWidget(3, 0, null);
        VerticalPanel windDisplay = new VerticalPanel();
        grid.setWidget(3, 0, windDisplay);
        for (Map.Entry<WindSource, WindTrackInfoDAO> e : result.windTrackInfoByWindSourceName.entrySet()) {
            Label windSourceLabel = new Label(stringConstants.windSource()+": "+e.getKey()+
                    ", "+stringConstants.dampeningInterval()+" "+e.getValue().dampeningIntervalInMilliseconds+"ms");
            windDisplay.add(windSourceLabel);
            timeColumn.setSortable(true);
            speedInKnotsColumn.setSortable(true);
            windDirectionInDegColumn.setSortable(true);
            dampenedSpeedInKnotsColumn.setSortable(true);
            dampenedWindDirectionInDegColumn.setSortable(true);
            CellTable<WindDAO> windTable = new CellTable<WindDAO>(/* pageSize */ 10000);
            if (e.getKey() == WindSource.WEB) {
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

    private void setWindSource(final Runnable runOnSuccess) {
        List<Triple<EventDAO, RegattaDAO, RaceDAO>> selection = trackedEventsComposite.getSelectedEventAndRace();
        if (selection != null && !selection.isEmpty()) {
            final Triple<EventDAO, RegattaDAO, RaceDAO> selectedRace = selection.get(0);
            final String windSourceName = windSourceSelection.getItemText(windSourceSelection.getSelectedIndex());
            sailingService.setWindSource(new EventNameAndRaceName(selectedRace.getA().name, selectedRace.getC().name),
                    windSourceName, raceIsKnownToStartUpwindBox.getValue(), new AsyncCallback<Void>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(WindPanel.this.stringConstants.errorWhileTryingToSetWindSourceForRace()+
                                    " "+selectedRace.getC().name+" "+WindPanel.this.stringConstants.inEvent()+" "+selectedRace.getA().name+
                                    " "+WindPanel.this.stringConstants.to()+" "+
                                    windSourceName+": "+caught.getMessage());
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
    public void onRaceSelectionChange(List<Triple<EventDAO, RegattaDAO, RaceDAO>> selectedRaces) {
        clearOrShowWindBasedOnRaceSelection(selectedRaces);
    }
    
    public List<Triple<EventDAO, RegattaDAO, RaceDAO>> getSelectedRaces(){
        return trackedEventsComposite.getSelectedEventAndRace();
    }
}
