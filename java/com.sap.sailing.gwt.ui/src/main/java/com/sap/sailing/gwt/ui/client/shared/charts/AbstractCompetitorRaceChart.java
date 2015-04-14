package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.BaseChart;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
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
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.ScatterPlotOptions;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.actions.GetCompetitorsRaceDataAction;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.shared.CompetitorRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsRaceDataDTO;
import com.sap.sse.common.Util;
import com.sap.sse.common.filter.Filter;
import com.sap.sse.common.filter.FilterSet;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.shared.components.Component;

/**
 * AbstractCompetitorChart is a chart that can show one sort of competitor data (e.g. current speed over ground, windward
 * distance to leader) for different races in a chart.
 * 
 * When calling the constructor a chart is created that creates a final amount of series (so the maximum number of
 * competitors cannot be changed in one chart) which are connected to competitors, when the sailing service returns the
 * data. So {@code seriesID, competitorID and markSeriesID} are linked with the index. So if u know for example the
 * seriesID-index, you can get the competitor by calling competitorID.get(index).
 * 
 * @author Benjamin Ebling (D056866), Axel Uhl (d043530)
 * 
 */
public abstract class AbstractCompetitorRaceChart<SettingsType extends ChartSettings> extends AbstractRaceChart implements
        CompetitorSelectionChangeListener, RequiresResize {
    public static final String LOAD_COMPETITOR_CHART_DATA_CATEGORY = "loadCompetitorChartData";
    
    private static final int LINE_WIDTH = 1;
    
    private final Label noCompetitorsSelectedLabel;
    private final Label noDataFoundLabel;
    private final CompetitorSelectionProvider competitorSelectionProvider;
    private DetailType selectedDetailType;

    private boolean compactChart;
    private final boolean allowTimeAdjust;
    private final String leaderboardGroupName;
    private final String leaderboardName;
    private long stepSize = 5000;
    private final Map<CompetitorDTO, Series> dataSeriesByCompetitor;
    private final Map<CompetitorDTO, Series> markPassingSeriesByCompetitor;
    private Long timeOfEarliestRequestInMillis;
    private Long timeOfLatestRequestInMillis;
    
    protected AbstractCompetitorRaceChart(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            CompetitorSelectionProvider competitorSelectionProvider, RaceSelectionProvider raceSelectionProvider,
            Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider, Button settingsButton,
            final StringMessages stringMessages, ErrorReporter errorReporter, DetailType detailType, boolean compactChart, boolean allowTimeAdjust) {
        this(sailingService, asyncActionsExecutor, competitorSelectionProvider, raceSelectionProvider, timer,
                timeRangeWithZoomProvider, stringMessages, errorReporter, detailType, compactChart, allowTimeAdjust,
                null, null);
    }

    AbstractCompetitorRaceChart(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            CompetitorSelectionProvider competitorSelectionProvider, RaceSelectionProvider raceSelectionProvider,
            Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider, final StringMessages stringMessages,
            ErrorReporter errorReporter, DetailType detailType, boolean compactChart, boolean allowTimeAdjust,
            String leaderboardGroupName, String leaderboardName) {
        super(sailingService, timer, timeRangeWithZoomProvider, stringMessages, asyncActionsExecutor, errorReporter);
        this.competitorSelectionProvider = competitorSelectionProvider;
        this.compactChart = compactChart;
        this.allowTimeAdjust = allowTimeAdjust;
        this.leaderboardGroupName = leaderboardGroupName;
        this.leaderboardName = leaderboardName;
        
        dataSeriesByCompetitor = new HashMap<CompetitorDTO, Series>();
        markPassingSeriesByCompetitor = new HashMap<CompetitorDTO, Series>();
        
        setSize("100%", "100%");
        noCompetitorsSelectedLabel = new Label(stringMessages.selectAtLeastOneCompetitor() + ".");
        noCompetitorsSelectedLabel.setStyleName("abstractChartPanel-importantMessageOfChart");
        noDataFoundLabel = new Label(stringMessages.noDataFound() + ".");
        noDataFoundLabel.setStyleName("abstractChartPanel-importantMessageOfChart");
        createChart();
        setSelectedDetailType(detailType);
        
        competitorSelectionProvider.addCompetitorSelectionChangeListener(this);
        raceSelectionProvider.addRaceSelectionChangeListener(this);
    }

    /**
     * Creates a new chart.
     * Attention: We can't reuse the old chart when the detail changes because HighChart does not support the inverting of the Y-Axis  
     */
    private Chart createChart() {
        Chart chart = new Chart().setZoomType(BaseChart.ZoomType.X)
                .setPersistent(true)
                .setWidth100()
                .setHeight100()
                .setMarginLeft(65)
                .setMarginRight(65)
                .setBorderColor(new Color("#CACACA"))
                .setBackgroundColor(new Color("#FFFFFF"))
                .setPlotBackgroundColor("#f8f8f8")
                .setBorderWidth(0)
                .setBorderRadius(0)
                .setPlotBorderWidth(0)
                .setCredits(new Credits().setEnabled(false))
                .setChartSubtitle(new ChartSubtitle().setText(stringMessages.clickAndDragToZoomIn()))
                .setLinePlotOptions(
                        new LinePlotOptions()
                                .setLineWidth(LINE_WIDTH)
                                .setMarker(
                                        new Marker().setEnabled(false).setHoverState(
                                                new Marker().setEnabled(true).setRadius(4))).setShadow(false)
                                .setHoverStateLineWidth(LINE_WIDTH));
        chart.setStyleName(chartsCss.chartStyle());
        ChartUtil.useCheckboxesToShowAndHide(chart);

        if (allowTimeAdjust) {
            chart.setClickEventHandler(new ChartClickEventHandler() {
                @Override
                public boolean onClick(ChartClickEvent chartClickEvent) {
                    return AbstractCompetitorRaceChart.this.onClick(chartClickEvent);
                }
            });
            chart.setSelectionEventHandler(new ChartSelectionEventHandler() {
                @Override
                public boolean onSelection(ChartSelectionEvent chartSelectionEvent) {
                    return AbstractCompetitorRaceChart.this.onXAxisSelectionChange(chartSelectionEvent);
                }
            });
        }

        chart.getXAxis().setType(Axis.Type.DATE_TIME).setMaxZoom(60 * 1000); // 1 minute
        chart.getYAxis().setStartOnTick(false).setShowFirstLabel(false);
        chart.getXAxis().setLabels(new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                return dateFormatHoursMinutes.format(new Date(axisLabelsData.getValueAsLong()));
            }
        }));
        timePlotLine = chart.getXAxis().createPlotLine().setColor("#656565").setWidth(1.5).setDashStyle(DashStyle.SOLID);

        if (compactChart) {
            chart.setSpacingBottom(10).setSpacingLeft(10).setSpacingRight(10).setSpacingTop(2)
                    .setOption("legend/margin", 2).setOption("title/margin", 5).setChartSubtitle(null).getXAxis()
                    .setAxisTitleText(null);
        }

        return chart;
    }

    protected abstract Component<SettingsType> getComponent();

    /**
     * Loads the needed data (data which isn't in the {@link #chartData cache}) for the
     * {@link #getSelectedCompetitors() visible competitors} via
     * {@link SailingServiceAsync#getCompetitorsRaceData(RaceIdentifier, List, long, DetailType, AsyncCallback)}. After
     * loading, the method {@link #drawChartData()} is called.
     * <p>
     * If no data needs to be {@link #needsDataLoading() loaded}, the "no competitors selected" label is displayed.
     */
    private void updateChart(Date from, Date to, boolean append) {
        if (hasSelectedCompetitors() && isVisible()) {
            remove(noCompetitorsSelectedLabel);
            if (!getChildren().contains(chart)) {
                add(chart);
            }
            ArrayList<CompetitorDTO> competitorsToLoad = new ArrayList<CompetitorDTO>();
            for (CompetitorDTO competitorDTO : getSelectedCompetitors()) {
                competitorsToLoad.add(competitorDTO);
            }
            loadData(from, to, competitorsToLoad, append);
        } else {
            remove(chart);
            if (!getChildren().contains(noCompetitorsSelectedLabel)) {
                add(noCompetitorsSelectedLabel);
            }
        }
    }

    private void loadData(final Date from, final Date to, final List<CompetitorDTO> competitors, final boolean append) {
        if (isVisible()) {
            showLoading(stringMessages.loadingCompetitorData());
            ArrayList<CompetitorDTO> competitorsToLoad = new ArrayList<CompetitorDTO>();
            for (CompetitorDTO competitorDTO : competitors) {
                competitorsToLoad.add(competitorDTO);
            }

            GetCompetitorsRaceDataAction getCompetitorsRaceDataAction = new GetCompetitorsRaceDataAction(sailingService,
                    selectedRaceIdentifier, competitorsToLoad, from, to, getStepSize(), getSelectedDetailType(),
                    leaderboardGroupName, leaderboardName);
            asyncActionsExecutor.execute(getCompetitorsRaceDataAction, LOAD_COMPETITOR_CHART_DATA_CATEGORY,
                    new AsyncCallback<CompetitorsRaceDataDTO>() {
                        @Override
                        public void onSuccess(final CompetitorsRaceDataDTO result) {
                            hideLoading();
                            if (result != null) {
                                if (result.isEmpty() && chartContainsNoData()) {
                                    setWidget(noDataFoundLabel);
                                } else {
                                    updateChartSeries(result, append);
                                }
                            } else {
                                if (!append) {
                                    clearChart();
                                }
                            }
                        }
            
                        @Override
                        public void onFailure(Throwable caught) {
                            hideLoading();
                            errorReporter.reportError(stringMessages.errorFetchingChartData(caught.getMessage()),
                                    timer.getPlayMode() == PlayModes.Live);
                        }
                    });
        }
    }
    
    private boolean chartContainsNoData() {
        for (Series competitorSeries : dataSeriesByCompetitor.values()) {
            if (competitorSeries.getPoints().length != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        if (isVisible()) {
            ArrayList<CompetitorDTO> competitorsToLoad = new ArrayList<CompetitorDTO>();
            competitorsToLoad.add(competitor);
            Date fromDate = timeOfEarliestRequestInMillis == null ? null : new Date(timeOfEarliestRequestInMillis);
            Date toDate = timeOfLatestRequestInMillis == null ? null : new Date(timeOfLatestRequestInMillis);
            loadData(fromDate, toDate, competitorsToLoad, false);
        }
    }

    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        Series competitorSeries = dataSeriesByCompetitor.get(competitor);
        if (competitorSeries != null) {
            chart.removeSeries(competitorSeries, false);
        }
        Series competitorMarkPassingSeries = markPassingSeriesByCompetitor.get(competitor);
        if (competitorMarkPassingSeries != null) {
            chart.removeSeries(competitorMarkPassingSeries, false);
        }
        
        if (isVisible()) {
             if (hasSelectedCompetitors()) {
                 chart.redraw();
             } else {
                 setWidget(noCompetitorsSelectedLabel);
             }
        }
    }

    /**
     * Creates the series for all selected competitors if these aren't created yet.<br />
     * Fills the series for the selected competitors with the data in {@link AbstractCompetitorRaceChart#chartData}.<br />
     */
    private synchronized void updateChartSeries(CompetitorsRaceDataDTO chartData, boolean append) {
        // Make sure the busy indicator is removed at this point, or plotting the data results in an exception
        setWidget(chart);
        List<Series> chartSeries = Arrays.asList(chart.getSeries());
        for (CompetitorDTO competitor : chartData.getCompetitors()) {
            Series competitorDataSeries = getOrCreateCompetitorDataSeries(competitor);
            Series markPassingSeries = getOrCreateCompetitorMarkPassingSeries(competitor);
            CompetitorRaceDataDTO competitorData = chartData.getCompetitorData(competitor);
            if (competitorData != null) {
                Date toDate = timer.getLiveTimePointAsDate();
                List<com.sap.sse.common.Util.Triple<String, Date, Double>> markPassingsData = competitorData.getMarkPassingsData();
                List<Point> markPassingPoints = new ArrayList<Point>();
                for (com.sap.sse.common.Util.Triple<String, Date, Double> markPassingData : markPassingsData) {
                    if (markPassingData.getB() != null && markPassingData.getC() != null) {
                        if (markPassingData.getB().before(toDate)) {
                            Point markPassingPoint = new Point(markPassingData.getB().getTime(),
                                    markPassingData.getC());
                            markPassingPoint.setName(markPassingData.getA());
                            markPassingPoints.add(markPassingPoint);
                        }
                    }
                }
                markPassingSeries.setPoints(markPassingPoints.toArray(new Point[0]), false);

                Point[] oldRaceDataPoints = competitorDataSeries.getPoints();
                List<com.sap.sse.common.Util.Pair<Date, Double>> raceData = competitorData.getRaceData();

                Point[] raceDataPointsToAdd = new Point[raceData.size()];
                int currentPointIndex = 0;
                for (com.sap.sse.common.Util.Pair<Date, Double> raceDataPoint : raceData) {
                    Double dataPointValue = raceDataPoint.getB();
                    if(dataPointValue != null) {
                        long dataPointTimeAsMillis = raceDataPoint.getA().getTime();
                        if(append == false || (timeOfEarliestRequestInMillis == null || dataPointTimeAsMillis < timeOfEarliestRequestInMillis) || 
                                timeOfLatestRequestInMillis == null || dataPointTimeAsMillis > timeOfLatestRequestInMillis) {
                            raceDataPointsToAdd[currentPointIndex++] = new Point(dataPointTimeAsMillis, dataPointValue);
                        }
                    }
                }

                Point[] newRaceDataPoints;
                if (append) {
                    newRaceDataPoints = new Point[oldRaceDataPoints.length + currentPointIndex];
                    System.arraycopy(oldRaceDataPoints, 0, newRaceDataPoints, 0, oldRaceDataPoints.length);
                    System.arraycopy(raceDataPointsToAdd, 0, newRaceDataPoints, oldRaceDataPoints.length, currentPointIndex);
                } else {
                    newRaceDataPoints = new Point[currentPointIndex];
                    System.arraycopy(raceDataPointsToAdd, 0, newRaceDataPoints, 0, currentPointIndex);
                }
                setSeriesPoints(competitorDataSeries, newRaceDataPoints);
                // Adding the series if chart doesn't contain it
                if (!chartSeries.contains(competitorDataSeries)) {
                    chart.addSeries(competitorDataSeries);
                    chart.addSeries(markPassingSeries);
                }
            }
        }
        if (timeOfEarliestRequestInMillis == null || timeOfEarliestRequestInMillis > chartData.getRequestedFromTime().getTime()) {
            timeOfEarliestRequestInMillis = chartData.getRequestedFromTime().getTime();
        }
        if (timeOfLatestRequestInMillis == null || timeOfLatestRequestInMillis < chartData.getRequestedToTime().getTime()) {
            timeOfLatestRequestInMillis = chartData.getRequestedToTime().getTime();
        }
        chart.redraw();
    }

    private Iterable<CompetitorDTO> getSelectedCompetitors() {
        return competitorSelectionProvider.getSelectedCompetitors();
    }

    private boolean hasSelectedCompetitors() {
        return Util.size(getSelectedCompetitors()) > 0;
    }

    /**
     * 
     * @param competitor
     * @return A series in the chart, that can be used to show the data of a specific competitor.
     */
    private Series getOrCreateCompetitorDataSeries(final CompetitorDTO competitor) {
        Series result = dataSeriesByCompetitor.get(competitor);
        if (result == null) {
            result = chart.createSeries().setType(Series.Type.LINE).setName(competitor.getName());
            result.setPlotOptions(new LinePlotOptions()
                    .setLineWidth(LINE_WIDTH)
                    .setMarker(new Marker().setEnabled(false).setHoverState(new Marker().setEnabled(true).setRadius(4)))
                    .setShadow(false).setHoverStateLineWidth(LINE_WIDTH)
                    .setColor(competitorSelectionProvider.getColor(competitor).getAsHtml()).setSelected(true));
            result.setOption("turboThreshold", MAX_SERIES_POINTS);
            dataSeriesByCompetitor.put(competitor, result);
        }
        return result;
    }

    /**
     * 
     * @param competitor
     * @return A series in the chart, that can be used to show the mark passings.
     */
    private Series getOrCreateCompetitorMarkPassingSeries(CompetitorDTO competitor) {
        Series result = markPassingSeriesByCompetitor.get(competitor);
        if (result == null) {
            result = chart.createSeries().setType(Series.Type.SCATTER)
                    .setName(stringMessages.markPassing() + " " + competitor.getName());
            result.setPlotOptions(new ScatterPlotOptions().setColor(competitorSelectionProvider.getColor(competitor).getAsHtml())
                    .setSelected(true));
            markPassingSeriesByCompetitor.put(competitor, result);
        }
        return result;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // Workaround for a highcharts bug: 
            // Set a chart title, overwrite the title, switch chart to invisible and visible again -> the old title appears  
            chart.setTitle(new ChartTitle().setText(DetailTypeFormatter.format(selectedDetailType)), null);
        }
    }

    /**
     * Clears the whole chart and empties cached data.
     */
    protected void clearChart() {
        timeOfEarliestRequestInMillis = null;
        timeOfLatestRequestInMillis = null;
        dataSeriesByCompetitor.clear();
        markPassingSeriesByCompetitor.clear();
        chart.removeAllSeries();
    }

    public String getLocalizedShortName() {
        return DetailTypeFormatter.format(getSelectedDetailType());
    }

    public Widget getEntryWidget() {
        return this;
    }

    public boolean hasSettings() {
        return true;
    }

    public ChartSettings getAbstractSettings() {
        return new ChartSettings(getStepSize());
    }

    /**
     * Updates the settings known to be contained in {@link ChartSettings}. Subclasses have to update settings provided
     * by subclasses thereof. Subclasses also need to call {@link #clearChart()} and {@link #updateChart(boolean)}, if
     * this method returns <code>true</code>;
     * 
     * @return <code>true</code> if the settings had been changed and a clearing and loading is needed.
     */
    protected boolean updateSettingsOnly(ChartSettings newSettings) {
        boolean settingsChanged = false;
        if (getStepSize() != newSettings.getStepSize()) {
            setStepSize(newSettings.getStepSize());
            settingsChanged = true;
        }
        return settingsChanged;
    }

    protected StringMessages getStringMessages() {
        return stringMessages;
    }

    protected long getStepSize() {
        return stepSize;
    }

    protected void setStepSize(long stepSize) {
        this.stepSize = stepSize;
    }

    protected DetailType getSelectedDetailType() {
        return this.selectedDetailType;
    }
    
    /**
     * Updates the {@link #selectedDetailType} field, clears the chart for the new <code>selectedDetailType</code> and
     * clears the {@link #chartData}.<br />
     * Doesn't {@link #updateChart(boolean) load the data}.
     * 
     * @return <code>true</code> if the detail type changed
     */
    protected boolean setSelectedDetailType(DetailType newSelectedDetailType) {
        boolean hasDetailTypeChanged = newSelectedDetailType != this.selectedDetailType;
        if (hasDetailTypeChanged) {
            boolean oldReversedYAxis = hasReversedYAxis(this.selectedDetailType);
            this.selectedDetailType = newSelectedDetailType;
            // TODO There is a bug in the highcharts library which prevents to change the reverse property of the YAxis
            // Because we need this functionality we need to recreate the chart each time the YAxis changes
            if (oldReversedYAxis != hasReversedYAxis(selectedDetailType)) {
                chart = createChart();
                if (isZoomed) {
                	com.sap.sse.common.Util.Pair<Date, Date> zoomRange = timeRangeWithZoomProvider.getTimeZoom();
                    onTimeZoomChanged(zoomRange.getA(), zoomRange.getB());
                } else {
                    resetMinMaxAndExtremesInterval(/* redraw */ true);
                }
            }
            chart.setTitle(new ChartTitle().setText(DetailTypeFormatter.format(selectedDetailType)), null);
            final String unit = DetailTypeFormatter.getUnit(getSelectedDetailType());
            final String label = unit.isEmpty() ? "" : "[" + unit + "]";
            if (!compactChart) {
                chart.getYAxis().setAxisTitleText(
                        DetailTypeFormatter.format(selectedDetailType) + " " + label);
            } else {
                chart.getYAxis().setAxisTitleText(label);
            }
            chart.getYAxis().setReversed(isYAxisReversed());
            final NumberFormat numberFormat = DetailTypeFormatter.getNumberFormat(selectedDetailType);
            chart.setToolTip(new ToolTip().setEnabled(true).setFormatter(new ToolTipFormatter() {
                @Override
                public String format(ToolTipData toolTipData) {
                    String seriesName = toolTipData.getSeriesName();

                    if (seriesName.equals(stringMessages.time())) {
                        return "<b>" + seriesName + ":</b> " + dateFormat.format(new Date(toolTipData.getXAsLong()))
                                + "<br/>(" + stringMessages.clickChartToSetTime() + ")";
                    } else {
                        return "<b>" + seriesName
                                + (toolTipData.getPointName() != null ? " " + toolTipData.getPointName() : "")
                                + "</b><br/>" + dateFormat.format(new Date(toolTipData.getXAsLong())) + ": "
                                + numberFormat.format(toolTipData.getYAsDouble()) + " " + unit;
                    }
                }
            }));
        }
        return hasDetailTypeChanged;
    }

    private boolean hasReversedYAxis(DetailType detailType) {
        return selectedDetailType == DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER ||
                selectedDetailType == DetailType.GAP_TO_LEADER_IN_SECONDS ||
                selectedDetailType == DetailType.RACE_RANK ||
                selectedDetailType == DetailType.REGATTA_RANK ||
                selectedDetailType == DetailType.OVERALL_RANK;
    }
    
    private boolean isYAxisReversed() {
        return hasReversedYAxis(selectedDetailType);
    }

    @Override
    public void onRaceSelectionChange(List<RegattaAndRaceIdentifier> selectedRaces) {
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            selectedRaceIdentifier = selectedRaces.iterator().next();
        } else {
            selectedRaceIdentifier = null;
        }

        clearChart();
        if (selectedRaceIdentifier != null) {
            timeChanged(timer.getTime(), null);
        }
    }

    /**
     * Checks the relation of the mark passings to the selection range.
     * 
     * @param markPassingInRange
     *            A Boolean matrix filled by
     *            {@link AbstractCompetitorRaceChart#fillPotentialXValues(double, double, ArrayList, ArrayList, ArrayList)
     *            fillPotentialXValues(...)}
     * @return A pair of Booleans. Value A contains false if a passing is not in the selection (error), so that the
     *         selection range needs to be refactored. Value B returns true if two passings are in range before the
     *         error happened or false, if the error happens before two passings were in the selection. B can be
     *         <code>null</code>.
     */
    public com.sap.sse.common.Util.Pair<Boolean, Boolean> checkPassingRelationToSelection(ArrayList<ArrayList<Boolean>> markPassingInRange) {
        boolean everyPassingInRange = true;
        Boolean twoPassingsInRangeBeforeError = null;
        ArrayList<Boolean> competitorPassings = markPassingInRange.get(0);
        for (int i = 0; i < competitorPassings.size(); i++) {
            Boolean passingInRange = competitorPassings.get(i);
            for (int j = 1; j < markPassingInRange.size(); j++) {
                Boolean passingToCompare = markPassingInRange.get(j).get(i);
                if (passingInRange != null) {
                    if (passingToCompare != null && everyPassingInRange) {
                        everyPassingInRange = passingInRange.equals(passingToCompare);
                        if (passingInRange && passingToCompare) {
                            twoPassingsInRangeBeforeError = true;
                        }
                    } else if (passingToCompare != null) {
                        if (passingInRange && passingToCompare) {
                            twoPassingsInRangeBeforeError = false;
                        }
                    }
                } else {
                    passingInRange = passingToCompare;
                }
            }
        }

        return new com.sap.sse.common.Util.Pair<Boolean, Boolean>(everyPassingInRange, twoPassingsInRangeBeforeError);
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        if (!isVisible()) {
            return;
        }

        if (allowTimeAdjust) {
            updateTimePlotLine(newTime);
        }
        
        switch (timer.getPlayMode()) {
        case Live: {
            // is date before first cache entry or is cache empty?
            if (timeOfEarliestRequestInMillis == null || newTime.getTime() < timeOfEarliestRequestInMillis) {
                updateChart(timeRangeWithZoomProvider.getFromTime(), newTime, /* append */true);
            } else if (newTime.getTime() > timeOfLatestRequestInMillis) {
                updateChart(new Date(timeOfLatestRequestInMillis), timeRangeWithZoomProvider.getToTime(), /* append */true);
            }
            // otherwise the cache spans across date and so we don't need to load anything
            break;
        }
        case Replay: {
            if (timeOfLatestRequestInMillis == null) {
                // pure replay mode
                updateChart(timeRangeWithZoomProvider.getFromTime(), timeRangeWithZoomProvider.getToTime(), /* append */false);
            } else {
                // replay mode during live play
                if (timeOfEarliestRequestInMillis == null || newTime.getTime() < timeOfEarliestRequestInMillis) {
                    updateChart(timeRangeWithZoomProvider.getFromTime(), newTime, /* append */true);
                } else if (newTime.getTime() > timeOfLatestRequestInMillis) {
                    updateChart(new Date(timeOfLatestRequestInMillis), timeRangeWithZoomProvider.getToTime(), /* append */true);
                }
            }
            break;
        }
        }
    }

    private void updateTimePlotLine(Date date) {
        chart.getXAxis().removePlotLine(timePlotLine);
        timePlotLine.setValue(date.getTime());
        chart.getXAxis().addPlotLines(timePlotLine);
    }

    @Override
    public void onResize() {
        chart.setSizeToMatchContainer();
        // it's important here to recall the redraw method, otherwise the bug fix for wrong checkbox positions
        // (nativeAdjustCheckboxPosition)
        // in the BaseChart class would not be called
        chart.redraw();
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
        timeChanged(timer.getTime(), null);
    }
    
    @Override
    public void filteredCompetitorsListChanged(Iterable<CompetitorDTO> filteredCompetitors) {
        timeChanged(timer.getTime(), null);
    }

    @Override
    public void filterChanged(FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> oldFilterSet,
            FilterSet<CompetitorDTO, ? extends Filter<CompetitorDTO>> newFilterSet) {
        // nothing to do; if it changes the filtered competitor list, a separate call to filteredCompetitorsListChanged will occur
    }
}
