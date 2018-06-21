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

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.WidgetCollection;
import com.sap.sailing.domain.common.RegattaAndRaceIdentifier;
import com.sap.sailing.gwt.ui.client.SailingServiceAsync;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.charts.ChartCssResources.ChartsCss;
import com.sap.sse.common.settings.Settings;
import com.sap.sse.gwt.client.ErrorReporter;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.controls.busyindicator.SimpleBusyIndicator;
import com.sap.sse.gwt.client.controls.slider.TimeTicksCalculator;
import com.sap.sse.gwt.client.controls.slider.TimeTicksCalculator.NormalizedInterval;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.TimeRangeChangeListener;
import com.sap.sse.gwt.client.player.TimeRangeWithZoomProvider;
import com.sap.sse.gwt.client.player.TimeZoomChangeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;
import com.sap.sse.gwt.client.player.Timer.PlayStates;
import com.sap.sse.gwt.client.shared.components.AbstractCompositeComponent;
import com.sap.sse.gwt.client.shared.components.Component;
import com.sap.sse.gwt.client.shared.components.SettingsDialog;
import com.sap.sse.gwt.client.shared.settings.ComponentContext;

public abstract class AbstractRaceChart<SettingsType extends Settings> extends AbstractCompositeComponent<SettingsType>
        implements TimeListener, TimeZoomChangeListener, TimeRangeChangeListener {
    /**
     * Used as the turboThreshold for the Highcharts series; this is basically the maximum number of points in a series
     * to be displayed. Default is 1000. See also bug 1742.
     */
    protected static final int MAX_SERIES_POINTS = 1000000;

    public static class ExposedAbsolutePanel extends AbsolutePanel {

        public WidgetCollection getChildren() {
            return super.getChildren();
        }

    }

    private ExposedAbsolutePanel rootPanel = new ExposedAbsolutePanel();

    protected Chart chart;
    protected PlotLine timePlotLine;

    protected final Timer timer;
    protected final TimeRangeWithZoomProvider timeRangeWithZoomProvider;

    protected final RegattaAndRaceIdentifier selectedRaceIdentifier;

    protected final DateTimeFormat dateFormat = DateTimeFormat.getFormat("HH:mm:ss");
    protected final DateTimeFormat dateFormatHoursMinutes = DateTimeFormat.getFormat("HH:mm");

    protected final StringMessages stringMessages;
    protected final ErrorReporter errorReporter;
    protected final AsyncActionsExecutor asyncActionsExecutor;
    protected final SailingServiceAsync sailingService;

    protected boolean isLoading = false;

    protected static ChartsCss chartsCss = ChartCssResources.INSTANCE.css();

    /** the tick count must be the same as TimeSlider.TICKCOUNT, otherwise the time ticks will be not synchronized */
    private final int TICKCOUNT = 10;

    public static final long MINUTE_IN_MILLIS = 60 * 1000;

    private boolean ignoreNextClickEvent;

    private final SimpleBusyIndicator busyIndicator;

    private final Button settingsButton;
    private final FlowPanel toolbar = new FlowPanel();

    protected AbstractRaceChart(Component<?> parent, ComponentContext<?> context, SailingServiceAsync sailingService,
            RegattaAndRaceIdentifier selectedRaceIdentifier, Timer timer,
            TimeRangeWithZoomProvider timeRangeWithZoomProvider, final StringMessages stringMessages,
            AsyncActionsExecutor asyncActionsExecutor, ErrorReporter errorReporter) {
        super(parent, context);
        this.sailingService = sailingService;
        this.selectedRaceIdentifier = selectedRaceIdentifier;
        this.timer = timer;
        this.timeRangeWithZoomProvider = timeRangeWithZoomProvider;
        this.stringMessages = stringMessages;
        this.asyncActionsExecutor = asyncActionsExecutor;
        this.errorReporter = errorReporter;
        timer.addTimeListener(this);
        timeRangeWithZoomProvider.addTimeZoomChangeListener(this);
        timeRangeWithZoomProvider.addTimeRangeChangeListener(this);
        chartsCss.ensureInjected();
        busyIndicator = new SimpleBusyIndicator(/* busy */ true, 2.0f);
        busyIndicator.setPanelStyleClass(chartsCss.busyIndicatorStyle());
        busyIndicator.setImageStyleClass(chartsCss.busyIndicatorImageStyle());
        toolbar.addStyleName(chartsCss.toolbar());
        add(toolbar);
        settingsButton = createSettingsButton();
        settingsButton.setStyleName(chartsCss.settingsButtonBackgroundImage());
        addToolbarButton(settingsButton);
        initWidget(rootPanel);
        getElement().getStyle().setMarginRight(12, Unit.PX);
        getElement().getStyle().setMarginLeft(12, Unit.PX);
    }

    /**
     * Subclasses implement this, e.g., by calling
     * {@link SettingsDialog#createSettingsButton(com.sap.sse.gwt.client.shared.components.Component, StringMessages)}.
     * This class's constructor will add the {@link ChartsCss#settingsButtonStyle()} and the
     * {@link ChartsCss#settingsButtonBackgroundImage()}.
     */
    protected abstract Button createSettingsButton();

    public void addToolbarButton(Button button) {
        toolbar.insert(button, 0);
    }

    /**
     * Subclasses need to provide a settings button which will be displayed at a useful position in the layout of this
     * complex panel, e.g., in the top right corner.
     */
    private Button getSettingsButton() {
        return settingsButton;
    }

    /**
     * Simulates a {@link SimplePanel} behavior by replacing all widgets but the {@link #getSettingsButton() settings
     * button} which is always supposed to be visible. If <code>widget</code> is already a child of this panel, it is
     * left unchanged, and all other widgets except for the settings button are removed.
     */
    protected void setWidget(Widget widget) {
        Button settingsButton = getSettingsButton();
        boolean foundWidget = false;
        for (Iterator<Widget> i = rootPanel.getChildren().iterator(); i.hasNext();) {
            Widget child = i.next();
            if (child == widget) {
                foundWidget = true;
            } else if (child != settingsButton && child != toolbar) {
                i.remove();
            }
        }
        if (!foundWidget) {
            add(widget);
        }
    }

    protected void showLoading(String message) {
        if (chart.isRendered()) {
            chart.showLoading(message);
        } else {
            add(busyIndicator);
        }
        isLoading = true;
    }

    protected void hideLoading() {
        chart.hideLoading();
        isLoading = false;
        remove(busyIndicator);
    }

    protected boolean onXAxisSelectionChange(ChartSelectionEvent chartSelectionEvent) {
        Long xAxisMin = chartSelectionEvent.getXAxisMinAsLongOrNull();
        Long xAxisMax = chartSelectionEvent.getXAxisMaxAsLongOrNull();
        // Set a minute as max time zoom just as for chart
        if (xAxisMax != null && xAxisMin != null) {
            if (xAxisMax - xAxisMin > MINUTE_IN_MILLIS) {
                Date rangeStart = new Date(xAxisMin);
                Date rangeEnd = new Date(xAxisMax);
                if(timer.getPlayMode() == PlayModes.Live) {
                    timer.pause();
                }
                Scheduler.get().scheduleDeferred(() -> timeRangeWithZoomProvider.setTimeZoom(rangeStart, rangeEnd));
                return true;
            }
            return false;
        } else {
            ignoreNextClickEvent = true;
            timeRangeWithZoomProvider.resetTimeZoom();
            return false;
        }
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

    /**
     * Does nothing here; subclasses have the possibility to override if they need to re-load data based on changes in
     * the visible area that may lead to step size / resolution adjustments
     */
    protected void updateChartIfEffectiveStepSizeChanged(Date minTimepoint, Date maxTimepoint) {
    }

    /**
     * @param allowZoomProcessing if true, in livemode and zoomed, reset the zoom
     */
    protected void changeMinMaxAndExtremesInterval(Date minTimepoint, Date maxTimepoint, boolean redraw) {
        if (chart != null) {
            XAxis xAxis = chart.getXAxis();
            // if we are zoomed, and in livemode, reset the zoom, as this cannot be handled by the timesliders expected
            // behaviour
            if (timeRangeWithZoomProvider.isZoomed() && timer.getPlayMode() == PlayModes.Live) {
                Scheduler.get().scheduleDeferred(timeRangeWithZoomProvider::resetTimeZoom);
                return;
            }
            if (minTimepoint != null && maxTimepoint != null) {
                xAxis.setExtremes(minTimepoint.getTime(), maxTimepoint.getTime(), /* redraw */ false, false);
                long tickInterval = (maxTimepoint.getTime() - minTimepoint.getTime()) / TICKCOUNT;
                TimeTicksCalculator calculator = new TimeTicksCalculator();
                NormalizedInterval normalizedInterval = calculator.normalizeTimeTickInterval(tickInterval);
                xAxis.setTickInterval(normalizedInterval.count * normalizedInterval.unitRange);
            }
            if (minTimepoint != null) {
                xAxis.setMin(minTimepoint.getTime());
            }
            if (maxTimepoint != null) {
                xAxis.setMax(maxTimepoint.getTime());
            }
            if (redraw) {
                chart.redraw();
            }
        }
    }

    protected void setSeriesPoints(Series series, Point[] points, boolean manageZoom) {
        if (manageZoom && timeRangeWithZoomProvider.isZoomed()) {
            com.sap.sse.common.Util.Pair<Date, Date> timeZoom = timeRangeWithZoomProvider.getTimeZoom();
            resetMinMaxAndExtremesInterval(/* redraw */ false);
            series.setPoints(points, /* redraw */ false);
            changeMinMaxAndExtremesInterval(timeZoom.getA(), timeZoom.getB(), /* redraw */ false);
        } else {
            series.setPoints(points, /* redraw */ false);
        }
    }

    protected void resetMinMaxAndExtremesInterval(boolean redraw) {
        changeMinMaxAndExtremesInterval(timeRangeWithZoomProvider.getFromTime(), timeRangeWithZoomProvider.getToTime(),
                redraw);
    }

    @Override
    public void onTimeZoomChanged(Date zoomStartTimepoint, Date zoomEndTimepoint) {
        changeMinMaxAndExtremesInterval(zoomStartTimepoint, zoomEndTimepoint, true);
        chart.showResetZoom(); // Patched method
        fireEvent(new ChartZoomChangedEvent(zoomStartTimepoint, zoomEndTimepoint));
    }

    @Override
    public void onTimeRangeChanged(Date fromTime, Date toTime) {
        resetMinMaxAndExtremesInterval(true);
    }

    @Override
    public void onTimeZoomReset() {
        fireEvent(new ChartZoomResetEvent());
        chart.hideResetZoom();
        resetMinMaxAndExtremesInterval(true);
    }

    protected void updateTimePlotLine(Date date) {
        chart.getXAxis().removePlotLine(timePlotLine);
        timePlotLine.setValue(date.getTime());
        chart.getXAxis().addPlotLines(timePlotLine);
    }

    public void add(Widget widget) {
        rootPanel.add(widget);
    }

    public boolean remove(Widget widget) {
        return rootPanel.remove(widget);
    }

    public WidgetCollection getChildren() {
        return rootPanel.getChildren();
    }

    /**
     * Determines if a standard loading message is allowed to appear over the chart or not.
     * 
     * @return <code>true</code> if the message is allowed, <code>false</code> otherwise.
     */
    protected boolean shouldShowLoading(Long timestamp) {
        return timestamp == null
                || (timer.getPlayState() != PlayStates.Playing && timer.getPlayMode() != PlayModes.Live);
    }

    public HandlerRegistration addChartZoomChangedHandler(ChartZoomChangedEvent.Handler handler) {
        return addHandler(handler, ChartZoomChangedEvent.TYPE);
    }

    public HandlerRegistration addChartZoomResetHandler(ChartZoomResetEvent.Handler handler) {
        return addHandler(handler, ChartZoomResetEvent.TYPE);
    }
}
