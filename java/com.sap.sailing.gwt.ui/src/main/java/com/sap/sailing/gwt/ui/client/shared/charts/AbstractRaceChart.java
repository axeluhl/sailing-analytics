package com.sap.sailing.gwt.ui.client.shared.charts;

import java.util.Date;
import java.util.Iterator;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.PlotLine;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.XAxis;
import org.moxieapps.gwt.highcharts.client.events.ChartClickEvent;
import org.moxieapps.gwt.highcharts.client.events.ChartSelectionEvent;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.RaceSelectionChangeListener;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.ChartCssResources.ChartsCss;
import com.sap.sailing.gwt.ui.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.TimeRangeChangeListener;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.TimeZoomChangeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

public abstract class AbstractRaceChart extends AbsolutePanel implements RaceSelectionChangeListener,
    TimeListener, TimeZoomChangeListener, TimeRangeChangeListener {
    /**
     * Used as the turboThreshold for the Highcharts series; this is basically the maximum number of points in a series
     * to be displayed. Default is 1000. See also bug 1742.
     */
    protected static final int MAX_SERIES_POINTS = 1000000;

    protected Chart chart;
    protected PlotLine timePlotLine;

    protected final Timer timer;
    protected final TimeRangeWithZoomProvider timeRangeWithZoomProvider; 
  
    protected RegattaAndRaceIdentifier selectedRaceIdentifier;

    protected final DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
    protected final DateTimeFormat dateFormatHoursMinutes = DateTimeFormat.getFormat("HH:mm");

    protected final StringMessages stringMessages;
    protected final ErrorReporter errorReporter;
    protected final AsyncActionsExecutor asyncActionsExecutor;
    protected final SailingServiceAsync sailingService;

    protected boolean isLoading = false;
    protected boolean isZoomed = false;
    
    protected static ChartsCss chartsCss = ChartCssResources.INSTANCE.css();
    
    /** the tick count must be the same as TimeSlider.TICKCOUNT, otherwise the time ticks will be not synchronized */  
    private final int TICKCOUNT = 10;

    private boolean ignoreNextClickEvent;
    
    private final SimpleBusyIndicator busyIndicator;
    
    private final Button settingsButton;
    
    protected AbstractRaceChart(SailingServiceAsync sailingService, Timer timer, TimeRangeWithZoomProvider timeRangeWithZoomProvider, final StringMessages stringMessages, 
            AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter) {
        this.sailingService = sailingService;
        this.timer = timer;
        this.timeRangeWithZoomProvider = timeRangeWithZoomProvider;
        this.stringMessages = stringMessages;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.errorReporter = errorReporter;
        timer.addTimeListener(this);
        timeRangeWithZoomProvider.addTimeZoomChangeListener(this);
        timeRangeWithZoomProvider.addTimeRangeChangeListener(this);
        chartsCss.ensureInjected();
        busyIndicator = new SimpleBusyIndicator(/* busy */ true, 2.0f, chartsCss.busyIndicatorStyle(), chartsCss.busyIndicatorImageStyle());
        settingsButton = createSettingsButton();
        settingsButton.setStyleName(chartsCss.settingsButtonStyle());
        settingsButton.addStyleName(chartsCss.settingsButtonBackgroundImage());
        add(settingsButton);
        getElement().getStyle().setMarginRight(12, Unit.PX);
        getElement().getStyle().setMarginLeft(12, Unit.PX);
    }
    
    /**
     * Subclasses implement this, e.g., by calling {@link SettingsDialog#createSettingsButton(com.sap.sailing.gwt.ui.client.shared.components.Component, StringMessages)}.
     * This class's constructor will add the {@link ChartsCss#settingsButtonStyle()} and the {@link ChartsCss#settingsButtonBackgroundImage()}.
     */
    protected abstract Button createSettingsButton();
    
    /**
     * Subclasses need to provide a settings button which will be displayed at a useful position in the layout of this
     * complex panel, e.g., in the top right corner.
     */
    private Button getSettingsButton() {
        return settingsButton;
    }
    
    /**
     * Simulates a {@link SimplePanel} behavior by replacing all widgets but the {@link #getSettingsButton() settings button} which is always
     * supposed to be visible. If <code>widget</code> is already a child of this panel, it is left unchanged, and all other widgets except for
     * the settings button are removed.
     */
    protected void setWidget(Widget widget) {
        Button settingsButton = getSettingsButton();
        boolean foundWidget = false;
        for (Iterator<Widget> i=getChildren().iterator(); i.hasNext(); ) {
            Widget child = i.next();
            if (child == widget) {
                foundWidget = true;
            } else if (child != settingsButton) {
                i.remove();
            }
        }
        if (!foundWidget) {
            add(widget);
        }
    }

    protected void showLoading(String message) {
        if (timer.getPlayMode() != PlayModes.Live) {
            if (chart.isRendered()) {
                chart.showLoading(message);
            } else {
                add(busyIndicator);
            }
        }
        isLoading = true;
    }

    protected void hideLoading() {
        if (timer.getPlayMode() != PlayModes.Live) {
            chart.hideLoading();
        }
        isLoading = false;
        remove(busyIndicator);
    }

    protected boolean onXAxisSelectionChange(ChartSelectionEvent chartSelectionEvent) {
        try {
            long xAxisMin = chartSelectionEvent.getXAxisMinAsLong();
            long xAxisMax = chartSelectionEvent.getXAxisMaxAsLong();
            if (!isZoomed) {
                isZoomed = true;
            }
            timeRangeWithZoomProvider.setTimeZoom(new Date(xAxisMin), new Date(xAxisMax), this);
        } catch (Exception e) {
            // in case the user clicks the "reset zoom" button chartSelectionEvent.getXAxisMinAsLong() throws in exception
            timeRangeWithZoomProvider.resetTimeZoom(this);
            // Trigger the redrawing... otherwise chart wouldn't reset the zoom
            chart.redraw();
            isZoomed = false;
            // after the selection change event, another click event is sent with the mouse position on the "Reset Zoom" button; ignore that
            ignoreNextClickEvent = true;
        }
        return true;
    }

    protected boolean onClick(ChartClickEvent chartClickEvent) {
        if (ignoreNextClickEvent) {
            ignoreNextClickEvent = false;
        } else {
            if (!isLoading) {
                timer.setPlayMode(PlayModes.Replay);
                timer.setTime(chartClickEvent.getXAxisValueAsLong());
            }
        }
        return true;
    }

    protected void changeMinMaxAndExtremesInterval(Date minTimepoint, Date maxTimepoint, boolean redraw) {
        XAxis xAxis = chart.getXAxis();
        if (minTimepoint != null) {
            xAxis.setMin(minTimepoint.getTime());
        }
        if (maxTimepoint != null) {
            xAxis.setMax(maxTimepoint.getTime());
        }
        if (minTimepoint != null && maxTimepoint != null) {
            xAxis.setExtremes(minTimepoint.getTime(), maxTimepoint.getTime(), false, false);
            long tickInterval = (maxTimepoint.getTime() - minTimepoint.getTime()) / TICKCOUNT;
            xAxis.setTickInterval(tickInterval);
        }
        if (redraw) {
            chart.redraw();
        }
    }

    protected void setSeriesPoints(Series series, Point[] points) {
        if (timeRangeWithZoomProvider.isZoomed()) {
            com.sap.sse.common.Util.Pair<Date, Date> timeZoom = timeRangeWithZoomProvider.getTimeZoom();
            resetMinMaxAndExtremesInterval(/* redraw */ false);
            series.setPoints(points, false);
            changeMinMaxAndExtremesInterval(timeZoom.getA(), timeZoom.getB(), /* redraw */ false);
        } else {
            series.setPoints(points, false);
        }
    }
    
    protected void resetMinMaxAndExtremesInterval(boolean redraw) {
        changeMinMaxAndExtremesInterval(timeRangeWithZoomProvider.getFromTime(), timeRangeWithZoomProvider.getToTime(), redraw);
    }

    @Override
    public void onTimeZoomChanged(Date zoomStartTimepoint, Date zoomEndTimepoint) {
        changeMinMaxAndExtremesInterval(zoomStartTimepoint, zoomEndTimepoint, true);
        // Probably there is a function for this in a newer version of highcharts: http://jsfiddle.net/mqz3N/1071/ 
        // chart.showResetZoom();
    }

    @Override
    public void onTimeRangeChanged(Date fromTime, Date toTime) {
        if (!(isZoomed && timer.getPlayMode() == PlayModes.Live)) {
            resetMinMaxAndExtremesInterval(/* redraw */ true);
        }
    }

    @Override
    public void onTimeZoomReset() {
        resetMinMaxAndExtremesInterval(true);
    }
}
