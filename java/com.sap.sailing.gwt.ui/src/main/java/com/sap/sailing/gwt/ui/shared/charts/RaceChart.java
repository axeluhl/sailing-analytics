package com.sap.sailing.gwt.ui.shared.charts;

import java.util.Date;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.XAxis;
import org.moxieapps.gwt.highcharts.client.events.ChartClickEvent;
import org.moxieapps.gwt.highcharts.client.events.ChartSelectionEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesCheckboxClickEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesCheckboxClickEventHandler;
import org.moxieapps.gwt.highcharts.client.events.SeriesLegendItemClickEvent;
import org.moxieapps.gwt.highcharts.client.events.SeriesLegendItemClickEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.PlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.RaceIdentifier;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.actions.AsyncActionsExecutor;
import com.sap.sailing.gwt.ui.client.ErrorReporter;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.RaceTimesCalculationUtil;
import com.sap.sailing.gwt.ui.client.RaceTimesInfoProviderListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.TimeListener;
import com.sap.sailing.gwt.ui.client.TimeZoomChangeListener;
import com.sap.sailing.gwt.ui.client.TimeZoomProvider;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.client.Timer.PlayModes;
import com.sap.sailing.gwt.ui.shared.RaceTimesInfoDTO;

public abstract class RaceChart extends SimplePanel implements RaceTimesInfoProviderListener, RaceSelectionChangeListener,
    TimeListener, TimeZoomChangeListener {
    protected Chart chart;

    protected final Timer timer;
    protected final TimeZoomProvider timeZoomProvider; 
    
    protected Date minTimepoint;
    protected Date maxTimepoint;
    
    protected boolean ignoreClickOnce;

    protected RaceIdentifier selectedRaceIdentifier;
    protected RaceTimesInfoDTO lastRaceTimesInfo;

    protected final DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
    protected final DateTimeFormat dateFormatHoursMinutes = DateTimeFormat.getFormat("HH:mm");

    protected final StringMessages stringMessages;
    protected final ErrorReporter errorReporter;
    protected final AsyncActionsExecutor asyncActionsExecutor;
    protected final SailingServiceAsync sailingService;

    protected boolean isLoading = false;
    protected boolean isZoomed = false;
    
    /** the tick count must be the same as TimeSlider.TICKCOUNT, otherwise the time ticks will be not synchronized */  
    private final int TICKCOUNT = 10;
    
    public RaceChart(SailingServiceAsync sailingService, Timer timer, TimeZoomProvider timeZoomProvider, final StringMessages stringMessages, 
            AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.timer = timer;
        this.timeZoomProvider = timeZoomProvider;
        this.stringMessages = stringMessages;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.errorReporter = errorReporter;
    }
    
    @Override
    public void raceTimesInfosReceived(Map<RaceIdentifier, RaceTimesInfoDTO> raceTimesInfos) {
        this.lastRaceTimesInfo = raceTimesInfos.get(selectedRaceIdentifier);
        
        Pair<Date, Date> raceMinMax = RaceTimesCalculationUtil.caluclateRaceMinMax(timer, this.lastRaceTimesInfo);
        boolean updateMinMax = false;
        
        if(chart != null) {
            if(minTimepoint == null || maxTimepoint == null) {
                minTimepoint = raceMinMax.getA();
                maxTimepoint = raceMinMax.getB();
                updateMinMax = true;
            } else if(minTimepoint.getTime() != raceMinMax.getA().getTime() || maxTimepoint.getTime() != raceMinMax.getB().getTime()) {
                minTimepoint = raceMinMax.getA();
                maxTimepoint = raceMinMax.getB();
                updateMinMax = true;
            }
        }
        if(updateMinMax) {
            chart.getXAxis().setMin(minTimepoint.getTime());
            chart.getXAxis().setMax(maxTimepoint.getTime());
            chart.getXAxis().setExtremes(minTimepoint.getTime(), maxTimepoint.getTime(), false, false);
            
            long tickInterval = (maxTimepoint.getTime() - minTimepoint.getTime()) / TICKCOUNT;
            chart.getXAxis().setTickInterval(tickInterval);
        }
    }

    protected void showLoading(String message) {
        if(timer.getPlayMode() != PlayModes.Live) {
            chart.showLoading(message);
        }
        isLoading = true;
    }

    protected void hideLoading() {
        if (timer.getPlayMode() != PlayModes.Live) {
            chart.hideLoading();
        }
        isLoading = false;
    }

    protected boolean onXAxisSelectionChange(ChartSelectionEvent chartSelectionEvent) {
        try {
            long xAxisMin = chartSelectionEvent.getXAxisMinAsLong();
            long xAxisMax = chartSelectionEvent.getXAxisMaxAsLong();

            if(!isZoomed) {
                isZoomed = true;
            }
            timeZoomProvider.setTimeZoom(new Date(xAxisMin), new Date(xAxisMax), this);
//            long tickInterval = (xAxisMax - xAxisMin) / TICKCOUNT;
//            chart.getXAxis().setTickInterval(tickInterval);
//            chart.redraw();
        } catch (Throwable t) {
            // in case the user clicks the "reset zoom" button chartSelectionEvent.getXAxisMinAsLong() throws in exception
            timeZoomProvider.resetTimeZoom(this);
            // Trigger the redrawing... otherwise chart wouldn't reset the zoom
            chart.redraw();
            isZoomed = false;
        }
        
        return true;
    }

    protected boolean onClick(ChartClickEvent chartClickEvent) {
        if (!isLoading && !isZoomed) {
            timer.setPlayMode(PlayModes.Replay);
            timer.setTime(chartClickEvent.getXAxisValueAsLong());
        }
        return true;
    }

    protected void changeMinMaxInterval(Date minIntervalTimepoint, Date maxIntervalTimepoint) {
        XAxis xAxis = chart.getXAxis();
        xAxis.setExtremes(minIntervalTimepoint.getTime(), maxIntervalTimepoint.getTime(), true, true);
    }
    
    public void onTimeZoom(Date zoomStartTimepoint, Date zoomEndTimepoint) {
        changeMinMaxInterval(zoomStartTimepoint, zoomEndTimepoint);
        // Probably there is a function for this in a newer version of highcharts: http://jsfiddle.net/mqz3N/1071/ 
        // chart.showResetZoom();
    }

    public void onTimeZoomReset() {
        changeMinMaxInterval(minTimepoint, maxTimepoint);
    }

    /**
     * When using this method to enable the use of checkboxes only for hiding / showing a series, callers need to ensure
     * that all series have {@link PlotOptions#setSelected(boolean)} set to <code>true</code> for all series that are
     * added to the chart and hence visible. Otherwise, the checkbox won't initially be in sync with the series'
     * visibility state.
     */
    protected void useCheckboxesToShowAndHide(final Chart chart) {
        chart.setLegend(new Legend().setEnabled(true).setBorderWidth(0).setSymbolPadding(25)); // make room for checkbox
        chart.setSeriesPlotOptions(new SeriesPlotOptions().setSeriesCheckboxClickEventHandler(new SeriesCheckboxClickEventHandler() {
                    @Override
                    public boolean onClick(SeriesCheckboxClickEvent seriesCheckboxClickEvent) {
                        if (seriesCheckboxClickEvent.isChecked()) {
                            chart.getSeries(seriesCheckboxClickEvent.getSeriesId()).show();
                        } else {
                            chart.getSeries(seriesCheckboxClickEvent.getSeriesId()).hide();
                        }
                        return false; // don't toggle the select state of the series
                    }
                }).setShowCheckbox(true).
                setSeriesLegendItemClickEventHandler(new SeriesLegendItemClickEventHandler() {
                    @Override
                    public boolean onClick(SeriesLegendItemClickEvent seriesLegendItemClickEvent) {
                        // disable toggling visibility by clicking the legend item; force user to use checkbox instead
                        return false;
                    }
                }));
    }
}
