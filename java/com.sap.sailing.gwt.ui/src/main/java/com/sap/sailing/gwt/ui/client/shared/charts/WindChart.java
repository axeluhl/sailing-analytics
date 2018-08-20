package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.BaseChart;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.PlotLine;
import org.moxieapps.gwt.highcharts.client.PlotLine.DashStyle;
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
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.client.DateTimeFormatRenderer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.WindSource;
import com.sap.sailing.domain.common.WindSourceType;
import com.sap.sailing.domain.common.impl.ColorMapImpl;
import com.sap.sailing.gwt.ui.actions.GetWindInfoAction;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.WindSourceTypeFormatter;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindInfoForRaceDTO;
import com.sap.sailing.gwt.ui.shared.WindTrackInfoDTO;
import com.sap.sse.common.Util;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.components.SettingsDialogComponent;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public class WindChart extends AbstractRaceChart<WindChartSettings> implements RequiresResize {
    public static final String LOAD_WIND_CHART_DATA_CATEGORY = "loadWindChartData";
    
    private static final int LINE_WIDTH = 1;

    private final WindChartSettings settings;
    private final WindChartLifecycle windChartLifecycle;
    
    /**
     * Holds one series for each wind source for which data has been received.
     */
    private final Map<WindSource, Series> windSourceDirectionSeries;
    private final Map<WindSource, Series> windSourceSpeedSeries;
    private final Map<WindSource, Point[]> windSourceDirectionPoints;
    private final Map<WindSource, Point[]> windSourceSpeedPoints;
    private Point firstPointOfFirstSeries;
    
    private Long timeOfEarliestRequestInMillis;
    private Long timeOfLatestRequestInMillis;

    private final ColorMapImpl<WindSource> colorMap;

    private WindSource preselectFilter;

    /**
     * @param selectedRaceIdentifier
     *            if <code>null</code>, this chart won't update its contents automatically upon race selection change;
     *            otherwise, whenever the selection changes, the wind data of the race selected now is loaded from the
     *            server and displayed in this chart. If no race is selected, the chart is cleared. The caller of this
     *            constructor must ensure to trigger {@link RaceSelectionChangeListener#onRaceSelectionChange(List)} at
     *            least once to ensure that this chart sets its {@link AbstractRaceChart#selectedRaceIdentifier} field.
     */
    public WindChart(Component<?> parent, ComponentContext<?> context, WindChartLifecycle windChartLifecycle,
            SailingServiceAsync sailingService,
            RegattaAndRaceIdentifier selectedRaceIdentifier, Timer timer,
            TimeRangeWithZoomProvider timeRangeWithZoomProvider, WindChartSettings settings, final StringMessages stringMessages, 
            AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter, boolean compactChart) {
        super(parent, context, sailingService, selectedRaceIdentifier, timer, timeRangeWithZoomProvider, stringMessages,
                asyncActionsExecutor, errorReporter);
        this.windChartLifecycle = windChartLifecycle;
        this.settings = settings;
        windSourceDirectionSeries = new HashMap<WindSource, Series>();
        windSourceSpeedSeries = new HashMap<WindSource, Series>();
        windSourceDirectionPoints = new HashMap<WindSource, Point[]>();
        windSourceSpeedPoints = new HashMap<WindSource, Point[]>();
        firstPointOfFirstSeries = null;
        colorMap = new ColorMapImpl<WindSource>();
        chart = new Chart()
                .setPersistent(true)
                .setReflow(false)
                .setZoomType(BaseChart.ZoomType.X)
                .setMarginLeft(65)
                .setMarginRight(65)
                .setWidth100()
                .setHeight100()
                .setBorderColor(new Color("#CACACA"))
                .setBorderWidth(0)
                .setBorderRadius(0)
                .setBackgroundColor(new Color("#FFFFFF"))
                .setPlotBackgroundColor("#f8f8f8")
                .setPlotBorderWidth(0)
                .setAlignTicks(false)
                .setCredits(new Credits().setEnabled(false))
                .setChartTitle(new ChartTitle().setText(stringMessages.wind()).setOption("floating",true))
                .setChartSubtitle(new ChartSubtitle().setText(stringMessages.clickAndDragToZoomIn()))
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(LINE_WIDTH).setMarker(
                        new Marker().setEnabled(false).setHoverState(
                                new Marker().setEnabled(true).setRadius(4))).setShadow(false)
                                    .setHoverStateLineWidth(LINE_WIDTH));
        chart.setStyleName(chartsCss.chartStyle());
        ChartUtil.useCheckboxesToShowAndHide(chart);
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
                            numberFormat.format(toolTipData.getYAsDouble()) + stringMessages.knotsUnit();
                }
            }
        }));
        
        chart.setClickEventHandler(new ChartClickEventHandler() {
            @Override
            public boolean onClick(ChartClickEvent chartClickEvent) {
                return WindChart.this.onClick(chartClickEvent);
            }
        });
       
        chart.setSelectionEventHandler(new ChartSelectionEventHandler() {
            @Override
            public boolean onSelection(ChartSelectionEvent chartSelectionEvent) {
                return WindChart.this.onXAxisSelectionChange(chartSelectionEvent);
            }
        });

        chart.getXAxis().setType(Axis.Type.DATE_TIME)
                        .setMaxZoom(60 * 1000) // 1 minute
                        .setAxisTitleText(stringMessages.time());
        chart.getXAxis().setLabels(new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                return dateFormatHoursMinutes.format(new Date(axisLabelsData.getValueAsLong()));
            }
        }));
        timePlotLine = chart.getXAxis().createPlotLine().setColor("#656565").setWidth(1.5).setDashStyle(DashStyle.SOLID);

        chart.getYAxis(0).setAxisTitleText(stringMessages.fromDeg()).setStartOnTick(false)
                .setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
                    @Override
                    public String format(AxisLabelsData axisLabelsData) {
                        long value = axisLabelsData.getValueAsLong() % 360;
                        return new Long(value < 0 ? value + 360 : value).toString();
                    }
                }));
        chart.getYAxis(1).setOpposite(true).setAxisTitleText(stringMessages.speed()+" ("+stringMessages.knotsUnit()+")").setMin(0)
            .setMaxPadding(0.05).setStartOnTick(false).setGridLineWidth(0).setMinorGridLineWidth(0);
        if (compactChart) {
            chart.setSpacingBottom(10).setSpacingLeft(10).setSpacingRight(10).setSpacingTop(20)
                 .setOption("legend/margin", 2)
                 .setOption("title/margin", 5)
                 .setChartSubtitle(null)
                 .getXAxis().setAxisTitle(null);
        }
        setSize("100%", "100%");
        if (selectedRaceIdentifier != null) {
            clearCacheAndReload();
            if (isVisible()) {
                updateVisibleSeries();
            }
        } else {
            clearChart();
        }
    }
    
    @Override
    protected Button createSettingsButton() {
        Button settingsButton = SettingsDialog.createSettingsButton(this, stringMessages);
        return settingsButton;
    }

    @Override
    public String getLocalizedShortName() {
        return windChartLifecycle.getLocalizedShortName();
    }

    @Override
    public Widget getEntryWidget() {
        return this;
    }

    private void updateVisibleSeries() {
        final Set<Series> visibleSeries = new HashSet<Series>(Arrays.asList(chart.getSeries()));

        if (preselectFilter != null) {
            forceSeriesSelection(visibleSeries, windSourceDirectionSeries);
            forceSeriesSelection(visibleSeries, windSourceSpeedSeries);
        } else {
            final boolean showDirectionSeries = settings.isShowWindDirectionsSeries();
            final Set<WindSourceType> directionSourceTypesToDisplay = settings.getWindDirectionSourcesToDisplay();
            updateSeries(visibleSeries, windSourceDirectionSeries, showDirectionSeries, directionSourceTypesToDisplay);
            final boolean showSpeedSeries = settings.isShowWindSpeedSeries();
            final Set<WindSourceType> speedSourceTypesToDisplay = settings.getWindSpeedSourcesToDisplay();
            updateSeries(visibleSeries, windSourceSpeedSeries, showSpeedSeries, speedSourceTypesToDisplay);
        }

        onResize();
    }

    private void updateSeries(final Set<Series> visibleSeries, final Map<WindSource, Series> windSourceToSeriesMap,
            final boolean showSeries, final Set<WindSourceType> windSourceTypesToDisplay) {
        if (showSeries) {
            for (Entry<WindSource, Series> entry : windSourceToSeriesMap.entrySet()) {
                final Series series = entry.getValue();
                if (windSourceTypesToDisplay.contains(entry.getKey().getType())) {
                    if (!visibleSeries.contains(series)) {
                        chart.addSeries(series, false, false);
                    }
                } else if (visibleSeries.contains(series)) {
                    chart.removeSeries(series, false);
                }
            }
        } else {
            for (Entry<WindSource, Series> entry : windSourceToSeriesMap.entrySet()) {
                final Series series = entry.getValue();
                if (visibleSeries.contains(series)) {
                    chart.removeSeries(series, false);
                }
            }
        }
    }

    private boolean forceSeriesSelection(Set<Series> visibleSeries, Map<WindSource, Series> toProcess) {
        boolean wasInResult = false;
        for (Map.Entry<WindSource, Series> e : toProcess.entrySet()) {
            Series series = e.getValue();
            WindSource seriesSource = e.getKey();
            // add all of type, remove non matching ones
            if (seriesSource.getType().equals(preselectFilter.getType())) {
                if (!visibleSeries.contains(series)) {
                    chart.addSeries(series, true, false);
                }
                // preselet the matching one
                if (preselectFilter.equals(e.getKey())) {
                    wasInResult = true;
                    series.select(true); // ensures that the checkbox will be ticked
                    series.setVisible(true, true);
                } else {
                    series.select(false);
                    series.setVisible(false, true);
                }
            } else {
                if (visibleSeries.contains(series)) {
                    chart.removeSeries(series, false);
                }
            }
        }
        return wasInResult;
    }

    /**
     * Creates the series for the <code>windSource</code> specified. If the series is created and needs to be visible
     * based on the {@link #windDirectionSourcesToDisplay}, it is added to the chart.
     */
    private Series getOrCreateSpeedSeries(WindSource windSource) {
        Series result = windSourceSpeedSeries.get(windSource);
        if (result == null) {
            result = createSpeedSeries(windSource);
            windSourceSpeedSeries.put(windSource, result);
        }
        return result;
    }

    /**
     * Creates the series for the <code>windSource</code> specified. If the series is created and needs to be visible
     * based on the {@link #windDirectionSourcesToDisplay}, it is added to the chart.
     */
    private Series getOrCreateDirectionSeries(WindSource windSource) {
        Series result = windSourceDirectionSeries.get(windSource);
        if (result == null) {
            result = createDirectionSeries(windSource);
            windSourceDirectionSeries.put(windSource, result);
        }
        return result;
    }


    /**
     * Only creates the series but doesn't add it to the chart. See also {@link #getOrCreateDirectionSeries(WindSource)} and
     * {@link #updateVisibleSeries()}
     */
    private Series createDirectionSeries(WindSource windSource) {
        Series newSeries = chart
                .createSeries()
                .setType(Series.Type.LINE)
                .setName(stringMessages.fromDeg()+" "+WindSourceTypeFormatter.format(windSource, stringMessages))
                .setYAxis(0)
                .setOption("turboThreshold", MAX_SERIES_POINTS)
                .setPlotOptions(new LinePlotOptions().setColor(colorMap.getColorByID(windSource).getAsHtml()).setSelected(true));
        return newSeries;
    }

    /**
     * Only creates the series but doesn't add it to the chart. See also {@link #getOrCreateSpeedSeries(WindSource)} and
     * {@link #updateVisibleSeries()}
     */
    private Series createSpeedSeries(WindSource windSource) {
        Series newSeries = chart
                .createSeries()
                .setType(Series.Type.LINE)
                .setName(stringMessages.windSpeed()+" "+WindSourceTypeFormatter.format(windSource, stringMessages))
                .setYAxis(1) // use the second Y-axis
                .setOption("turboThreshold", MAX_SERIES_POINTS)
                .setPlotOptions(new LinePlotOptions().setDashStyle(PlotLine.DashStyle.SHORT_DOT)
                        .setLineWidth(3).setHoverStateLineWidth(3)
                        .setColor(colorMap.getColorByID(windSource).getAsHtml()).setSelected(true)); // show only the markers, not the connecting lines
        return newSeries;
    }

    /**
     * Updates the wind charts with the wind data from <code>result</code>. If <code>append</code> is <code>true</code>, previously
     * existing points in the chart are left unchanged. Otherwise, the existing wind series are replaced.
     */
    public void updateChartSeries(WindInfoForRaceDTO result, boolean append) {
        final NumberFormat numberFormat = NumberFormat.getFormat("0");
        Long newMinTimepoint = timeOfEarliestRequestInMillis;
        Long newMaxTimepoint = timeOfLatestRequestInMillis;

        for (WindSource windSource: result.windTrackInfoByWindSource.keySet()) {
            WindTrackInfoDTO windTrackInfo = result.windTrackInfoByWindSource.get(windSource);
            Series directionSeries = getOrCreateDirectionSeries(windSource);
            Series speedSeries = null;
            if (windSource.getType().useSpeed()) {
                speedSeries = getOrCreateSpeedSeries(windSource);
            }

            Point previousDirectionPoint = null;
            if (append && windSourceDirectionPoints.get(windSource) != null
                       && windSourceDirectionPoints.get(windSource).length != 0) {
                previousDirectionPoint = windSourceDirectionPoints.get(windSource)[windSourceDirectionPoints.get(windSource).length - 1];
            }
            Point[] directionPoints = new Point[windTrackInfo.windFixes.size()];
            Point[] speedPoints = new Point[windTrackInfo.windFixes.size()];
            int currentPointIndex = 0;

            for (WindDTO wind : windTrackInfo.windFixes) {
                if (newMinTimepoint == null || wind.requestTimepoint < newMinTimepoint) {
                    newMinTimepoint = wind.requestTimepoint;
                }
                if (newMaxTimepoint == null || wind.requestTimepoint > newMaxTimepoint) {
                    newMaxTimepoint = wind.requestTimepoint;
                }
                //if we are in non appending mode, the data is the truth, use all of it without filtering
                if (!append || ((timeOfEarliestRequestInMillis == null || wind.requestTimepoint < timeOfEarliestRequestInMillis) || 
                    timeOfLatestRequestInMillis == null || wind.requestTimepoint > timeOfLatestRequestInMillis)) {
                    Point newDirectionPoint = new Point(wind.requestTimepoint, wind.dampenedTrueWindFromDeg);
                    if (wind.dampenedTrueWindSpeedInKnots != null) {
                        String name = numberFormat.format(wind.dampenedTrueWindSpeedInKnots)+ stringMessages.knotsUnit();
                        // name += " Confidence:" + wind.confidence;
                        newDirectionPoint.setName(name);
                    }
                    
                    if (previousDirectionPoint != null) {
                        newDirectionPoint = ChartPointRecalculator.stayClosestToPreviousPoint(previousDirectionPoint,
                                newDirectionPoint);
                    } else if (firstPointOfFirstSeries != null && windSourceDirectionPoints.get(windSource) == null) {
                        //This Point is the first point of a new series
                        newDirectionPoint = ChartPointRecalculator.stayClosestToPreviousPoint(firstPointOfFirstSeries, newDirectionPoint);
                    }
                    directionPoints[currentPointIndex] = newDirectionPoint;
                    previousDirectionPoint = newDirectionPoint;

                    Point newSpeedPoint = new Point(wind.requestTimepoint, wind.dampenedTrueWindSpeedInKnots);
                    speedPoints[currentPointIndex++] = newSpeedPoint;
                }
            }
            
            Point[] newDirectionPoints;
            Point[] newSpeedPoints = null;
            if (append) {
                Point[] oldDirectionPoints = windSourceDirectionPoints.get(windSource) != null ? windSourceDirectionPoints.get(windSource) : new Point[0];
                
                newDirectionPoints = new Point[oldDirectionPoints.length + currentPointIndex];
                System.arraycopy(oldDirectionPoints, 0, newDirectionPoints, 0, oldDirectionPoints.length);
                System.arraycopy(directionPoints, 0, newDirectionPoints, oldDirectionPoints.length, currentPointIndex);
                if (windSource.getType().useSpeed()) {
                    Point[] oldSpeedPoints =  windSourceSpeedPoints.get(windSource) != null ? windSourceSpeedPoints.get(windSource) : new Point[0];
                    newSpeedPoints = new Point[oldSpeedPoints.length + currentPointIndex];
                    System.arraycopy(oldSpeedPoints, 0, newSpeedPoints, 0, oldSpeedPoints.length);
                    System.arraycopy(speedPoints, 0, newSpeedPoints, oldSpeedPoints.length, currentPointIndex);
                }
            } else {
                newDirectionPoints = directionPoints;
                newSpeedPoints = speedPoints;
            }
            setSeriesPoints(directionSeries, newDirectionPoints, /* manageZoom */ true);
            windSourceDirectionPoints.put(windSource, newDirectionPoints);
            if (windSource.getType().useSpeed()) {
                setSeriesPoints(speedSeries, newSpeedPoints, /* manageZoom */ true);
                windSourceSpeedPoints.put(windSource, newSpeedPoints);
            }
            
            if (firstPointOfFirstSeries == null && newDirectionPoints.length != 0) { //If firstPointOfFirstSeries is null, than this series is the first
                firstPointOfFirstSeries = newDirectionPoints[0];
            }
        }
        
        timeOfEarliestRequestInMillis = newMinTimepoint;
        timeOfLatestRequestInMillis = newMaxTimepoint;
    }

    @Override
    public boolean hasSettings() {
        return windChartLifecycle.hasSettings();
    }

    @Override
    public SettingsDialogComponent<WindChartSettings> getSettingsDialogComponent(WindChartSettings settings) {
        return windChartLifecycle.getSettingsDialogComponent(settings);
    }

    /**
     * Sets the visibilities of the wind source series based on the new settings. Note that this does not
     * re-load any wind data. This has to happen by calling {@link #updateChartSeries(WindInfoForRaceDTO, boolean)}.
     */
    @Override
    public void updateSettings(WindChartSettings newSettings) {
        preselectFilter = null;
        boolean clearCacheAndReload = false;
        final Set<String> oldWindSourceTypesToRequest = getNamesOfWindSourceTypesOfWhichToDisplaySpeedOrDirection();
        if (newSettings.getResolutionInMilliseconds() != settings.getResolutionInMilliseconds()) {
            settings.setResolutionInMilliseconds(newSettings.getResolutionInMilliseconds());
            clearCacheAndReload = true;
        }
        settings.setShowWindDirectionsSeries(newSettings.isShowWindDirectionsSeries());
        settings.setWindDirectionSourcesToDisplay(newSettings.getWindDirectionSourcesToDisplay());

        settings.setShowWindSpeedSeries(newSettings.isShowWindSpeedSeries());
        settings.setWindSpeedSourcesToDisplay(newSettings.getWindSpeedSourcesToDisplay());
        if (!oldWindSourceTypesToRequest.equals(getNamesOfWindSourceTypesOfWhichToDisplaySpeedOrDirection())) {
            clearCacheAndReload = true;
        }
        if (clearCacheAndReload) {
            clearCacheAndReload();
        }
        updateVisibleSeries();
    }

    private void clearCacheAndReload() {
        timeOfEarliestRequestInMillis = null;
        timeOfLatestRequestInMillis = null;
        windSourceDirectionPoints.clear();
        windSourceSpeedPoints.clear();
        firstPointOfFirstSeries = null;
        loadData(timeRangeWithZoomProvider.getFromTime(), timeRangeWithZoomProvider.getToTime(), /* append */false);
    }

    /**
     * @param append
     *            if <code>true</code>, the results retrieved from the server will be appended to the wind chart instead
     *            of overwriting the existing series.
     */
    private void loadData(final Date from, final Date to, final boolean append) {
        if (isVisible()) {
            if (selectedRaceIdentifier == null) {
                clearChart();
            } else if (from != null && to != null) {
                setWidget(chart);
                // if not playing or empty show loading message
                if (shouldShowLoading(timeOfLatestRequestInMillis)) {
                    showLoading(stringMessages.windChartLoading());
                }
                GetWindInfoAction getWindInfoAction = new GetWindInfoAction(sailingService, selectedRaceIdentifier,
                        from, to, settings.getResolutionInMilliseconds(), getNamesOfWindSourceTypesOfWhichToDisplaySpeedOrDirection(),
                        /* onlyUpToNewestEvent==true because we don't want
                        to overshoot the evidence so far */ true);
                asyncActionsExecutor.execute(getWindInfoAction, LOAD_WIND_CHART_DATA_CATEGORY,
                        new AsyncCallback<WindInfoForRaceDTO>() {
                            @Override
                            public void onSuccess(WindInfoForRaceDTO result) {
                                if (result != null) {
                                    updateChartSeries(result, append);
                                    updateVisibleSeries();
                                } else {
                                    if (!append) {
                                        clearChart(); // no wind known for untracked race
                                    }
                                }
                                hideLoading();
                            }
            
                            @Override
                            public void onFailure(Throwable caught) {
                                errorReporter.reportError(stringMessages.errorFetchingWindInformationForRace() + " "
                                        + selectedRaceIdentifier + ": " + caught.getMessage(), timer.getPlayMode() == PlayModes.Live);
                                hideLoading();
                            }
                        });
            }
        }
    }
    
    private Set<String> getNamesOfWindSourceTypesOfWhichToDisplaySpeedOrDirection() {
        final Set<String> result = new HashSet<>();
        for (WindSourceType speedType : getSettings().getWindSpeedSourcesToDisplay()) {
            result.add(speedType.name());
        }
        for (WindSourceType speedType : getSettings().getWindDirectionSourcesToDisplay()) {
            result.add(speedType.name());
        }
        return result;
    }

    private void clearChart() {
        chart.removeAllSeries();
    }

    /**
     * If in live mode, fetches what's missing since the last fix and <code>date</code>. If nothing has been loaded yet,
     * loads from the beginning up to <code>date</code>. If in replay mode, checks if anything has been loaded at all. If not,
     * everything for the currently selected race is loaded; otherwise, no-op.
     */
    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        if (isVisible()) {
            updateTimePlotLine(newTime);
            switch (timer.getPlayMode()) {
                case Live:
                {
                    // is date before first cache entry or is cache empty?
                    if (timeOfEarliestRequestInMillis == null || newTime.getTime() < timeOfEarliestRequestInMillis) {
                        loadData(timeRangeWithZoomProvider.getFromTime(), newTime, /* append */ true);
                    } else if (newTime.getTime() > timeOfLatestRequestInMillis) {
                        loadData(new Date(timeOfLatestRequestInMillis), timeRangeWithZoomProvider.getToTime(), /* append */true);
                    }
                    // otherwise the cache spans across date and so we don't need to load anything
                    break;
                }
                case Replay:
                {
                    if (timeOfLatestRequestInMillis == null) {
                        // pure replay mode
                        loadData(timeRangeWithZoomProvider.getFromTime(), timeRangeWithZoomProvider.getToTime(), /* append */false);
                    } else {
                        // replay mode during live play
                        if (timeOfEarliestRequestInMillis == null || newTime.getTime() < timeOfEarliestRequestInMillis) {
                            loadData(timeRangeWithZoomProvider.getFromTime(), newTime, /* append */ true);
                        } else if (newTime.getTime() > timeOfLatestRequestInMillis) {
                            loadData(new Date(timeOfLatestRequestInMillis), newTime, /* append */true);
                        }                    
                    }
                    break;
                }
            }
        }
     }

    @Override
    public void onResize() {
        chart.setSizeToMatchContainer();
        // it's important here to recall the redraw method, otherwise the bug fix for wrong checkbox positions (nativeAdjustCheckboxPosition)
        // in the BaseChart class would not be called 
        chart.redraw();
    }

    /**
     * Prints basic data and points of a WindInfoForRaceDTO object to a formatted string.
     * Can be used for debugging
     */
    @SuppressWarnings("unused")
    private String printWindInfoForRace(final Date from, final Date to, WindInfoForRaceDTO result, boolean printFixDetails) {
        DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat("HH:mm:ss:SSS"));
        StringBuffer buffer = new StringBuffer();
        buffer.append("\n");
        buffer.append("Loaded wind data..." + "\n");
        buffer.append("From: " + timeFormatter.render(from) + "\n");
        buffer.append("To: " + timeFormatter.render(to) + "\n");
        buffer.append("With resolution: " + settings.getResolutionInMilliseconds() + "\n");
        
        for(WindSource windSource: result.windTrackInfoByWindSource.keySet()) {
            WindTrackInfoDTO windTrackInfoDTO = result.windTrackInfoByWindSource.get(windSource);
            int i = 1;
            buffer.append("Data of windsource: " + windSource.name() + "\n");
            if(printFixDetails) {
                for(WindDTO windDTO: windTrackInfoDTO.windFixes) {
                    String windFix = "P" + i++ + ": " + timeFormatter.render(new Date(windDTO.requestTimepoint));
                    if(windDTO.measureTimepoint != null) {
                        windFix += " ," + timeFormatter.render(new Date(windDTO.measureTimepoint));
                    }
                    buffer.append(windFix + "\n");
                }
            } else {
                buffer.append(Util.size(windTrackInfoDTO.windFixes) + " Fixes" + "\n");
            }
        }
        buffer.append("\n");
        return buffer.toString();
    }    

    /**
     * Prints basic data and all points of a windSource to a formatted string.
     * Can be used for debugging
     */
    @SuppressWarnings("unused")
    private String printPoints(WindSource windSource, String whatIsIt, Point[] points, boolean printFixDetails) {
        StringBuffer buffer = new StringBuffer();
        DateTimeFormatRenderer timeFormatter = new DateTimeFormatRenderer(DateTimeFormat.getFormat("HH:mm:ss:SSS"));
        buffer.append("\n");
        buffer.append("WindSource: " + windSource.name() + ": " + whatIsIt + "\n");
        buffer.append("Resolution in ms: " + settings.getResolutionInMilliseconds() + "\n");
        buffer.append("timeOfEarliestRequest: " + (timeOfEarliestRequestInMillis != null ? timeFormatter.render(new Date(timeOfEarliestRequestInMillis)) : "") + "\n");
        buffer.append("timeOfLatestRequest: " + (timeOfLatestRequestInMillis != null ? timeFormatter.render(new Date(timeOfLatestRequestInMillis)) : "") + "\n");
        if (points == null) {
            buffer.append("Points is null" + "\n");
        } else {
            buffer.append("Point count: " + points.length + "\n");
            if(printFixDetails) {
                Date xAsDate = new Date();
                for (int i = 0; i < points.length; i++) {
                    Point point = points[i];
                    xAsDate.setTime(point.getX().longValue());
                    buffer.append("P" + (i + 1) + ": " + timeFormatter.render(xAsDate) + ", V: " + point.getY() + "\n");
                }
            }
        }
        buffer.append("\n");
        return buffer.toString();
    }

    @Override
    public String getDependentCssClassName() {
        return "windChart";
    }

    @Override
    public WindChartSettings getSettings() {
        return settings;
    }

    @Override
    public String getId() {
        return windChartLifecycle.getComponentId();
    }

    /**
     * Forces the display of a specific windProvider preselected, and all of same type unselected. Will be disabled by either updateSettings or once the provider was properly found and shown
     */
    public void showProvider(WindSource windprovider) {
        WindSourceType type = windprovider.getType();
        Set<WindSourceType> windSpeedSourcesToDisplay = new HashSet<>();
        Set<WindSourceType> windDirectionSourcesToDisplay = new HashSet<>();
        windSpeedSourcesToDisplay.add(type);
        windDirectionSourcesToDisplay.add(type);
        WindChartSettings patched = new WindChartSettings(true,windSpeedSourcesToDisplay,true,windDirectionSourcesToDisplay,settings.getResolutionInMilliseconds());
        updateSettings(patched);
        preselectFilter = windprovider;
        updateVisibleSeries();
    }
}
