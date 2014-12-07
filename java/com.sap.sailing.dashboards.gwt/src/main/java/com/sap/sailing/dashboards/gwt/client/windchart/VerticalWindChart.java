package com.sap.sailing.dashboards.gwt.client.windchart;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.moxieapps.gwt.highcharts.client.Axis.Type;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.DateTimeLabelFormats;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.PlotLine.DashStyle;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Style;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.DataLabels;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.BarPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiConstructor;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.RibDashboardEntryPoint;
import com.sap.sailing.dashboards.gwt.shared.MovingAverage;

/**
 * The class represents a wind chart that is displayed vertically. Because it is meant to be used to display wind fixes,
 * it shows also changes its threshold to indicate the average wind value over a certain amount of time. The charts
 * x-axis can show the values in a small and big time interval that the user can change by tabing somewhere on the whole
 * chart.
 * 
 * @author Alexander Ries (D062114)
 *
 */
public class VerticalWindChart extends Composite implements HasWidgets {

    private Series verticalWindChartSeries;
    private String positiveSeriesColorAsHex;
    private String negativeSeriesColorAsHex;

    /**
     * The field is used to cache the current selected display interval. It is either
     * {@link #SMALL_DISPLAY_INTERVALL_IN_MINUTES} or {@link #LARGE_DISPLAY_INTERVALL_IN_MINUTES}.
     * */
    private int chartIntervallinMinutes = SMALL_DISPLAY_INTERVALL_IN_MINUTES;

    /**
     * LARGE_DISPLAY_INTERVALL_IN_MINUTES and SMALL_DISPLAY_INTERVALL_IN_MINUTES define two possible intervals that the
     * user can select by tabing on the chart.
     * */
    private static final int LARGE_DISPLAY_INTERVALL_IN_MINUTES = 60;
    private static final int SMALL_DISPLAY_INTERVALL_IN_MINUTES = 15;

    private static final Logger logger = Logger.getLogger(RibDashboardEntryPoint.class.getName());

    private static VerticalWindChartUiBinder uiBinder = GWT.create(VerticalWindChartUiBinder.class);

    interface VerticalWindChartUiBinder extends UiBinder<Widget, VerticalWindChart> {
    }

    interface VerticalWindChartStyle extends CssResource {
    }

    /**
     * An invisible clickable edge to edge area that changes onClick the charts x-axis extreme values. See
     * {@link #verticalWindChartClickAreaClicked(ClickEvent)}
     * */
    @UiField
    FocusPanel verticalWindChartClickArea;

    @UiField(provided = true)
    Chart verticalWindChart;

    @UiField
    HTMLPanel verticalWindChartClickHint;

    @UiField
    SpanElement clickHintMinutesSpan;

    public VerticalWindChart() {
        initWidget(uiBinder.createAndBindUi(this));
    }

    public @UiConstructor VerticalWindChart(String positiveFillColor, String negativeFillColor) {
        initVerticalWindChartWithColors(positiveFillColor, negativeFillColor);
        initWidget(uiBinder.createAndBindUi(this));
    }

    private void initVerticalWindChartWithColors(String positiveFillColor, String negativeFillColor) {
        verticalWindChart = new Chart();
        setChartOptions();
        setXAxisOptions();
        setYAxisOptions();
        positiveSeriesColorAsHex = positiveFillColor;
        negativeSeriesColorAsHex = negativeFillColor;
    }

    private void setChartOptions() {
        verticalWindChart.setType(Series.Type.AREA_SPLINE).setInverted(true).setChartTitleText(" ").setMarginTop(10)
                .setMarginBottom(25).setMarginRight(20)
                .setBarPlotOptions(new BarPlotOptions().setDataLabels(new DataLabels().setEnabled(true)))
                .setLegend(new Legend().setEnabled(false)).setCredits(new Credits().setEnabled(false))
                .setToolTip(new ToolTip().setEnabled(false));
    }

