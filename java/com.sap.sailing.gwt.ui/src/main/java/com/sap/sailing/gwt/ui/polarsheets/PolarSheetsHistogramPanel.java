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

import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class PolarSheetsHistogramPanel extends SimplePanel implements RequiresResize{
    
    private final Chart chart;
    private final StringMessages stringMessages;

    public PolarSheetsHistogramPanel(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        setSize("100%", "100%");
        chart = createHistogramChart();
        setWidget(chart);
    }

    private Chart createHistogramChart() {
        Chart histogramChart = new Chart().setType(Type.COLUMN).setZoomType(Chart.ZoomType.X).setWidth(800);
        //TODO string messages
        histogramChart.setChartTitleText("Histogram:");
        histogramChart.getYAxis().setMin(0).setAxisTitle(new AxisTitle().setText("Number of data-points"));
        histogramChart.getXAxis().setLabels(new XAxisLabels().setRotation(-90f).setY(10)).setAxisTitle(new AxisTitle().setText("Speed in knots"));
        histogramChart.setLegend(new Legend().setEnabled(false));
        return histogramChart;
    }

    public void setData(PolarSheetsHistogramData data) {
        chart.removeAllSeries();
        chart.setTitle(new ChartTitle().setText("Histogram"), new ChartSubtitle().setText("Angle: " + data.getAngle() + "; Total number of data-points: " + data.getDataCount()));
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
    public void onResize() {
        chart.setSizeToMatchContainer();
        chart.redraw();
    }

}
