package com.sap.sailing.gwt.ui.polarsheets;

import java.util.Map;
import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.PlotOptions.Stacking;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.sap.sailing.domain.common.PolarSheetsHistogramData;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Shows the underlying distribution for any point in the polar diagrams
 * of the {@link PolarSheetsChartPanel}.
 * 
 * @author d054528 Frederik Petersen
 *
 */
public class PolarSheetsHistogramPanel extends DockLayoutPanel {
    
    private final Chart chart;
    private final StringMessages stringMessages;
    private PolarSheetsHistogramData currentData;
    private final PolarSheetsHistogramDataArrangeButtonBar dataArrangeBar;

    public PolarSheetsHistogramPanel(StringMessages stringMessages) {
        super(Unit.PX);
        this.stringMessages = stringMessages;
        setSize("100%", "100%");
        dataArrangeBar = createDataArrangeButtons();
        addSouth(dataArrangeBar, 20);
        
        chart = createHistogramChart();
        chart.getElement().setAttribute("align", "top");
        add(chart);
    }

    private PolarSheetsHistogramDataArrangeButtonBar createDataArrangeButtons() {
        return new PolarSheetsHistogramDataArrangeButtonBar(this);
    }

    private Chart createHistogramChart() {
        Chart histogramChart = new Chart().setType(Type.AREA).setHeight100().setWidth100();
        histogramChart.setChartTitleText(stringMessages.histogram());
        histogramChart.getYAxis().setMin(0).setAxisTitle(new AxisTitle().setText(stringMessages.numberOfDataPoints()));
        histogramChart.getXAxis().setLabels(new XAxisLabels().setRotation(-90f).setY(10))
                .setAxisTitle(new AxisTitle().setText(stringMessages.speedInKnots()));
        histogramChart.setAreaPlotOptions(new AreaPlotOptions().setStacking(Stacking.NORMAL).setLineColor("#666666")
                .setLineWidth(1).setMarker(new Marker().setLineWidth(1).setLineColor("#666666")));
        return histogramChart;
    }

    public void setData(PolarSheetsHistogramData data) {
        dataArrangeBar.reset();
        currentData = data;
        chart.setTitle(new ChartTitle().setText(stringMessages.histogram()), new ChartSubtitle().setText(stringMessages
                .angleAndTotalNumberOfDataPointsAndCovAndCm(data.getAngle(), data.getDataCount(),
                        data.getCoefficiantOfVariation(), data.getConfidenceMeasure())));
        arrangeByNothing();
    }

    private Point[] toPoints(PolarSheetsHistogramData data) {
        Number[] xValues = data.getxValues();
        Number[] yValues = data.getyValues();
        return toPoints(xValues, yValues);
    }

    public Point[] toPoints(Number[] xValues, Number[] yValues) {
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
    
    public void arrangeByNothing() {
        chart.removeAllSeries();
        Point[] points = toPoints(currentData);
        Series series = chart.createSeries();
        series.setPoints(points);  
        chart.addSeries(series);
    }

    public void arrangeByWindGaugeIds() {
        Map<String, Integer[]> yValueMap = currentData.getYValuesByGaugeIds();
        arrangeByYValueMap(yValueMap);
    }

    public void arrangeByDay() {
        Map<String, Integer[]> yValueMap = currentData.getYValuesByDay();
        arrangeByYValueMap(yValueMap);
    }

    public void arrangeByDayAndGaugeIds() {
        Map<String, Integer[]> yValueMap = currentData.getYValuesByDayAndGaugeId();
        arrangeByYValueMap(yValueMap);
    }
    
    private void arrangeByYValueMap(Map<String, Integer[]> yValueMap) {
        chart.removeAllSeries();   
        for (Entry<String, Integer[]> entry : yValueMap.entrySet()) {
            Series series = chart.createSeries();
            series.setName(entry.getKey());
            series.setPoints(toPoints(currentData.getxValues(), entry.getValue()));
            chart.addSeries(series);
        }
    }
    
    public boolean hasData() {
        boolean hasData = true;
        if (currentData == null) {
            hasData = false;
        }
        return hasData;
    }
    
    
    

}