    private void setXAxisOptions() {
        verticalWindChart
                .getXAxis()
                .setType(Type.DATE_TIME)
                .setMaxPadding(0)
                .setReversed(false)
                .setLabels(
                        new XAxisLabels().setStyle(new Style().setFontFamily("Arial").setFontSize("12")
                                .setColor("grey"))).setLineColor("grey").setTickWidth(0.5).setLineWidth(1)
                .setTickWidth(1).setTickColor("grey").setOffset(0).setMinPadding(20)
                .setDateTimeLabelFormats(new DateTimeLabelFormats().setMonth("%e. %b").setYear("%b"));
    }

    private void setYAxisOptions() {
        verticalWindChart
                .getYAxis()
                .setLineColor("grey")
                .setTickWidth(1)
                .setTickColor("grey")
                .setAxisTitleText(null)
                .setGridLineWidth(0)
                .setLineWidth(1)
                .setOffset(0)
                .setMaxPadding(0)
                .setLabels(
                        new YAxisLabels().setStyle(
                                new Style().setFontFamily("Arial").setFontSize("12").setColor("grey")).setFormatter(
                                new AxisLabelsFormatter() {
                                    @Override
                                    public String format(AxisLabelsData axisLabelsData) {
                                        long value = axisLabelsData.getValueAsLong() % 360;
                                        return new Long(value < 0 ? value + 360 : value).toString();
                                    }
                                }));
    }

    private void initVerticalWindChartSeries() {
        verticalWindChartSeries = verticalWindChart.createSeries();
        AreaPlotOptions areaPlotOptions = new AreaPlotOptions();
        verticalWindChart.addSeries(verticalWindChartSeries.setName(null).setPlotOptions(
                areaPlotOptions.setDashStyle(DashStyle.SOLID).setLineWidth(0.1)
                        .setMarker(new Marker().setEnabled(false)).setShadow(false).setHoverStateEnabled(false)
                        .setLineColor("#FFFFFF").setFillColor(positiveSeriesColorAsHex)
                        .setOption("negativeFillColor", negativeSeriesColorAsHex)));
    }

    /**
     * The methods adds one point to the charts series and updates the threshold with a wind average value. When the
     * series point contains wind direction values the y values gets adapted to stay close to the previous one.
     * Depending on the selected chart display interval {@link #SMALL_DISPLAY_INTERVALL_IN_MINUTES} or
     * {@link #LARGE_DISPLAY_INTERVALL_IN_MINUTES}, the extreme values of the charts x-axis get adapted. See
     * {@link #adaptVerticalWindChartExtemes()},
     * {@link #setXAxisExtremesForSeriesPointRangeIsSmallerThanChartDisplayIntervall()} and
     * {@link #setXAxisExtremesForSeriesPointRangeIsBiggerThanChartDisplayIntervall()}.
     * */
    public void addPointsToSeriesWithAverage(Point[] points, double average) {
        if (verticalWindChartSeries == null) {
            initVerticalWindChartSeries();
            verticalWindChartSeries.setPoints(points, true);
        } else {
            for (Point point : points) {
                verticalWindChartSeries.addPoint(point, true, false, false);
            }
        }
        verticalWindChartSeries.updateThreshold("" + average);
        adaptVerticalWindChartExtemes();
        verticalWindChart.setSizeToMatchContainer();
    }

    /**
     * Depending on the series points time range and the selected display interval, the series fits into the display
     * interval or not. So there are two cases where the extreme points of the x-axis get calculated differently.
     * */
    private void adaptVerticalWindChartExtemes() {
        if (getVerticalWindChartSeriesPointsTimeRangeInMilliseconds() < chartIntervallinMinutes * 60 * 1000) {
            setXAxisExtremesForSeriesPointRangeIsSmallerThanChartDisplayIntervall();
        } else {
            setXAxisExtremesForSeriesPointRangeIsBiggerThanChartDisplayIntervall();
        }
    }

    /**
     * When the time range of the series points is smaller than the current selected display interval represented by
     * either LARGE_DISPLAY_INTERVALL_IN_MINUTES or SMALL_DISPLAY_INTERVALL_IN_MINUTES the x-axis extremes min value is
     * the the first points time and the maximum value the min value plus the selected display interval. The chart gets
     * other extremes, because the values fit easily into the selected display interval.
     * */
    private void setXAxisExtremesForSeriesPointRangeIsSmallerThanChartDisplayIntervall() {
        long minimumExtremeValueInMillis = getFirstPointOfVerticalWindChartSeries().getX().longValue();
        long maximumExtremeValueInMillis = minimumExtremeValueInMillis + chartIntervallinMinutes * 60 * 1000;
        verticalWindChart.getXAxis().setExtremes(minimumExtremeValueInMillis, maximumExtremeValueInMillis, true, true);
    }

