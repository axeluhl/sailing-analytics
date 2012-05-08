package com.sap.sailing.gwt.ui.adminconsole;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Extremes;
import org.moxieapps.gwt.highcharts.client.PlotLine;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.events.ChartClickEvent;
import org.moxieapps.gwt.highcharts.client.events.ChartClickEventHandler;
import org.moxieapps.gwt.highcharts.client.events.ChartSelectionEvent;
import org.moxieapps.gwt.highcharts.client.events.ChartSelectionEventHandler;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.EventAndRaceIdentifier;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.actions.GetWindInfoAction;
import com.sap.sailing.gwt.ui.client.ColorMap;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.raceboard.RaceTimesCalculationUtil;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.components.SettingsDialogComponent;

public class WindChart extends SimpleChartPanel implements Component<WindChartSettings>, RaceSelectionChangeListener, 
    RaceTimesInfoProviderListener, TimeListener, RequiresResize {
    public static final long DEFAULT_RESOLUTION_IN_MILLISECONDS = 10000;

    private static final int LINE_WIDTH = 1;
    private final StringMessages stringMessages;
    private final Set<WindSourceType> windSourceTypesToDisplay;
    private long resolutionInMilliseconds;
    
    /**
     * Holds one series for each wind source for which data has been received.
     */
    private final Map<WindSource, Series> windSourceDirectionSeries;
    private final Map<WindSource, Series> windSourceSpeedSeries;
    
    private final ErrorReporter errorReporter;
    private final AsyncActionsExecutor asyncActionsExecutor;
    private final SailingServiceAsync sailingService;
    private final Chart chart;
    private Series timeLineSeries;
    private final boolean allowTimeAdjust;
    private boolean ignoreClickOnce;
    private final DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
    
    private Long timeOfEarliestRequestInMillis;
    private Long timeOfLatestRequestInMillis;
    
    private Date chartMinTimepoint;
    private Date chartMaxTimepoint;
    
    private RaceIdentifier selectedRaceIdentifier;
    private RaceTimesInfoDTO lastRaceTimesInfo;
    
    private final ColorMap<WindSource> colorMap;

    /**
     * @param raceSelectionProvider
     *            if <code>null</code>, this chart won't update its contents automatically upon race selection change;
     *            otherwise, whenever the selection changes, the wind data of the race selected now is loaded from the
     *            server and displayed in this chart. If no race is selected, the chart is cleared.
     */
    public WindChart(SailingServiceAsync sailingService, RaceSelectionProvider raceSelectionProvider, Timer timer,
            WindChartSettings settings, final StringMessages stringMessages, AsyncActionsExecutor asyncActionsExecutor,
            ErrorReporter errorReporter, boolean compactChart, boolean allowTimeAdjust) {
        super(timer);
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.stringMessages = stringMessages;
        this.errorReporter = errorReporter;
        this.allowTimeAdjust = allowTimeAdjust;
        this.windSourceDirectionSeries = new HashMap<WindSource, Series>();
        this.windSourceSpeedSeries = new HashMap<WindSource, Series>();
        this.colorMap = new ColorMap<WindSource>();
        this.windSourceTypesToDisplay = new HashSet<WindSourceType>();
        chart = new Chart()
                .setZoomType(Chart.ZoomType.X)
                .setMarginLeft(65)
                .setMarginRight(65)
                .setWidth100()
                .setHeight100()
                .setChartTitle(new ChartTitle().setText(stringMessages.wind()))
                .setChartSubtitle(new ChartSubtitle().setText(stringMessages.clickAndDragToZoomIn()))
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(LINE_WIDTH).setMarker(
                        new Marker().setEnabled(false).setHoverState(
                                new Marker().setEnabled(true).setRadius(4))).setShadow(false)
                                    .setHoverStateLineWidth(LINE_WIDTH));
        useCheckboxesToShowAndHide(chart);
        final NumberFormat numberFormat = NumberFormat.getFormat("0");
        chart.setToolTip(new ToolTip().setEnabled(true).setFormatter(new ToolTipFormatter() {
            @Override
            public String format(ToolTipData toolTipData) {
                String seriesName = toolTipData.getSeriesName();
                
                if (seriesName.equals(WindChart.this.stringMessages.time())) {
                    return "<b>" + seriesName + ":</b> " + dateFormat.format(new Date(toolTipData.getXAsLong()))
                            + "<br/>(" + stringMessages.clickChartToSetTime() + ")";
                } else if (seriesName.startsWith(stringMessages.fromDeg()+" ")) {
                    double value = toolTipData.getYAsDouble() % 360;
                    return "<b>" + seriesName + (toolTipData.getPointName() != null ? " "+toolTipData.getPointName() : "")
                            + "</b><br/>" +  
                            dateFormat.format(new Date(toolTipData.getXAsLong())) + ": " +
                            numberFormat.format(value < 0 ? value + 360 : value) + stringMessages.degreesShort();
                } else {
                    return "<b>" + seriesName + (toolTipData.getPointName() != null ? " "+toolTipData.getPointName() : "")
                            + "</b><br/>" +  
                            dateFormat.format(new Date(toolTipData.getXAsLong())) + ": " +
                            numberFormat.format(toolTipData.getYAsDouble()) + stringMessages.averageSpeedInKnotsUnit();
                }
            }
        }));
        
        chart.setBackgroundColor(new Color("#ebebec"));
        
        if (allowTimeAdjust) {
            chart.setClickEventHandler(new ChartClickEventHandler() {
                @Override
                public boolean onClick(ChartClickEvent chartClickEvent) {
                    if (ignoreClickOnce) {
                        ignoreClickOnce = false;
                    } else {
                        WindChart.this.timer.setPlayMode(PlayModes.Replay);
                        WindChart.this.timer.setTime(chartClickEvent.getXAxisValueAsLong());
                    }
                    return true;
                }
            });
            chart.setSelectionEventHandler(new ChartSelectionEventHandler() {
                @Override
                public boolean onSelection(ChartSelectionEvent chartSelectionEvent) {
                    try {
                        chartSelectionEvent.getXAxisMaxAsLong();
                        chartSelectionEvent.getXAxisMinAsLong();
                        ignoreClickOnce = true;
                    } catch (Throwable t) {
                        //Redrawing, or the chart wouldn't rezoom
                        chart.redraw();
                    }
                    return true;
                }
            });
        }
        
        chart.getXAxis().setType(Axis.Type.DATE_TIME).setMaxZoom(10000) // ten seconds
                .setAxisTitleText(stringMessages.time());
        chart.getYAxis(0).setAxisTitleText(stringMessages.fromDeg()).setStartOnTick(false).setShowFirstLabel(false)
                .setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
                    @Override
                    public String format(AxisLabelsData axisLabelsData) {
                        long value = axisLabelsData.getValueAsLong() % 360;
                        return new Long(value < 0 ? value + 360 : value).toString();
                    }
                }));
        chart.getYAxis(1).setOpposite(true).setAxisTitleText(stringMessages.speed()+" ("+stringMessages.averageSpeedInKnotsUnit()+")")
            .setStartOnTick(false).setShowFirstLabel(false).setGridLineWidth(0).setMinorGridLineWidth(0);
        
        if (compactChart) {
            chart.setSpacingBottom(10).setSpacingLeft(10).setSpacingRight(10).setSpacingTop(2)
                 .setOption("legend/margin", 2)
                 .setOption("title/margin", 5)
                 .setChartSubtitle(null)
                 .getXAxis().setAxisTitle(null);
        }
        
        timeLineSeries = createTimeLineSeries();
        
        setWidget(chart);
        setSize("100%", "100%");
        updateSettings(settings);
        if (raceSelectionProvider != null) {
            raceSelectionProvider.addRaceSelectionChangeListener(this);
            onRaceSelectionChange(raceSelectionProvider.getSelectedRaces());
        }
        showVisibleSeries();
        timer.addTimeListener(this);
    }

    @Override
    public String getLocalizedShortName() {
        return stringMessages.wind();
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    private void showVisibleSeries() {
        Series[] currentlyVisible = chart.getSeries();
        Set<Series> visible = new HashSet<Series>();
        for (Series series : currentlyVisible) {
            visible.add(series);
        }
        for (Map.Entry<WindSource, Series> e : windSourceDirectionSeries.entrySet()) {
            if (windSourceTypesToDisplay.contains(e.getKey().getType())) {
                if (!visible.contains(e.getValue())) {
                    chart.addSeries(e.getValue());
                    e.getValue().select(true); // ensures that the checkbox will be ticked
                } else {
                    visible.remove(e.getValue());
                }
            }
        }
        for (Map.Entry<WindSource, Series> e : windSourceSpeedSeries.entrySet()) {
            if (windSourceTypesToDisplay.contains(e.getKey().getType())) {
                if (!visible.contains(e.getValue())) {
                    chart.addSeries(e.getValue());
                    e.getValue().select(true); // ensures that the checkbox will be ticked
                } else {
                    visible.remove(e.getValue());
                }
            }
        }
        for (Series seriesToRemove : visible) {
            chart.removeSeries(seriesToRemove);
        }
    }

    /**
     * Creates the series for the <code>windSource</code> specified. If the series is created and needs to be visible
     * based on the {@link #windSourceTypesToDisplay}, it is added to the chart.
     */
    private Series getOrCreateSpeedSeries(WindSource windSource) {
        Series result = windSourceSpeedSeries.get(windSource);
        if (result == null) {
            result = createSpeedSeries(windSource);
            windSourceSpeedSeries.put(windSource, result);
            if (windSourceTypesToDisplay.contains(windSource.getType())) {
                chart.addSeries(result);
            }
        }
        return result;
    }

    /**
     * Only creates the series but doesn't add it to the chart. See also {@link #getOrCreateDirectionSeries(WindSource)} and
     * {@link #showVisibleSeries()}
     */
    private Series createDirectionSeries(WindSource windSource) {
        Series newSeries = chart
                .createSeries()
                .setType(Series.Type.LINE)
                .setName(stringMessages.fromDeg()+" "+WindSourceTypeFormatter.format(windSource, stringMessages))
                .setYAxis(0)
                .setPlotOptions(new LinePlotOptions().setColor(colorMap.getColorByID(windSource)).setSelected(true));
        return newSeries;
    }

    /**
     * Only creates the series but doesn't add it to the chart. See also {@link #getOrCreateSpeedSeries(WindSource)} and
     * {@link #showVisibleSeries()}
     */
    private Series createSpeedSeries(WindSource windSource) {
        Series newSeries = chart
                .createSeries()
                .setType(Series.Type.LINE)
                .setName(stringMessages.windSpeed()+" "+WindSourceTypeFormatter.format(windSource, stringMessages))
                .setYAxis(1) // use the second Y-axis
                .setPlotOptions(new LinePlotOptions().setDashStyle(PlotLine.DashStyle.SHORT_DOT)
                        .setLineWidth(3).setHoverStateLineWidth(3)
                        .setColor(colorMap.getColorByID(windSource)).setSelected(true)); // show only the markers, not the connecting lines
        return newSeries;
    }

    private Series createTimeLineSeries() {
        return chart
                .createSeries()
                .setType(Series.Type.LINE)
                .setName(stringMessages.time())
                .setYAxis(0)
                .setPlotOptions(new LinePlotOptions().setShowInLegend(false).setHoverStateEnabled(false).setLineWidth(2));
    }

    /**
     * Updates the wind charts with the wind data from <code>result</code>. If <code>append</code> is <code>true</code>, previously
     * existing points in the chart are left unchanged. Otherwise, the existing wind series are replaced.
     */
    public void updateStripChartSeries(WindInfoForRaceDTO result, boolean append) {
        final NumberFormat numberFormat = NumberFormat.getFormat("0");
        for (Map.Entry<WindSource, WindTrackInfoDTO> e : result.windTrackInfoByWindSource.entrySet()) {
            WindSource windSource = e.getKey();
            Series directionSeries = getOrCreateDirectionSeries(windSource);
            Series speedSeries = null;
            if (windSource.getType().useSpeed()) {
                speedSeries = getOrCreateSpeedSeries(windSource);
            }
            WindTrackInfoDTO windTrackInfo = e.getValue();
            Double directionMin = null;
            Double directionMax = null;
            Point[] directionPoints = new Point[windTrackInfo.windFixes.size()];
            Point[] speedPoints = new Point[windTrackInfo.windFixes.size()];
            int i=0;
            for (WindDTO wind : windTrackInfo.windFixes) {
                if (timeOfEarliestRequestInMillis == null || wind.timepoint<timeOfEarliestRequestInMillis) {
                    timeOfEarliestRequestInMillis = wind.timepoint;
                }
                if (timeOfLatestRequestInMillis == null || wind.timepoint>timeOfLatestRequestInMillis) {
                    timeOfLatestRequestInMillis = wind.timepoint;
                }
                
                Point newDirectionPoint = new Point(wind.timepoint, wind.dampenedTrueWindFromDeg);
                if (wind.dampenedTrueWindSpeedInKnots != null) {
                    newDirectionPoint.setName(numberFormat.format(wind.dampenedTrueWindSpeedInKnots)+stringMessages.averageSpeedInKnotsUnit());
                }
                newDirectionPoint = recalculateDirectionPoint(directionMax, directionMin, newDirectionPoint);
                directionPoints[i] = newDirectionPoint;

                double direction = newDirectionPoint.getY().doubleValue();
                if (directionMax == null || direction > directionMax) {
                    directionMax = direction;
                }
                if (directionMin == null || direction < directionMin) {
                    directionMin = direction;
                }

                Point newSpeedPoint = new Point(wind.timepoint, wind.dampenedTrueWindSpeedInKnots);
                speedPoints[i++] = newSpeedPoint;
            }
            Point[] newDirectionPoints;
            Point[] newSpeedPoints = null;
            if (append) {
                Point[] oldDirectionPoints = directionSeries.getPoints();
                newDirectionPoints = new Point[oldDirectionPoints.length + directionPoints.length];
                System.arraycopy(oldDirectionPoints, 0, newDirectionPoints, 0, oldDirectionPoints.length);
                System.arraycopy(directionPoints, 0, newDirectionPoints, oldDirectionPoints.length, directionPoints.length);
                if (windSource.getType().useSpeed()) {
                    Point[] oldSpeedPoints = speedSeries.getPoints();
                    newSpeedPoints = new Point[oldSpeedPoints.length + speedPoints.length];
                    System.arraycopy(oldSpeedPoints, 0, newSpeedPoints, 0, oldSpeedPoints.length);
                    System.arraycopy(speedPoints, 0, newSpeedPoints, oldSpeedPoints.length, speedPoints.length);
                }
            } else {
                newDirectionPoints = directionPoints;
                newSpeedPoints = speedPoints;
            }
            directionSeries.setPoints(newDirectionPoints);
            if (windSource.getType().useSpeed()) {
                speedSeries.setPoints(newSpeedPoints);
            }
        }
    }
    
    private Point recalculateDirectionPoint(Double yMax, Double yMin, Point directionPoint) {
        double y = directionPoint.getY().doubleValue();
        boolean recalculated = false;

        if (yMax != null && yMin != null && (y < yMin || yMax < y)) {
            double deltaMin = Math.abs(yMin - y);
            double deltaMax = Math.abs(yMax - y);

            double yDown = y - 360;
            double deltaMinDown = Math.abs(yMin - Math.abs(yDown));

            double yUp = y + 360;
            double deltaMaxUp = Math.abs(yMax - Math.abs(yUp));

            if (!(deltaMin <= deltaMinDown && deltaMin <= deltaMaxUp)
                    && !(deltaMax <= deltaMinDown && deltaMax <= deltaMaxUp)) {
                y = deltaMaxUp <= deltaMinDown ? yUp : yDown;
                recalculated = true;
            }
        }
        
        return recalculated ? new Point(directionPoint.getX(), y) : directionPoint;
    }

    @Override
    public boolean hasSettings() {
        return true;
    }

    @Override
    public SettingsDialogComponent<WindChartSettings> getSettingsDialogComponent() {
        return new WindChartSettingsDialogComponent(new WindChartSettings(windSourceTypesToDisplay, resolutionInMilliseconds), stringMessages);
    }

    /**
     * Sets the visibilities of the wind source series based on the new settings. Note that this does not
     * re-load any wind data. This has to happen by calling {@link #updateStripChartSeries(WindInfoForRaceDTO, boolean)}.
     */
    @Override
    public void updateSettings(WindChartSettings newSettings) {
        if (newSettings.getResolutionInMilliseconds() != resolutionInMilliseconds) {
            resolutionInMilliseconds = newSettings.getResolutionInMilliseconds();
            clearCacheAndReload();
        }
        windSourceTypesToDisplay.clear();
        windSourceTypesToDisplay.addAll(newSettings.getWindSourceTypesToDisplay());
        chart.removeAllSeries(/* redraw */ false);
        for (Map.Entry<WindSource, Series> e : windSourceDirectionSeries.entrySet()) {
            if (windSourceTypesToDisplay.contains(e.getKey().getType())) {
                chart.addSeries(e.getValue());
                e.getValue().select(true); // ensures that the checkbox will be ticked
                if (windSourceSpeedSeries.containsKey(e.getKey()) && e.getKey().getType().useSpeed()) {
                    chart.addSeries(windSourceSpeedSeries.get(e.getKey()));
                    windSourceSpeedSeries.get(e.getKey()).select(true); // ensures that the checkbox will be ticked
                }
            }
        }
        chart.redraw();
    }

    private void clearCacheAndReload() {
        timeOfEarliestRequestInMillis = null;
        timeOfLatestRequestInMillis = null;
        
        loadData(selectedRaceIdentifier, chartMinTimepoint, chartMaxTimepoint, /* append */false);
    }

    /**
     * Creates the series for the <code>windSource</code> specified. If the series is created and needs to be visible
     * based on the {@link #windSourceTypesToDisplay}, it is added to the chart.
     */
    private Series getOrCreateDirectionSeries(WindSource windSource) {
        Series result = windSourceDirectionSeries.get(windSource);
        if (result == null) {
            result = createDirectionSeries(windSource);
            windSourceDirectionSeries.put(windSource, result);
            if (windSourceTypesToDisplay.contains(windSource.getType())) {
                chart.addSeries(result);
            }
        }
        return result;
    }

    /**
     * @param append
     *            if <code>true</code>, the results retrieved from the server will be appended to the wind chart instead
     *            of overwriting the existing series.
     */
    private void loadData(final RaceIdentifier raceIdentifier, final Date from, final Date to, final boolean append) {
        if (raceIdentifier == null) {
            clearChart();
        } else if (needsDataLoading() && from != null && to != null) {
            GetWindInfoAction getWindInfoAction = new GetWindInfoAction(sailingService, raceIdentifier,
                    // TODO Time interval should be determined by a selection in the chart but be at most 60s. See bug #121.
                    // Consider incremental updates for new data only.
                    from, to, resolutionInMilliseconds,
                    null, new AsyncCallback<WindInfoForRaceDTO>() {
                        @Override
                        public void onSuccess(WindInfoForRaceDTO result) {
                            if (result != null) {
                                updateStripChartSeries(result, append);
                            } else {
                                if (!append) {
                                    clearChart(); // no wind known for untracked race
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.errorFetchingWindInformationForRace() + " "
                                    + raceIdentifier + ": " + caught.getMessage(), timer.getPlayMode() == PlayModes.Live);
                        }
                    });
            asyncActionsExecutor.execute(getWindInfoAction);
        }
    }
    
    private void clearChart() {
        chart.removeAllSeries();
    }

    @Override
    public void onRaceSelectionChange(List<EventAndRaceIdentifier> selectedRaces) {
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            // show wind of first selected race
            selectedRaceIdentifier = selectedRaces.iterator().next();
            clearCacheAndReload();
        } else {
            clearChart();
        }
    }

    @Override
    public void raceTimesInfosReceived(Map<RaceIdentifier, RaceTimesInfoDTO> raceTimesInfos) {
        this.lastRaceTimesInfo = raceTimesInfos.get(selectedRaceIdentifier);
        
        Pair<Date, Date> raceMinMax = RaceTimesCalculationUtil.caluclateRaceMinMax(timer, this.lastRaceTimesInfo);
        
        this.chartMinTimepoint = raceMinMax.getA();
        this.chartMaxTimepoint = raceMinMax.getB();
    }

    /**
     * If in live mode, fetches what's missing since the last fix and <code>date</code>. If nothing has been loaded yet,
     * loads from the beginning up to <code>date</code>. If in replay mode, checks if anything has been loaded at all. If not,
     * everything for the currently selected race is loaded; otherwise, no-op.
     */
    @Override
    public void timeChanged(Date date) {
        if (timer.getPlayMode() == PlayModes.Live) {
            // is date before first cache entry or is cache empty?
            if (timeOfEarliestRequestInMillis == null || timeOfEarliestRequestInMillis > date.getTime()) {
                loadData(selectedRaceIdentifier, chartMinTimepoint, date, /* append */ true);
            } else if (timeOfLatestRequestInMillis < date.getTime()) {
                loadData(selectedRaceIdentifier, new Date(timeOfLatestRequestInMillis), chartMaxTimepoint, /* append */true);
            }
            // otherwise the cache spans across date and so we don't need to load anything
        } else {
            // assuming play mode is replay / non-live
            if (timeOfLatestRequestInMillis == null) {
                loadData(selectedRaceIdentifier, chartMinTimepoint, chartMaxTimepoint, /* append */false); // replace old series
            }
        }
        updateTimeLine(date);
    }

    /**
     * Forces the chart to {@link #updateTimeLine(Date) update} the position and to {@link #ensureTimeLineIsVisible()
     * ensure}, that the time line is visible.
     */
    public void forceTimeLineUpdate() {
        updateTimeLine(timer.getTime());
    }
    
    /**
     * Updates the position of the time line for the given {@link Date}, if {@link #allowTimeAdjust} is <code>true</code>.<br />
     * @param date Defines the x-Value of the line points
     */
    private void updateTimeLine(Date date) {
        if (allowTimeAdjust) {
            Long x = date.getTime();
            try {
                Extremes extremes = chart.getYAxis(0).getExtremes();
                Point[] points = new Point[2];
                points[0] = new Point(x, extremes.getDataMin());
                points[1] = new Point(x, extremes.getDataMax());
                timeLineSeries.setPoints(points);
                ensureTimeLineIsVisible();
            } catch (NullPointerException e) {
                // explicitly do nothing; it means that getExtremes() has thrown an exception, so we don't know how to set them
            }
        }
    }
    
    /**
     * Checks if the series of the chart contain the {@link #timeLineSeries}. If not, it is added to the chart.
     */
    private void ensureTimeLineIsVisible() {
        if (!Arrays.asList(chart.getSeries()).contains(timeLineSeries)) {
            chart.addSeries(timeLineSeries);
        }
    }
    
    @Override
    public void onResize() {
        chart.setSizeToMatchContainer();
    }
    
    private boolean needsDataLoading() {
        return isVisible();
    }

}
