package com.sap.sailing.gwt.ui.polarsheets;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.events.PointMouseOverEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class PolarSheetsChartPanel extends SimplePanel implements RequiresResize {

    private StringMessages stringMessages;
    private Chart chart;
    private Map<String, Series[]> seriesMap;

    public PolarSheetsChartPanel(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        setPixelSize(800, 800);
        chart = createPolarSheetChart();
        seriesMap = new HashMap<String, Series[]>();
        setWidget(chart);
    }

    private Chart createPolarSheetChart() {
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE)
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(1)).setZoomType(Chart.ZoomType.X_AND_Y)
                .setPolar(true).setSize(800, 800);
        polarSheetChart.setChartTitleText(stringMessages.polarSheetChart());
        polarSheetChart.getYAxis().setMin(0);
        return polarSheetChart;
    }

    private void newSeriesArray(String name) {
        if (!seriesMap.containsKey(name)) {
            Series[] seriesPerWindSpeed = new Series[13];
            seriesMap.put(name, seriesPerWindSpeed);
        } else {
            // TODO exception handling
        }
    }

    private void createSeriesForWindspeed(String name, int windSpeed) {
        Series[] seriesPerWindSpeed = seriesMap.get(name);
        Number[] forEachDeg = initializeDataForNewSeries();
        seriesPerWindSpeed[windSpeed] = chart.createSeries().setPoints(forEachDeg);
        seriesPerWindSpeed[windSpeed].setName(name + "-" + (windSpeed));
        chart.addSeries(seriesPerWindSpeed[windSpeed]);
    }

    private Number[] initializeDataForNewSeries() {
        Number[] forEachDeg = new Number[360];
        return forEachDeg;
    }

    private void addValuesToSeries(String seriesId, PolarSheetsData result) {
        if (seriesMap.containsKey(seriesId)) {
            for (int i = 0; i < 13; i++) {
                if (hasSufficientDataForWindspeed(result.getDataCountPerAngleForWindspeed(i), result.getDataCount())) {
                    if (seriesMap.get(seriesId)[i] == null) {
                        createSeriesForWindspeed(seriesId, i);
                    }
                    Series series = seriesMap.get(seriesId)[i];
                    series.setPoints(result.getAveragedPolarDataByWindSpeed()[i], false);
                    Point[] points = createPointsWithMarkerAlphaAccordingToDataCount(result, i);
                    if (points != null) {
                        series.setPoints(points);
                    }
                }
            }
        }
    }

    private boolean hasSufficientDataForWindspeed(Integer[] dataCountPerAngleForWindspeed, int countOverall) {
        int sum = 0;
        for (int count : dataCountPerAngleForWindspeed) {
            sum = sum + count;
        }
        //TODO make configurable
        if (sum >= (0.05 * countOverall)) {
            return true;
        }
        return false;
    }

    private Point[] createPointsWithMarkerAlphaAccordingToDataCount(PolarSheetsData result, int beaufort) {
        Point[] points = new Point[360];
        List<Integer> dataCountList = Arrays.asList(result.getDataCountPerAngleForWindspeed(beaufort));
        Integer max = Collections.max(dataCountList);
        if (max <= 0) {
            return null;
        }
        for (int i = 0; i < 360; i++) {
            points[i] = new Point(result.getAveragedPolarDataByWindSpeed()[beaufort][i]);
            if (points[i] == null) {
                points[i] = new Point(0);
            }
            double alpha = (double) dataCountList.get(i) / (double) max;
            int blue;
            int red;
            int radius;
            if (alpha > 0.2) {
                red = (int) (alpha * 255);
                blue = 0;
                radius = 4;
            } else {
                blue = (int) ((1 - alpha) * 255);
                red = 0;
                radius = 2;
            }

            // Don't let the markers be invisible
            alpha = 0.5 + 0.5 * alpha;
            // TODO maybe set to series color. Not sure if this (highcharts-generated color) can be queried before
            // rendering
            points[i].setMarker(new Marker().setFillColor(new Color(red, 0, blue, alpha)).setRadius(radius));
        }

        return points;
    }

    public void removeSeries(String seriesId) {
        if (seriesMap.containsKey(seriesId)) {
            Series[] seriesPerWindSpeed = seriesMap.get(seriesId);
            for (Series series : seriesPerWindSpeed) {
                chart.removeSeries(series);
            }
            seriesMap.remove(seriesId);
        }
    }

    public void removeAllSeries() {
        chart.removeAllSeries();
        seriesMap.clear();
    }

    public void setData(String id, PolarSheetsData result) {
        if (id == null) {
            // TODO Exception handling
            return;
        }
        if (!seriesMap.containsKey(id)) {
            newSeriesArray(id);
        }
        addValuesToSeries(id, result);
        chart.redraw();
    }

    public void setSeriesPointMouseOverHandler(PointMouseOverEventHandler pointMouseOverHandler) {
        chart.setSeriesPlotOptions(new SeriesPlotOptions().setPointMouseOverEventHandler(pointMouseOverHandler));
    }

    @Override
    public void onResize() {
        chart.setSizeToMatchContainer();
        chart.redraw();
    }

}
