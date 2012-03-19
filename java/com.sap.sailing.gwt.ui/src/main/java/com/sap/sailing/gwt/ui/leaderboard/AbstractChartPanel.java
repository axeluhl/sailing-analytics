package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Extremes;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.events.ChartClickEvent;
import org.moxieapps.gwt.highcharts.client.events.ChartClickEventHandler;
import org.moxieapps.gwt.highcharts.client.events.ChartSelectionEvent;
import org.moxieapps.gwt.highcharts.client.events.ChartSelectionEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.ScatterPlotOptions;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.actions.GetCompetitorsRaceDataAction;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.CompetitorSelectionProvider;
import com.sap.sailing.gwt.ui.client.DetailTypeFormatter;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceSelectionProvider;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.MultiCompetitorRaceDataDTO;
import com.sap.sailing.gwt.ui.shared.components.Component;
import com.sap.sailing.gwt.ui.shared.panels.BusyIndicator;
import com.sap.sailing.gwt.ui.shared.panels.SimpleBusyIndicator;

/**
 * ChartPanel is a GWT panel that can show one sort of competitor data (e.g. current speed over ground, windward distance to
 * leader) for different races in a chart.
 * 
 * When calling the constructor a chart is created that creates a final amount of series (so the maximum number of
 * competitors cannot be changed in one chart) which are connected to competitors, when the sailing service returns the
 * data. So {@code seriesID, competitorID and markSeriesID} are linked with the index. So if u know for example the
 * seriesID-index, you can get the competitor by calling competitorID.get(index).
 * 
 * @author Benjamin Ebling (D056866), Axel Uhl (d043530)
 * 
 */
