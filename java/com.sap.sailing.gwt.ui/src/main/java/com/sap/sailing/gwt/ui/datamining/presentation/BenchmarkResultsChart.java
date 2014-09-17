package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
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
import com.sap.sailing.gwt.ui.client.StringMessages;

public class BenchmarkResultsChart extends SimplePanel implements RequiresResize {

    private StringMessages stringMessages;
    private List<BenchmarkResult> results;
    private double thresholdFactor;
    private Chart chart;
    
    private Series serverTimeSeries;
    private Series cleanedServerTimeSeries;
    private PlotLine averageServerTimePlotLine;

    private Series overallTimeSeries;
    private Series cleanedOverallTimeSeries;
    private PlotLine averageOverallTimePlotLine;
    
    public BenchmarkResultsChart(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        
        results = new ArrayList<BenchmarkResult>();
        thresholdFactor = 5;
        
        createChart();
        setWidget(chart);
    }
    
    public void addResult(BenchmarkResult newResult) {
        results.add(newResult);
    }
    
    public void showResults() {
        int x = 1;
        List<Point> serverTimePoints = new ArrayList<Point>();
        List<Point> overallTimePoints = new ArrayList<Point>();
        for (BenchmarkResult result : results) {
            serverTimePoints.add(new Point(x, result.getServerTime()).setName(result.getIdentifier()));
            overallTimePoints.add(new Point(x, result.getOverallTime()).setName(result.getIdentifier()));
            x++;
        }

        Point[] cleanedServerTimePoints = cleanPoints(serverTimePoints);
        Point[] cleanedOverallTimePoints = cleanPoints(overallTimePoints);
        double averageServerTime = getAverage(cleanedServerTimePoints);
        double averageOverallTime = getAverage(cleanedOverallTimePoints);
        
        //This method has to be called, before the points are added to the chart.
        updateChartSubtitle(results.get(0).getDataAmount(), averageServerTime, averageOverallTime);
        serverTimeSeries.setPoints(serverTimePoints.toArray(new Point[serverTimePoints.size()]));
        cleanedServerTimeSeries.setPoints(cleanedServerTimePoints);
        cleanedServerTimeSeries.setName(stringMessages.cleanedServerTime() + "(" + (serverTimePoints.size() - cleanedServerTimePoints.length) + ")");
        overallTimeSeries.setPoints(overallTimePoints.toArray(new Point[overallTimePoints.size()]));
        cleanedOverallTimeSeries.setPoints(cleanedOverallTimePoints);
        cleanedOverallTimeSeries.setName(stringMessages.cleanedOverallTime() + "(" + (overallTimePoints.size() - cleanedOverallTimePoints.length) + ")");
        updatePlotLines(averageServerTime, averageOverallTime);
        
        ensureChartContainsSeries();
    }

    private void ensureChartContainsSeries() {
        Series[] allSeries = new Series[] {serverTimeSeries, cleanedServerTimeSeries, overallTimeSeries, cleanedOverallTimeSeries};
        for (Series series : allSeries) {
            boolean isContained = false;
            for (Series containedSeries : chart.getSeries()) {
                if (series.equals(containedSeries)) {
                    isContained = true;
                    break;
                }
            }
            if (!isContained) {
                chart.addSeries(series);
            }
        }
    }
    
    private double getAverage(Point[] points) {
        double sum = 0;
        for (Point point : points) {
            sum += point.getY().doubleValue();
        }
        return sum / points.length;
    }

    private Point[] cleanPoints(List<Point> points) {
        List<Point> sortedPoints = new ArrayList<Point>(points);
        Collections.sort(sortedPoints, new Comparator<Point>() {
            @Override
            public int compare(Point p1, Point p2) {
                return Double.compare(p1.getY().doubleValue(), p2.getY().doubleValue());
            }
        });
        
        double lowerQuartil = getQuantil(0.25, sortedPoints);
        double median = getQuantil(0.5, sortedPoints);
        double upperQuartil = getQuantil(0.75, sortedPoints);
        double interquartilRange = upperQuartil - lowerQuartil;
        double minThreshold = median - interquartilRange * thresholdFactor;
        double maxThreshold = median + interquartilRange * thresholdFactor;

        List<Point> pointsToOperateOn = new ArrayList<Point>(points);
        Iterator<Point> pointsIterator = pointsToOperateOn.iterator();
        while (pointsIterator.hasNext()) {
            Point point = (Point) pointsIterator.next();
            if (point.getY().doubleValue() < minThreshold || point.getY().doubleValue() > maxThreshold) {
                pointsIterator.remove();
            }
        }
        
        return pointsToOperateOn.toArray(new Point[pointsToOperateOn.size()]);
    }
    
