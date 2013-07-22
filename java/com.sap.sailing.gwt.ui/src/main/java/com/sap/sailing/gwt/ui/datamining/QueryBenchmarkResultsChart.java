package com.sap.sailing.gwt.ui.datamining;

import java.util.ArrayList;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.PlotLine;
import org.moxieapps.gwt.highcharts.client.PlotLine.DashStyle;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;

public class QueryBenchmarkResultsChart extends SimplePanel implements RequiresResize {
    
    private Chart chart;
    private Series serverTimeSeries;
    private double serverTimeSum = 0;
    private PlotLine averageServerTimePlotLine;
    
    private Series overallTimeSeries;
    private double overallTimeSum = 0;
    private PlotLine averageOverallTimePlotLine;
    
    private List<QueryBenchmarkResult> results;
    
    public QueryBenchmarkResultsChart() {
        results = new ArrayList<QueryBenchmarkResult>();
        chart = new Chart()
                      .setMarginLeft(100)
                      .setMarginRight(45)
                      .setWidth100()
                      .setCredits(new Credits().setEnabled(false))
                      .setChartTitle(new ChartTitle().setText("Query Benchmark Results"));
        
        chart.getXAxis().setType(Axis.Type.LINEAR).setAllowDecimals(false);

        chart.getYAxis().setAxisTitleText("Time").setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                try {
                    return axisLabelsData.getValueAsDouble() + "s";
                } catch (Exception e) {
                    return "";
                }
            }
        }));
        
        chart.setToolTip(new ToolTip().setPointFormat("<span style=\"color:{series.color}\">{series.name}</span>: <b>{point.y}s</b><br/>"));
        
        serverTimeSeries = chart.createSeries().setName("Server Time").setPlotOptions(new LinePlotOptions().setColor("#000099").setMarker(new Marker().setEnabled(false)));
        averageServerTimePlotLine = chart.getYAxis().createPlotLine().setColor("#000099").setWidth(2).setDashStyle(DashStyle.SOLID);
        
        overallTimeSeries = chart.createSeries().setName("Overall Time").setPlotOptions(new LinePlotOptions().setColor("#00bb00").setMarker(new Marker().setEnabled(false)));
        averageOverallTimePlotLine = chart.getYAxis().createPlotLine().setColor("#00bb00").setWidth(2).setDashStyle(DashStyle.SOLID);
        
        this.setWidget(chart);
    }
    
    public void addResult(QueryBenchmarkResult newResult) {
        results.add(newResult);
    }
    
    public void showResults() {
        int i = 0;
        Point[] serverTimePoints = new Point[results.size()];
        Point[] overallTimePoints = new Point[results.size()];
        for (QueryBenchmarkResult result : results) {
            serverTimePoints[i] = new Point(i + 1, result.getServerTime()).setName(result.getIdentifier());
            serverTimeSum += result.getServerTime();
            overallTimePoints[i] = new Point(i + 1, result.getOverallTime()).setName(result.getIdentifier());
            overallTimeSum += result.getOverallTime();
            i++;
        }
        
        //This method has to be called, before the points are added to the chart.
        updateChartSubtitle();
        serverTimeSeries.setPoints(serverTimePoints);
        overallTimeSeries.setPoints(overallTimePoints);
        updatePlotLines();
        
        if (!serverTimeSeries.isVisible()) {
            chart.addSeries(serverTimeSeries);
            chart.addSeries(overallTimeSeries);
        }
    }
    
    private void updateChartSubtitle() {
        StringBuilder subtitelBuilder = new StringBuilder();
        subtitelBuilder.append("Number of GPS-Fixes: " + results.get(0).getNumberOfGPSFixes());
        subtitelBuilder.append(" - \u2300 Server Time: " + getAverageServerTime() + "s");
        subtitelBuilder.append(" - \u2300 Overall Time: " + getAverageOverallTime() + "s");
        
        chart.setChartSubtitle(new ChartSubtitle().setText(subtitelBuilder.toString()));
        //This is needed, so that the subtitle is updated. Otherwise the text would stay empty
        this.setWidget(null);
        this.setWidget(chart);
    }

    private void updatePlotLines() {
        chart.getYAxis().removePlotLine(averageServerTimePlotLine);
        chart.getYAxis().removePlotLine(averageOverallTimePlotLine);
        averageServerTimePlotLine.setValue(getAverageServerTime());
        averageOverallTimePlotLine.setValue(getAverageOverallTime());
        chart.getYAxis().addPlotLines(averageServerTimePlotLine, averageOverallTimePlotLine);
    }

    private double getAverageOverallTime() {
        return overallTimeSum / results.size();
    }

    private double getAverageServerTime() {
        return serverTimeSum / results.size();
    }

    public void reset() {
        results.clear();
        serverTimeSum = 0;
        overallTimeSum = 0;
        serverTimeSeries.setPoints(new Point[0]);
        overallTimeSeries.setPoints(new Point[0]);
    }

    @Override
    public void onResize() {
        chart.setSizeToMatchContainer();
        // it's important here to recall the redraw method, otherwise the bug fix for wrong checkbox positions (nativeAdjustCheckboxPosition)
        // in the BaseChart class would not be called 
        chart.redraw();
    }

}
