package com.sap.sailing.gwt.ui.leaderboard;

import java.util.ArrayList;
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
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.ToolTipData;
import org.moxieapps.gwt.highcharts.client.ToolTipFormatter;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.ScatterPlotOptions;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.shared.GwtEvent;
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
import com.sap.sailing.gwt.ui.shared.CompetitorInRaceDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorsAndTimePointsDTO;
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
    protected CompetitorInRaceDTO chartData;
    protected CompetitorsAndTimePointsDTO competitorsAndTimePointsDTO = null;
    protected final SailingServiceAsync sailingService;
    protected final ErrorReporter errorReporter;
    protected Chart chart;
    protected final AbsolutePanel busyIndicatorPanel;
    protected final Label noCompetitorsSelectedLabel;
    protected final Map<CompetitorDTO, Series> seriesByCompetitor;
    protected final Map<CompetitorDTO, Series> markPassingSeriesByCompetitor;
    protected final RaceSelectionProvider raceSelectionProvider;
    protected long stepSize = 5000;
    protected final StringMessages stringMessages;
    protected final Set<Series> seriesIsUsed;
    protected final Timer timer;
    protected final DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
    protected DetailType dataToShow;
    protected final CompetitorSelectionProvider competitorSelectionProvider;

    public AbstractChartPanel(SailingServiceAsync sailingService,
            CompetitorSelectionProvider competitorSelectionProvider, RaceSelectionProvider raceSelectionProvider,
            Timer timer, final StringMessages stringMessages, ErrorReporter errorReporter, DetailType dataToShow) {
        this.stringMessages = stringMessages;
    	seriesByCompetitor = new HashMap<CompetitorDTO, Series>();
        markPassingSeriesByCompetitor = new HashMap<CompetitorDTO, Series>();
    	this.timer = timer;
    	this.competitorSelectionProvider = competitorSelectionProvider;
    	competitorSelectionProvider.addCompetitorSelectionChangeListener(this);
    	this.errorReporter = errorReporter;
    	chartData = new CompetitorInRaceDTO();
        this.dataToShow = dataToShow;
    	seriesIsUsed = new HashSet<Series>();
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

        setWidget(busyIndicatorPanel);
        List<RaceIdentifier> selectedRaces = raceSelectionProvider.getSelectedRaces();
        if(!selectedRaces.isEmpty()) {
            loadData();
        }
    }

    protected void selectRace(final RaceIdentifier selectedRace) {
    }
    
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
        return chart;
    }

    protected abstract Component<SettingsType> getComponent();
    
    protected void loadData() {
        setWidget(busyIndicatorPanel);
        if (getCompetitorsAndTimePointsDTO() != null) {
            doLoadData();
        } else {
            this.sailingService.getCompetitorsAndTimePoints(getSelectedRace(), getStepSize(),
                    new AsyncCallback<CompetitorsAndTimePointsDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(stringMessages.failedToLoadRaceInformation(caught.toString()));
                        }

                        @Override
                        public void onSuccess(CompetitorsAndTimePointsDTO result) {
                            setCompetitorsAndTimePointsDTO(result);
                            doLoadData();
                        }
                    });
        }
    }

    private void doLoadData() {
        final List<CompetitorDTO> competitorsToLoad = new ArrayList<CompetitorDTO>();
        // Assumption: for those competitors shown in chart we already have all data that's needed (TODO: what to do in live mode?)
        // Therefore, we only need to load race data for those to be shown but not yet in the chart. Find them:
        for (CompetitorDTO competitor : competitorSelectionProvider.getAllCompetitors()) {
            if (isCompetitorVisible(competitor) && !seriesByCompetitor.keySet().contains(competitor)) {
                competitorsToLoad.add(competitor);
            }
        }
        if (competitorsToLoad != null && !competitorsToLoad.isEmpty()) {
            final CompetitorsAndTimePointsDTO competitorsAndTimePointsToLoad = new CompetitorsAndTimePointsDTO(
                    getStepSize());
            competitorsAndTimePointsToLoad.setStartTime(getCompetitorsAndTimePointsDTO().getStartTime());
            competitorsAndTimePointsToLoad.setTimePointOfNewestEvent(getCompetitorsAndTimePointsDTO()
                    .getTimePointOfNewestEvent());
            for (CompetitorDTO competitor : competitorsToLoad) {
                competitorsAndTimePointsToLoad.setMarkPassings(competitor, getCompetitorsAndTimePointsDTO()
                        .getMarkPassings(competitor));
            }
            competitorsAndTimePointsToLoad.setCompetitors(competitorsToLoad.toArray(new CompetitorDTO[0]));
            AbstractChartPanel.this.sailingService.getCompetitorRaceData(getSelectedRace(),
                    competitorsAndTimePointsToLoad, getDataToShow(), new AsyncCallback<CompetitorInRaceDTO>() {
                        @Override
                        public void onFailure(Throwable caught) {
                            errorReporter.reportError(getStringMessages().failedToLoadRaceData() + ": "
                                    + caught.toString());
                        }

                        @Override
                        public void onSuccess(CompetitorInRaceDTO result) {
                            fireEvent(new DataLoadedEvent());
                            for (CompetitorDTO competitor : competitorsToLoad) {
                                chartData.setRaceData(competitor, result.getRaceData(competitor));
                                chartData.setMarkPassingData(competitor, result.getMarkPassings(competitor));
                            }
                            updateTableData(competitorsAndTimePointsToLoad.getCompetitors());
                            setWidget(chart);
                        }
                    });
        } else {
            //No data is needed to load, so display the selected competitors
            //This wouldn't be done by the AsyncCallbacks, because it never reaches the onSuccess-Method
            for (CompetitorDTO competitor : competitorSelectionProvider.getSelectedCompetitors()) {
                competitorsToLoad.add(competitor);
            }
            if (!competitorsToLoad.isEmpty()) {
                setWidget(chart);
                updateTableData(competitorsToLoad.toArray(new CompetitorDTO[0]));
            } else {
                setWidget(noCompetitorsSelectedLabel);
            }
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
        loadData();
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
        if (competitorSelectionProvider.getSelectedCompetitors() == null
                || !competitorSelectionProvider.getSelectedCompetitors().iterator().hasNext()) {
            setWidget(noCompetitorsSelectedLabel);
        }
    }
    
    private synchronized void updateTableData(CompetitorDTO[] competitorDTOs) {
        //Make sure the busy indicator is removed at this point, or plotting the data results in an exception
        setWidget(chart);
        if (getCompetitorsAndTimePointsDTO() != null && chartData != null) {
            //Clearing the series to keep the chart clean
            chart.removeAllSeries();
            for (CompetitorDTO competitor : competitorDTOs) {
                Series compSeries = getCompetitorSeries(competitor);
                seriesIsUsed.add(compSeries);
                Series markSeries = getCompetitorMarkPassingSeries(competitor);
                seriesIsUsed.add(markSeries);
                if (isCompetitorVisible(competitor) && chartData.getRaceData(competitor) != null) {
                    long starttime = System.currentTimeMillis();
                    Pair<String, Long>[] markPassingTimes = getCompetitorsAndTimePointsDTO().getMarkPassings(competitor);
                    List<Point> markPassingPoints = new ArrayList<Point>();
                    Double[] markPassingValues = chartData.getMarkPassings(competitor);
                    for (int j = 0; j < markPassingTimes.length; j++) {
                        if (markPassingValues[j] != null && markPassingTimes[j].getB() != null) {
                            Point markPassingPoint = new Point(markPassingTimes[j].getB(), markPassingValues[j]);
                            markPassingPoint.setName(markPassingTimes[j].getA());
                            markPassingPoints.add(markPassingPoint);
                        }
                    }
                    markSeries.setPoints(markPassingPoints.toArray(new Point[0]));
                    GWT.log("Update mark passings time for " + competitor.name + ": "
                            + (System.currentTimeMillis() - starttime));
                    starttime = System.currentTimeMillis();
                    Double[] data = chartData.getRaceData(competitor);
                    List<Long> timepoints = getCompetitorsAndTimePointsDTO().getTimePoints();
                    List<Point> competitorPoints = new ArrayList<Point>();
                    for (int j = 0; j < timepoints.size(); j++) {
                        if (data[j] != null) {
                            Point competitorPoint = new Point(timepoints.get(j), data[j]);
                            competitorPoints.add(competitorPoint);
                        }
                    }
                    compSeries.setPoints(competitorPoints.toArray(new Point[0]));
                    GWT.log("Update data time for " + competitor.name + ": " + (System.currentTimeMillis() - starttime));
                }
                if (isCompetitorVisible(competitor)) {
                    chart.addSeries(compSeries);
                    chart.addSeries(markSeries);
                } else {
                    chart.removeSeries(compSeries);
                    chart.removeSeries(markSeries);
                }
            }
        }
    }

    // DataLoaded event handling.
    public void addDataLoadedHandler(DataLoadedHandler handler) {
        this.addHandler(handler, DataLoadedEvent.TYPE);
    }

    public interface DataLoadedHandler extends com.google.gwt.event.shared.EventHandler {
        public void onDataLoaded(DataLoadedEvent event);
    }

    public static class DataLoadedEvent extends GwtEvent<DataLoadedHandler> {
        public static Type<DataLoadedHandler> TYPE = new Type<DataLoadedHandler>();

        public DataLoadedEvent() {
            super();
        }

        @Override
        protected void dispatch(DataLoadedHandler handler) {
            handler.onDataLoaded(this);
        }

        @Override
        public com.google.gwt.event.shared.GwtEvent.Type<DataLoadedHandler> getAssociatedType() {
            return TYPE;
        }
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
        Series result = seriesByCompetitor.get(competitor);
    	if (result == null) {
    	    result = chart.createSeries().setType(Series.Type.LINE).setName(competitor.name);
            result.setPlotOptions(new LinePlotOptions()
                    .setLineWidth(LINE_WIDTH)
                    .setMarker(new Marker().setEnabled(false).setHoverState(new Marker().setEnabled(true).setRadius(4)))
                    .setShadow(false).setHoverStateLineWidth(LINE_WIDTH)
                    .setColor(competitorSelectionProvider.getColor(competitor)));
            seriesByCompetitor.put(competitor, result);
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
     * 
     * @param clearCheckBoxes Declares whether the checkboxes for the visibility of the competitors should be cleared too. Should be true, when you change the race to show.
     */
    protected void clearChart(boolean clearCheckBoxes) {
        if (clearCheckBoxes) {
            seriesIsUsed.clear();
        }
        seriesByCompetitor.clear();
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
     * by subclasses thereof. Subclasses also need to call {@link #clearChart(boolean)} and {@link #loadData()} after
     * updating all settings.
     */
    protected void updateSettingsOnly(ChartSettings newSettings) {
        setStepSize(newSettings.getStepSize());
        setCompetitorsAndTimePointsDTO(null);
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
     * Updates the {@link #dataToShow} field but does not yet {@link #clearChart(boolean) clear the chart} nor
     * {@link #loadData load the data}.
     */
    protected void setDataToShow(DetailType dataToShow) {
        if (dataToShow != this.dataToShow) {
            this.dataToShow = dataToShow;
            chart = createChart(dataToShow);
            this.setWidget(chart);
        }
    }

    protected CompetitorsAndTimePointsDTO getCompetitorsAndTimePointsDTO() {
        return competitorsAndTimePointsDTO;
    }

    protected void setCompetitorsAndTimePointsDTO(CompetitorsAndTimePointsDTO competitorsAndTimePointsDTO) {
        this.competitorsAndTimePointsDTO = competitorsAndTimePointsDTO;
    }

    @Override
    public void onRaceSelectionChange(List<RaceIdentifier> selectedRaces) {
      setCompetitorsAndTimePointsDTO(null);
      clearChart(true);
      loadData();
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
        if (timer.getPlayMode() == PlayModes.Live) {
            // TODO fetch parts missing so far from cache 
        } else {
            // TODO check if fetched already; if not, fetch all
        }
    }
}
