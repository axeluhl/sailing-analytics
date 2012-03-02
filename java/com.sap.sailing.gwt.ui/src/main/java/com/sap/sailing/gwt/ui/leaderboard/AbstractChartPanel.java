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
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.DetailType;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.domain.common.impl.Util.Triple;
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
implements CompetitorSelectionChangeListener, RaceSelectionChangeListener, TimeListener {
    protected static final int LINE_WIDTH = 1;
    protected MultiCompetitorRaceDataDTO chartData;
    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    protected Chart chart;
    protected final AbsolutePanel busyIndicatorPanel;
    protected final Label noCompetitorsSelectedLabel;
    protected final Map<CompetitorDTO, Series> dataSeriesByCompetitor;
    protected final Map<CompetitorDTO, Series> markPassingSeriesByCompetitor;
    protected final RaceSelectionProvider raceSelectionProvider;
    protected long stepSize = 5000;
    protected final StringMessages stringMessages;
    protected final Timer timer;
    protected final DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
    protected DetailType dataToShow;
    protected final CompetitorSelectionProvider competitorSelectionProvider;

    public AbstractChartPanel(SailingServiceAsync sailingService,
            CompetitorSelectionProvider competitorSelectionProvider, RaceSelectionProvider raceSelectionProvider,
            Timer timer, final StringMessages stringMessages, ErrorReporter errorReporter, DetailType dataToShow) {
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
        this.sailingService = sailingService;
        this.raceSelectionProvider = raceSelectionProvider;
        raceSelectionProvider.addRaceSelectionChangeListener(this);

        noCompetitorsSelectedLabel = new Label(stringMessages.selectAtLeastOneCompetitor() + ".");
        noCompetitorsSelectedLabel.setStyleName("abstractChartPanel-importantMessageOfChart");
        
        chart = createChart(dataToShow);
        
        busyIndicatorPanel = new AbsolutePanel();
        final BusyIndicator busyIndicator = new SimpleBusyIndicator(/*busy*/ true, /*scale*/ 1);
        //Adding the busyIndicator with an scheduler, to be sure that the busyIndicatorPanel has a width and a height
        Scheduler.get().scheduleDeferred(new ScheduledCommand() {
            @Override
            public void execute() {
                busyIndicatorPanel.setSize("100%", "400px");
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
                .setChartSubtitle(new ChartSubtitle().setText(stringMessages.clickAndDragToZoomIn()))
                .setLegend(new Legend().setEnabled(true))
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(LINE_WIDTH).setMarker(new Marker().setEnabled(false).setHoverState(
                                                new Marker().setEnabled(true).setRadius(4))).setShadow(false)
                                .setHoverStateLineWidth(LINE_WIDTH));
        chart.setChartTitle(new ChartTitle().setText(DetailTypeFormatter.format(dataToShow, stringMessages)));
        final String unit = getUnit();
        chart.getYAxis().setAxisTitleText(DetailTypeFormatter.format(dataToShow, stringMessages) + " ["+unit+"]");
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
                return "<b>" + toolTipData.getSeriesName() + (toolTipData.getPointName() != null ? " "+toolTipData.getPointName() : "")
                        + "</b><br/>" +  
                        dateFormat.format(new Date(toolTipData.getXAsLong())) + ": " +
                        numberFormat.format(toolTipData.getYAsDouble()) + unit;
            }
        }));
        
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
     * If no competitor is visible, is the {@link #noCompetitorsSelectedLabel} displayed.
     * 
     * @param showBusyIndicator If <code>true</code> is the busy indicator shown while loading the data from the server.
     */
    protected void loadData(boolean showBusyIndicator) {
        if (hasVisibleCompetitors()) {
            if (showBusyIndicator) {
                setWidget(busyIndicatorPanel);
            }
            if (chartData == null || chartData.getDetailType() != getDataToShow()) {
                chartData = new MultiCompetitorRaceDataDTO(getDataToShow());
            }
            
            final ArrayList<Pair<Date, CompetitorDTO>> dataQuery = new ArrayList<Pair<Date, CompetitorDTO>>();
            for (CompetitorDTO competitor : getVisibleCompetitors()) {
                Date chartDataDateOfNewestData = chartData.getDateOfNewestData();
                if (!chartData.contains(competitor)) {
                    dataQuery.add(new Pair<Date, CompetitorDTO>(new Date(0), competitor));
                } else if (chartData.getCompetitorData(competitor).getDateOfNewestData().before(chartDataDateOfNewestData)) {
                    Date competitorDateOfNewestData = chartData.getCompetitorData(competitor).getDateOfNewestData();
                    dataQuery.add(new Pair<Date, CompetitorDTO>(new Date(competitorDateOfNewestData.getTime() + getStepSize()), competitor));
                }
            }
            
            sailingService.getCompetitorsRaceData(getSelectedRace(), dataQuery, new Date(System.currentTimeMillis()
                    - timer.getLivePlayDelayInMillis()), getStepSize(), getDataToShow(),
                    new AsyncCallback<MultiCompetitorRaceDataDTO>() {

                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(getStringMessages().failedToLoadRaceData() + ": "
                                    + caught.toString());
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
     * Removes series of competitors, which aren't selected and adds series for competitors, which are newly selected.<br /><br />
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
                        List<Triple<String, Date, Double>> markPassingsData = competitorData.getMarkPassingsData();
                        List<Point> markPassingPoints = new ArrayList<Point>();
                        for (Triple<String, Date, Double> markPassingData : markPassingsData) {
                            if (markPassingData.getB() != null && markPassingData.getC() != null) {
                                Point markPassingPoint = new Point(markPassingData.getB().getTime(), markPassingData.getC());
                                markPassingPoint.setName(markPassingData.getA());
                                markPassingPoints.add(markPassingPoint);
                            }
                        }
                        markSeries.setPoints(markPassingPoints.toArray(new Point[0]));
                        
                        Point[] compSeriesPoints = compSeries.getPoints();
                        Date dateOfNewestSeriesPoint = compSeriesPoints.length == 0 ? new Date(0) : new Date(
                                compSeriesPoints[compSeriesPoints.length - 1].getX().longValue());
                        List<Pair<Date, Double>> raceData = competitorData.getRaceDataAfterDate(dateOfNewestSeriesPoint);
                        for (Pair<Date, Double> data : raceData) {
                            if (data.getA() != null && data.getB() != null) {
                                Point competitorPoint = new Point(data.getA().getTime(), data.getB());
                                compSeries.addPoint(competitorPoint);
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

    @Override
    public void timeChanged(Date date) {
        if (getChartData() != null) {
            Date newestEvent = getChartData().getOldestDateOfNewestData();
            if (hasVisibleCompetitors()
                    && (newestEvent == null || (newestEvent.before(date) && (date.getTime() - newestEvent.getTime()) >= getStepSize()))) {
                loadData(false);
            }
        }
    }
}