public abstract class AbstractChartPanel<SettingsType extends ChartSettings> extends SimplePanel
implements CompetitorSelectionChangeListener, RaceSelectionChangeListener, TimeListener, RequiresResize {
    protected static final int LINE_WIDTH = 1;
    protected MultiCompetitorRaceDataDTO chartData;
    protected final SailingServiceAsync sailingService;
    protected final AsyncActionsExecutor asyncActionsExecutor;
    protected final ErrorReporter errorReporter;
    protected Chart chart;
    private boolean compactChart;
    protected final AbsolutePanel busyIndicatorPanel;
    protected final Label noCompetitorsSelectedLabel;
    protected final Map<CompetitorDTO, Series> dataSeriesByCompetitor;
    protected final Map<CompetitorDTO, Series> markPassingSeriesByCompetitor;
    private Series timeLineSeries;
    private boolean timeLineNeedsUpdate = true;
    private final boolean allowTimeAdjust;
    private boolean ignoreTimeAdjustOnce;
    protected final RaceSelectionProvider raceSelectionProvider;
    protected long stepSize = 5000;
    protected final StringMessages stringMessages;
    protected final Timer timer;
    protected final DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
    protected DetailType dataToShow;
    protected final CompetitorSelectionProvider competitorSelectionProvider;

    public AbstractChartPanel(SailingServiceAsync sailingService, AsyncActionsExecutor asyncActionsExecutor,
            CompetitorSelectionProvider competitorSelectionProvider, RaceSelectionProvider raceSelectionProvider,
            Timer timer, final StringMessages stringMessages, ErrorReporter errorReporter, DetailType dataToShow,
            boolean compactChart, boolean allowTimeAdjust) {
        this.stringMessages = stringMessages;
    	dataSeriesByCompetitor = new HashMap<CompetitorDTO, Series>();
        markPassingSeriesByCompetitor = new HashMap<CompetitorDTO, Series>();
    	this.timer = timer;
    	this.timer.addTimeListener(this);
    	this.competitorSelectionProvider = competitorSelectionProvider;
    	competitorSelectionProvider.addCompetitorSelectionChangeListener(this);
    	this.errorReporter = errorReporter;
        this.dataToShow = dataToShow;
        chartData = null;
        this.compactChart = compactChart;
        this.sailingService = sailingService;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.raceSelectionProvider = raceSelectionProvider;
        this.allowTimeAdjust = allowTimeAdjust;
        raceSelectionProvider.addRaceSelectionChangeListener(this);
        setSize("100%", "100%");

        noCompetitorsSelectedLabel = new Label(stringMessages.selectAtLeastOneCompetitor() + ".");
        noCompetitorsSelectedLabel.setStyleName("abstractChartPanel-importantMessageOfChart");
        
        chart = createChart(dataToShow);
        if (allowTimeAdjust) {
            timeLineSeries = createTimeLineSeries();
        }
        
        busyIndicatorPanel = new AbsolutePanel();
        final BusyIndicator busyIndicator = new SimpleBusyIndicator(/*busy*/ true, /*scale*/ 1);
        //Adding the busyIndicator with an scheduler, to be sure that the busyIndicatorPanel has a width and a height
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                busyIndicatorPanel.setSize("100%", "100%");
                busyIndicatorPanel.add(busyIndicator, busyIndicatorPanel.getOffsetWidth() / 2, busyIndicatorPanel.getOffsetHeight() / 2);
            }
        });

        List<RaceIdentifier> selectedRaces = raceSelectionProvider.getSelectedRaces();
        if(!selectedRaces.isEmpty()) {
            loadData(true);
        }
        timer.addTimeListener(this);
    }

    protected void selectRace(final RaceIdentifier selectedRace) {
    }
    
    /**
     * Creates a new chart for the given {@link DetailType} <code>dataToShow</code> and also 
     * clears the {@link #chartData}, the {@link #dataSeriesByCompetitor} and the {@link #markPassingSeriesByCompetitor}.
     * @param dataToShow The detail type for the new chart.
     * @return A chart for the given detail Type
     */
    private Chart createChart(DetailType dataToShow) {
        Chart chart = new Chart().setZoomType(Chart.ZoomType.X)
                .setSpacingRight(20)
                .setWidth100()
                .setHeight100()
                .setChartSubtitle(new ChartSubtitle().setText(stringMessages.clickAndDragToZoomIn()))
                .setLegend(new Legend().setEnabled(true).setBorderWidth(0))
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(LINE_WIDTH).setMarker(new Marker().setEnabled(false).setHoverState(
                                                new Marker().setEnabled(true).setRadius(4))).setShadow(false)
                                .setHoverStateLineWidth(LINE_WIDTH));
        chart.setChartTitle(new ChartTitle().setText(DetailTypeFormatter.format(dataToShow, stringMessages)));
        
        if (allowTimeAdjust) {
            chart.setClickEventHandler(new ChartClickEventHandler() {
                @Override
                public boolean onClick(ChartClickEvent chartClickEvent) {
                    if (ignoreTimeAdjustOnce) {
                        ignoreTimeAdjustOnce = false;
                    } else {
                        timer.setPlayMode(PlayModes.Replay);
                        timer.setTime(chartClickEvent.getXAxisValueAsLong());
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
                        ignoreTimeAdjustOnce = true;
                    } catch (Throwable t) {
                        // Redrawing, or the chart wouldn't rezoom
                        AbstractChartPanel.this.chart.redraw();
                    }
                    return true;
                }
            });
        }
        final String unit = getUnit();
        if(!compactChart)
            chart.getYAxis().setAxisTitleText(DetailTypeFormatter.format(dataToShow, stringMessages) + " ["+unit+"]");
        else
            chart.getYAxis().setAxisTitleText("["+unit+"]");
        chart.getYAxis().setStartOnTick(false).setShowFirstLabel(false);
        chart.getYAxis().setReversed((dataToShow == DetailType.WINDWARD_DISTANCE_TO_OVERALL_LEADER || 
                                      dataToShow == DetailType.GAP_TO_LEADER_IN_SECONDS) ? true : false);
        chart.getXAxis().setType(Axis.Type.DATE_TIME).setMaxZoom(10000) // ten seconds
                .setAxisTitleText(stringMessages.time());
        String decimalPlaces = "";
        for (int i = 0; i < dataToShow.getPrecision(); i++) {
            if (i == 0) {
                decimalPlaces += ".";
            }
            decimalPlaces += "0";
        }
        final NumberFormat numberFormat = NumberFormat.getFormat("0" + decimalPlaces);
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
                            + numberFormat.format(toolTipData.getYAsDouble()) + unit;
                }
            }
        }));
        
        if (compactChart) {
            chart.setSpacingBottom(10).setSpacingLeft(10).setSpacingRight(10).setSpacingTop(2)
                 .setOption("legend/margin", 2)
                 .setOption("title/margin", 5)
                 .setChartSubtitle(null)
                 .getXAxis().setAxisTitleText(null);
        }
        
        setChartData(null);
        dataSeriesByCompetitor.clear();
        markPassingSeriesByCompetitor.clear();
        return chart;
    }

    protected abstract Component<SettingsType> getComponent();
    
    /**
     * Loads the needed data (data which isn't in the {@link #chartData cache}) for the {@link #getVisibleCompetitors()
     * visible competitors} via
     * {@link SailingServiceAsync#getCompetitorsRaceData(RaceIdentifier, List, long, DetailType, AsyncCallback)}. After
     * loading is the method {@link #drawChartData()} called.<br />
     * If no data needs to be {@link #needsDataLoading() loaded}, the no competitors selected label is displayed.
     * 
     * @param showBusyIndicator If <code>true</code> is the busy indicator shown while loading the data from the server.
     */
    protected void loadData(boolean showBusyIndicator) {
        if (needsDataLoading()) {
            if (showBusyIndicator) {
                setWidget(busyIndicatorPanel);
            }
            if (chartData == null || chartData.getDetailType() != getDataToShow()) {
                chartData = new MultiCompetitorRaceDataDTO(getDataToShow());
            }
            
            Date toDate = new Date(System.currentTimeMillis() - timer.getLivePlayDelayInMillis());
            final ArrayList<Pair<Date, CompetitorDTO>> dataQuery = new ArrayList<Pair<Date, CompetitorDTO>>();
            for (CompetitorDTO competitor : getVisibleCompetitors()) {
                Date chartDataDateOfNewestData = chartData.getDateOfNewestData();
                Date competitorDateOfNewestData = chartData.contains(competitor) ? chartData.getCompetitorData(competitor).getDateOfNewestData() : null;
                if (!chartData.contains(competitor)) {
                    dataQuery.add(new Pair<Date, CompetitorDTO>(new Date(0), competitor));
                } else if (competitorDateOfNewestData.before(chartDataDateOfNewestData) || competitorDateOfNewestData.before(toDate)) {
                    dataQuery.add(new Pair<Date, CompetitorDTO>(new Date(competitorDateOfNewestData.getTime() + getStepSize()), competitor));
                }
            }
            
            GetCompetitorsRaceDataAction getCompetitorsRaceDataAction = new GetCompetitorsRaceDataAction(sailingService,
                        getSelectedRace(), dataQuery, toDate, getStepSize(), getDataToShow(),
                    new AsyncCallback<MultiCompetitorRaceDataDTO>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            if(timer.getPlayMode() != PlayModes.Live)
                                errorReporter.reportError(getStringMessages().failedToLoadRaceData() + ": " + caught.toString());
                        }

                        @Override
                        public void onSuccess(MultiCompetitorRaceDataDTO result) {
                            if (result != null) {
                                for (CompetitorRaceDataDTO competitorData : result.getAllRaceData()) {
                                    if (chartData.contains(competitorData.getCompetitor())) {
                                        chartData.addCompetitorRaceData(competitorData);
                                        chartData.setCompetitorMarkPassingsData(competitorData);
                                    } else {
                                        chartData.setCompetitorData(competitorData.getCompetitor(), competitorData);
                                    }
                                }
                            }
                            drawChartData();
                            setWidget(chart);
                        }
                    });
                    asyncActionsExecutor.execute(getCompetitorsRaceDataAction);
        } else {
            setWidget(noCompetitorsSelectedLabel);
        }
    }

    private RaceIdentifier getSelectedRace() {
        RaceIdentifier result = null;
        List<RaceIdentifier> selectedRaces = raceSelectionProvider.getSelectedRaces();
        if (selectedRaces != null && !selectedRaces.isEmpty()) {
            result = selectedRaces.iterator().next();
        }
        return result;
    }

    @Override
    public void addedToSelection(CompetitorDTO competitor) {
        timeLineNeedsUpdate = true;
        loadData(true);
    }
    
    @Override
    public void removedFromSelection(CompetitorDTO competitor) {
        Series competitorSeries = getCompetitorSeries(competitor);
        if (competitorSeries != null) {
            chart.removeSeries(competitorSeries);
        }
        Series competitorMarkPassingSeries = getCompetitorMarkPassingSeries(competitor);
        if (competitorMarkPassingSeries != null) {
            chart.removeSeries(competitorMarkPassingSeries);
        }
        if (!hasVisibleCompetitors()) {
            setWidget(noCompetitorsSelectedLabel);
        }
    }
    
    /**
     * Creates the series for all selected competitors if these aren't created yet.<br />
     * Fills the series for the selected competitors with the data in {@link AbstractChartPanel#chartData}.<br />
     * Removes series of competitors, which aren't selected and adds series for competitors, which are newly selected.<br />
     * Also {@link #forceTimeLineUpdate() updates} the {@link #timeLineSeries} if {@link #timeLineNeedsUpdate} is <code>true</code>.<br /><br />
     * 
     * The data for all {@link #getVisibleCompetitors() visible competitors} needs to be filled before calling this method.
     */
    private synchronized void drawChartData() {
        //Make sure the busy indicator is removed at this point, or plotting the data results in an exception
        setWidget(chart);
        Iterable<CompetitorDTO> competitors = competitorSelectionProvider.getAllCompetitors();
        if (chartData != null && competitors != null && competitors.iterator().hasNext()) {
            List<Series> chartSeries = Arrays.asList(chart.getSeries());
            for (CompetitorDTO competitor : competitors) {
                Series compSeries = getCompetitorSeries(competitor);
                Series markSeries = getCompetitorMarkPassingSeries(competitor);
                if (isCompetitorVisible(competitor)) {
                    CompetitorRaceDataDTO competitorData = chartData.getCompetitorData(competitor);
                    if (competitorData != null) {
                        Date toDate = new Date(System.currentTimeMillis() - timer.getLivePlayDelayInMillis());
                        
                        List<Triple<String, Date, Double>> markPassingsData = competitorData.getMarkPassingsData();
                        List<Point> markPassingPoints = new ArrayList<Point>();
                        for (Triple<String, Date, Double> markPassingData : markPassingsData) {
                            if (markPassingData.getB() != null && markPassingData.getC() != null) {
                                if (markPassingData.getB().before(toDate)) {
                                    Point markPassingPoint = new Point(markPassingData.getB().getTime(),
                                            markPassingData.getC());
                                    markPassingPoint.setName(markPassingData.getA());
                                    markPassingPoints.add(markPassingPoint);
                                }
                            }
                        }
                        markSeries.setPoints(markPassingPoints.toArray(new Point[0]));
                        
                        Point[] compSeriesPoints = compSeries.getPoints();
                        Date dateOfNewestSeriesPoint = compSeriesPoints.length == 0 ? null :
                            new Date(compSeriesPoints[compSeriesPoints.length - 1].getX().longValue());
                        List<Pair<Date, Double>> raceData = dateOfNewestSeriesPoint == null ? competitorData
                                .getRaceData() : competitorData.getRaceDataAfterDate(dateOfNewestSeriesPoint);
                        for (Pair<Date, Double> data : raceData) {
                            if (data.getA() != null && data.getB() != null) {
                                if (data.getA().before(toDate)) {
                                    Point competitorPoint = new Point(data.getA().getTime(), data.getB());
                                    compSeries.addPoint(competitorPoint);
                                }
                            }
                        }
                        
                        //Adding the series if chart doesn't contain it
                        if (!chartSeries.contains(compSeries)) {
                            chart.addSeries(compSeries);
                            chart.addSeries(markSeries);
                        }
                    }
                } else {
                    //Removing the series if chart contains it
                    if (chartSeries.contains(compSeries)) {
                        chart.removeSeries(compSeries);
                        chart.removeSeries(markSeries);
                    }
                }
            }
            if (timeLineNeedsUpdate) {
                forceTimeLineUpdate();
            }
        }
    }

    private Iterable<CompetitorDTO> getVisibleCompetitors() {
        return competitorSelectionProvider.getSelectedCompetitors();
    }

    private boolean hasVisibleCompetitors() {
        return getVisibleCompetitors().iterator().hasNext();
    }

    private boolean isCompetitorVisible(CompetitorDTO competitor) {
        return competitorSelectionProvider.isSelected(competitor);
    }

    /**
     * 
     * @param competitor
     * @return A series in the chart, that can be used to show the data of a specific competitor.
     */
    private Series getCompetitorSeries(final CompetitorDTO competitor){
        Series result = dataSeriesByCompetitor.get(competitor);
    	if (result == null) {
    	    result = chart.createSeries().setType(Series.Type.LINE).setName(competitor.name);
            result.setPlotOptions(new LinePlotOptions()
                    .setLineWidth(LINE_WIDTH)
                    .setMarker(new Marker().setEnabled(false).setHoverState(new Marker().setEnabled(true).setRadius(4)))
                    .setShadow(false).setHoverStateLineWidth(LINE_WIDTH)
                    .setColor(competitorSelectionProvider.getColor(competitor)));
            dataSeriesByCompetitor.put(competitor, result);
    	}
    	return result;
    }

    private Series createTimeLineSeries() {
        return chart
                .createSeries()
                .setType(Series.Type.LINE)
                .setName(stringMessages.time())
                .setPlotOptions(new LinePlotOptions().setShowInLegend(false).setHoverStateEnabled(false).setLineWidth(2));
    }

    private String getUnit() {
        String unit = "";
        switch (getDataToShow()) {
        case CURRENT_SPEED_OVER_GROUND_IN_KNOTS:
            unit = getStringMessages().currentSpeedOverGroundInKnotsUnit();
            break;
        case DISTANCE_TRAVELED:
            unit = getStringMessages().distanceInMetersUnit();
            break;
        case GAP_TO_LEADER_IN_SECONDS:
            unit = getStringMessages().gapToLeaderInSecondsUnit();
            break;
        case VELOCITY_MADE_GOOD_IN_KNOTS:
            unit = getStringMessages().velocityMadeGoodInKnotsUnit();
            break;
        case WINDWARD_DISTANCE_TO_OVERALL_LEADER:
            unit = getStringMessages().windwardDistanceToGoInMetersUnit();
        }
        return unit;
    }
    
    /**
     * 
     * @param competitor
     * @return A series in the chart, that can be used to show the mark passings.
     */
    private Series getCompetitorMarkPassingSeries(CompetitorDTO competitor){
        Series result = markPassingSeriesByCompetitor.get(competitor);
        if (result == null) {
            result = chart.createSeries().setType(Series.Type.SCATTER).setName(stringMessages.markPassing()+" "+competitor.name);
            result.setPlotOptions(new ScatterPlotOptions().setColor(competitorSelectionProvider.getColor(competitor)));
            markPassingSeriesByCompetitor.put(competitor, result);
        }
        return result;
    }
    
    /**
     * Clears the whole chart and empties cached data.
     */
    protected void clearChart() {
        setChartData(null);
        dataSeriesByCompetitor.clear();
        markPassingSeriesByCompetitor.clear();
        chart.removeAllSeries();
    }

    public String getLocalizedShortName() {
        return DetailTypeFormatter.format(getDataToShow(), getStringMessages());
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
     * by subclasses thereof. Subclasses also need to call {@link #clearChart()} and {@link #loadData(boolean)}, if this method returns <code>true</code>;
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
    
    protected DetailType getDataToShow() {
        return this.dataToShow;
    }
    
    /**
     * Updates the {@link #dataToShow} field, creates a new chart for the new <code>dataToShow</code> and clears the {@link #chartData}.<br />
     * Doesn't {@link #loadData(boolean) load the data}.
     * 
     * @return <code>true</code> if the data to show changed
     */
    protected boolean setDataToShow(DetailType dataToShow) {
        if (dataToShow != this.dataToShow) {
            this.dataToShow = dataToShow;
            setChartData(null);
            chart = createChart(dataToShow);
            setWidget(chart);
            
            return true;
        } else {
            return false;
        }
    }
    
    protected MultiCompetitorRaceDataDTO getChartData() {
        return chartData;
    }
    
    protected void setChartData(MultiCompetitorRaceDataDTO chartData) {
        this.chartData = chartData;
    }
    
    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
        setChartData(null);
        clearChart();
        timeLineNeedsUpdate = true;
        loadData(true);
    }

    /**
     * Checks the relation of the mark passings to the selection range.
     * 
     * @param markPassingInRange
     *            A Boolean matrix filled by
     *            {@link AbstractChartPanel#fillPotentialXValues(double, double, ArrayList, ArrayList, ArrayList)
     *            fillPotentialXValues(...)}
     * @return A pair of Booleans. Value A contains false if a passing is not in the selection (error), so that the
     *         selection range needs to be refactored. Value B returns true if two passings are in range before the
     *         error happened or false, if the error happens before two passings were in the selection. B can be
     *         <code>null</code>.
     */
    public Pair<Boolean, Boolean> checkPassingRelationToSelection(ArrayList<ArrayList<Boolean>> markPassingInRange) {
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
        
        return new Pair<Boolean, Boolean>(everyPassingInRange, twoPassingsInRangeBeforeError);
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
        if (allowTimeAdjust && chart != null) {
            raceSelectionProvider.getSelectedRaces().get(0);
            
            Long x = date.getTime();
            Extremes extremes = chart.getYAxis(0).getExtremes();
            Point[] points = new Point[2];
            points[0] = new Point(x, extremes.getDataMin());
            points[1] = new Point(x, extremes.getDataMax());
            timeLineSeries.setPoints(points);
            ensureTimeLineIsVisible();
            timeLineNeedsUpdate = false;
        }
    }
    
    /**
     * Checks if the series of the chart contain the {@link #timeLineSeries}. If not, it is added to the chart.<br />
     * Throws a NPE if the chart is <code>null</code>.
     */
    private void ensureTimeLineIsVisible() {
        if (!Arrays.asList(chart.getSeries()).contains(timeLineSeries)) {
            chart.addSeries(timeLineSeries);
        }
    }

    @Override
    public void timeChanged(Date date) {
        if (getChartData() != null) {
            Date newestEvent = getChartData().getOldestDateOfNewestData();
            updateTimeLine(date);
            if ((newestEvent == null || (newestEvent.before(date) && (date.getTime() - newestEvent.getTime()) >= getStepSize()))) {
                loadData(false);
            }
        }
    }
    
    private boolean needsDataLoading() {
        return hasVisibleCompetitors() && isVisible();
    }

    @Override
    public void onResize() {
        if(getChartData() != null) {
            chart.setSizeToMatchContainer();
        }
    }

    @Override
    public void competitorsListChanged(Iterable<CompetitorDTO> competitors) {
        timeChanged(timer.getTime());
    }
    
    public void triggerDataLoading() {
        loadData(true);
    }
}
