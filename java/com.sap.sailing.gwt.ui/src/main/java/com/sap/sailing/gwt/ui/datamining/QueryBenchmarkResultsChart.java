package com.sap.sailing.gwt.ui.datamining;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.PlotLine;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;

public class QueryBenchmarkResultsChart extends SimplePanel implements RequiresResize {
    
    private Chart chart;
    private Series serverTimeSeries;
    private double serverTimeSum = 0;
    private PlotLine averageServerTimePlotLine;
//    private Series overallTimeSeries;
//    private Series gpsFixAmountSeries;
    
    public QueryBenchmarkResultsChart() {
        chart = new Chart()
                      .setMarginLeft(100)
                      .setMarginRight(100)
                      .setWidth100()
                      .setHeight100()
                      .setCredits(new Credits().setEnabled(false))
                      .setChartTitle(new ChartTitle().setText("Query Benchmark Results"));
        
        chart.getXAxis().setType(Axis.Type.LINEAR);

        chart.getYAxis(0).setAxisTitleText("Time").setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                return axisLabelsData.getValueAsDouble() + "s";
            }
        }));
        averageServerTimePlotLine = chart.getYAxis(0).createPlotLine().setWidth(1);
//        chart.getYAxis(1).setAxisTitleText("Number of GPS-Fixes");
        
        serverTimeSeries = chart.createSeries().setName("Server Time").setYAxis(0);
//        overallTimeSeries = chart.createSeries().setName("Overall Time").setYAxis(0);
//        gpsFixAmountSeries = chart.createSeries().setName("Number of GPS-Fixes").setYAxis(1);
        
        this.setWidget(chart);
    }
    
    public void updateResults(QueryBenchmarkResult newResult) {
        serverTimeSeries.addPoint(newResult.getServerTime());
        serverTimeSum += newResult.getServerTime();
        updatePlotLines();
//        overallTimeSeries.addPoint(newResult.getOverallTime());
//        gpsFixAmountSeries.addPoint(newResult.getNumberOfGPSFixes());
        
        if (!serverTimeSeries.isVisible()) {
            chart.addSeries(serverTimeSeries);
//            chart.addSeries(overallTimeSeries);
//            chart.addSeries(gpsFixAmountSeries);
        }
    }
    
    private void updatePlotLines() {
        chart.getYAxis(0).removePlotLine(averageServerTimePlotLine);
        averageServerTimePlotLine.setValue(serverTimeSum / serverTimeSeries.getPoints().length);
        chart.getYAxis(0).addPlotLines(averageServerTimePlotLine);
    }

    public void reset() {
        serverTimeSum = 0;
        for (Series series : chart.getSeries()) {
            series.setPoints(new Number[0]);
        }
    }

    @Override
    public void onResize() {
        chart.setSizeToMatchContainer();
        // it's important here to recall the redraw method, otherwise the bug fix for wrong checkbox positions (nativeAdjustCheckboxPosition)
        // in the BaseChart class would not be called 
        chart.redraw();
    }

}
