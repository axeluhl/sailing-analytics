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
    private Map<String, Series> seriesMap;

    public PolarSheetsChartPanel(StringMessages stringMessages) {
        this.stringMessages = stringMessages;
        setSize("100%", "100%");
        chart = createPolarSheetChart();
        seriesMap = new HashMap<String, Series>();
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

    private void newSeries(String name) {
        if (!seriesMap.containsKey(name)) {
            Number[] forEachDeg = initializeDataForNewSeries();
            Series newSeries = chart.createSeries().setPoints(forEachDeg);
            newSeries.setName(name);
            chart.addSeries(newSeries);
            seriesMap.put(name, newSeries);
        } else {
            // TODO exception handling
        }
    }

    private Number[] initializeDataForNewSeries() {
        Number[] forEachDeg = new Number[360];
        return forEachDeg;
    }

    private void addValuesToSeries(String seriesId, PolarSheetsData result) {
        if (seriesMap.containsKey(seriesId)) {
            Series series = seriesMap.get(seriesId);
            series.setPoints(result.getValues(), false);
            Point[] points = createPointsWithMarkerAlphaAccordingToDataCount(result);
            if (points != null) {
                series.setPoints(points);
            }
        }
    }

    private Point[] createPointsWithMarkerAlphaAccordingToDataCount(PolarSheetsData result) {
        Point[] points = new Point[360];
        List<Integer> dataCountList = Arrays.asList(result.getDataCountPerAngle());
        Integer max = Collections.max(dataCountList);
        if (max <= 0) {
            return null;
        }
        for (int i = 0; i < 360; i++) {
            points[i] = new Point(result.getValues()[i]);
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
            Series series = seriesMap.get(seriesId);
            chart.removeSeries(series);
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
            newSeries(id);
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