    private double getQuantil(double p, List<Point> sortedList) {
        double indexAsDouble = p * sortedList.size();
        if (indexAsDouble - (int) indexAsDouble <= 0.000001) {
            int index = (int) indexAsDouble;
            Point p1 = sortedList.get(index);
            Point p2 = sortedList.get(index + 1);
            return (p1.getY().doubleValue() + p2.getY().doubleValue()) * 0.5;
        } else {
            int index = (int) Math.ceil(indexAsDouble);
            return sortedList.get(index).getY().doubleValue();
        }
    }

    private void updateChartSubtitle(int dataAmount, double averageServerTime, double averageOverallTime) {
        StringBuilder subtitelBuilder = new StringBuilder();
        subtitelBuilder.append(stringMessages.dataAmount() + ": " + dataAmount);
        subtitelBuilder.append(" - " + stringMessages.averageCleanedServerTime() + ": " + averageServerTime + "s");
        subtitelBuilder.append(" - " + stringMessages.averageCleanedOverallTime() + ": " + averageOverallTime + "s");
        
        chart.setChartSubtitle(new ChartSubtitle().setText(subtitelBuilder.toString()));
        //This is needed, so that the subtitle is updated. Otherwise the text would stay empty
        this.setWidget(null);
        this.setWidget(chart);
    }

    private void updatePlotLines(double averageServerTime, double averageOverallTime) {
        chart.getYAxis().removePlotLine(averageServerTimePlotLine);
        chart.getYAxis().removePlotLine(averageOverallTimePlotLine);
        averageServerTimePlotLine.setValue(averageServerTime);
        averageOverallTimePlotLine.setValue(averageOverallTime);
        chart.getYAxis().addPlotLines(averageServerTimePlotLine, averageOverallTimePlotLine);
    }

    public void reset() {
        results.clear();
        for (Series series : chart.getSeries()) {
            series.setPoints(new Point[0]);
        }
    }

    private void createChart() {
        chart = new Chart()
                      .setMarginLeft(100)
                      .setMarginRight(45)
                      .setWidth100()
                      .setHeight100()
                      .setBorderColor(new Color("#F0AB00"))
                      .setPlotBorderWidth(0)
                      .setCredits(new Credits().setEnabled(false))
                      .setChartTitle(new ChartTitle().setText(stringMessages.dataMiningBenchmarkResults()));
        
        chart.getXAxis().setType(Axis.Type.LINEAR).setAllowDecimals(false);

        chart.getYAxis().setAxisTitleText(stringMessages.time()).setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
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

        serverTimeSeries = chart.createSeries().setName(stringMessages.serverTime()).setPlotOptions(new LinePlotOptions().setColor("#656565").setMarker(new Marker().setEnabled(false)));
        cleanedServerTimeSeries = chart.createSeries().setName(stringMessages.cleanedServerTime()).setPlotOptions(new LinePlotOptions().setColor("#000099").setMarker(new Marker().setEnabled(false)));
        averageServerTimePlotLine = chart.getYAxis().createPlotLine().setColor("#000099").setWidth(2).setDashStyle(DashStyle.SOLID);

        overallTimeSeries = chart.createSeries().setName(stringMessages.overallTime()).setPlotOptions(new LinePlotOptions().setColor("#656565").setMarker(new Marker().setEnabled(false)));
        cleanedOverallTimeSeries = chart.createSeries().setName(stringMessages.cleanedOverallTime()).setPlotOptions(new LinePlotOptions().setColor("#00bb00").setMarker(new Marker().setEnabled(false)));
        averageOverallTimePlotLine = chart.getYAxis().createPlotLine().setColor("#00bb00").setWidth(2).setDashStyle(DashStyle.SOLID);
    }

    @Override
    public void onResize() {
        chart.setSizeToMatchContainer();
        chart.redraw();
    }

}
