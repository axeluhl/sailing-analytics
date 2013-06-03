package com.sap.sailing.gwt.ui.polarsheets;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class PolarSheetsHistogramPanel extends DockLayoutPanel {
    
    private final Chart chart;
    private final StringMessages stringMessages;

    public PolarSheetsHistogramPanel(StringMessages stringMessages) {
        super(Unit.PCT);
        this.stringMessages = stringMessages;
        setSize("100%", "100%");
        chart = createHistogramChart();
        chart.getElement().setAttribute("align", "top");
        add(chart);
    }

    private Chart createHistogramChart() {
        Chart histogramChart = new Chart().setType(Type.COLUMN).setZoomType(Chart.ZoomType.X).setHeight100().setWidth100();
        histogramChart.setChartTitleText(stringMessages.histogram());
        histogramChart.getYAxis().setMin(0).setAxisTitle(new AxisTitle().setText(stringMessages.numberOfDataPoints()));
        histogramChart.getXAxis().setLabels(new XAxisLabels().setRotation(-90f).setY(10)).setAxisTitle(new AxisTitle().setText(
                stringMessages.speedInKnots()));
        histogramChart.setLegend(new Legend().setEnabled(false));
        return histogramChart;
    }

    public void setData(PolarSheetsHistogramData data) {
        chart.removeAllSeries();
        chart.setTitle(new ChartTitle().setText(stringMessages.histogram()),
                new ChartSubtitle().setText(stringMessages.angleAndTotalNumberOfDataPointsAndStandardDeviator(data.getAngle(), data.getDataCount(), data.getStandardDeviator())));
        Point[] points = toPoints(data);
        Series series = chart.createSeries();
        series.setPoints(points);
        chart.addSeries(series);
    }

    private Point[] toPoints(PolarSheetsHistogramData data) {
        Number[] xValues = data.getxValues();
        Number[] yValues = data.getyValues();
        Point[] points = new Point[xValues.length];
        for (int i = 0; i < xValues.length; i++) {
            points[i] = new Point(xValues[i], yValues[i]);
        }
        return points;
    }
    
    @Override
    protected void onLoad() {
        Timer timer = new Timer() {
            
            @Override
            public void run() {
                chart.setSizeToMatchContainer();
            }
        };
        timer.schedule(200);
        super.onLoad();
    }
    
    @Override
    public void onResize() {
        chart.setSizeToMatchContainer();
        super.onResize();
    }
    

}