    /**
     * When the time range of the series points is bigger than the current selected display interval represented by
     * either LARGE_DISPLAY_INTERVALL_IN_MINUTES or SMALL_DISPLAY_INTERVALL_IN_MINUTES the x-axis extremes min value is
     * the the max value minus the selected display interval and the max value the last points time value. The chart
     * gets other extremes, because the values do not fit anymore into the selected display interval.
     * */
    private void setXAxisExtremesForSeriesPointRangeIsBiggerThanChartDisplayIntervall() {
        long maximumExtremeValueInMillis = getLastPointOfVerticalWindChartSeries().getX().longValue();
        long minimumExtremeValueInMillis = maximumExtremeValueInMillis - chartIntervallinMinutes * 60 * 1000;
        verticalWindChart.getXAxis().setExtremes(minimumExtremeValueInMillis, maximumExtremeValueInMillis, true, true);
    }

    private long getVerticalWindChartSeriesPointsTimeRangeInMilliseconds() {
        Point[] seriesPoints = verticalWindChartSeries.getPoints();
        long pointRangeInMilliseconds;
        int seriesPointsLenght = seriesPoints.length;
        if (seriesPointsLenght > 0) {
            long earliesPointAsTimestamp = seriesPoints[0].getX().longValue();
            long latestPointAsTimestamp;
            if (seriesPointsLenght > 1) {
                latestPointAsTimestamp = seriesPoints[seriesPointsLenght - 1].getX().longValue();
            } else {
                latestPointAsTimestamp = earliesPointAsTimestamp;
            }
            pointRangeInMilliseconds = latestPointAsTimestamp - earliesPointAsTimestamp;
        } else {
            pointRangeInMilliseconds = 0;
        }
        logger.log(Level.INFO, "Points Range " + pointRangeInMilliseconds);
        return pointRangeInMilliseconds;
    }

    private Point getLastPointOfVerticalWindChartSeries() {
        Point lastPointOfVerticalWindChartSeries = null;
        if (verticalWindChartSeries != null) {
            Point[] seriesPoints = verticalWindChartSeries.getPoints();
            int seriesPointsLenght = seriesPoints.length;
            if (seriesPointsLenght > 0) {
                lastPointOfVerticalWindChartSeries = seriesPoints[seriesPointsLenght - 1];
            }
        }
        return lastPointOfVerticalWindChartSeries;
    }

    private Point getFirstPointOfVerticalWindChartSeries() {
        Point firstPointOfVerticalWindChartSeries = null;
        Point[] seriesPoints = verticalWindChartSeries.getPoints();
        int seriesPointsLenght = seriesPoints.length;
        if (seriesPointsLenght > 0) {
            firstPointOfVerticalWindChartSeries = seriesPoints[0];
        }
        return firstPointOfVerticalWindChartSeries;
    }

    /**
     * The method handles a click on the vertical wind charts focus panel. It changes the display interval of the x-axis
     * and adapts the click hint at the bottom of the chart.
     * */
    @UiHandler("verticalWindChartClickArea")
    public void verticalWindChartClickAreaClicked(ClickEvent e) {
        if (chartIntervallinMinutes == SMALL_DISPLAY_INTERVALL_IN_MINUTES) {
            chartIntervallinMinutes = LARGE_DISPLAY_INTERVALL_IN_MINUTES;
        } else {
            chartIntervallinMinutes = SMALL_DISPLAY_INTERVALL_IN_MINUTES;
        }
        adaptVerticalWindChartExtemes();
        clickHintMinutesSpan.setInnerHTML("" + chartIntervallinMinutes);
    }

    @Override
    public void add(Widget w) {
        throw new UnsupportedOperationException("The method add(Widget w) is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("The method clear() is not supported.");
    }

    @Override
    public Iterator<Widget> iterator() {
        return null;
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }
}
