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

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class PolarSheetsChartPanel extends DockLayoutPanel {

    private StringMessages stringMessages;
    private Chart chart;
    private Map<String, Series[]> seriesMap;
    
    private Map<Series, String> nameForSeries = new HashMap<Series, String>();
    private PolarSheetGenerationSettings settings;

    public PolarSheetsChartPanel(StringMessages stringMessages) {
        super(Unit.PCT);
        this.stringMessages = stringMessages;
        setSize("100%", "100%");
        chart = createPolarSheetChart();
        seriesMap = new HashMap<String, Series[]>();
        add(chart);
    }

    private Chart createPolarSheetChart() {
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE)
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(1)).setZoomType(Chart.ZoomType.X_AND_Y)
                .setPolar(true).setHeight100().setWidth100();
        polarSheetChart.setChartTitleText(stringMessages.polarSheetChart());
        polarSheetChart.getYAxis().setMin(0);
        return polarSheetChart;
    }

    private void newSeriesArray(String name, int steppingCount) {
        if (!seriesMap.containsKey(name)) {
            Series[] seriesPerWindSpeed = new Series[steppingCount];
            seriesMap.put(name, seriesPerWindSpeed);
        } else {
            // TODO exception handling
        }
    }

    private void createSeriesForWindspeed(String name, int windSpeedLevel, int windSpeed) {
        Series[] seriesPerWindSpeed = seriesMap.get(name);
        Number[] forEachDeg = initializeDataForNewSeries();
        seriesPerWindSpeed[windSpeedLevel] = chart.createSeries().setPoints(forEachDeg);
        String actualSeriesName = name + "-" + windSpeed;
        seriesPerWindSpeed[windSpeedLevel].setName(actualSeriesName);
        nameForSeries.put(seriesPerWindSpeed[windSpeedLevel],actualSeriesName);
        chart.addSeries(seriesPerWindSpeed[windSpeedLevel]);
    }

    private Number[] initializeDataForNewSeries() {
        Number[] forEachDeg = new Number[360];
        return forEachDeg;
    }

    private void addValuesToSeries(String seriesId, PolarSheetsData result) {
        int stepCount = result.getStepping().getRawStepping().length;
        if (seriesMap.containsKey(seriesId)) {
            for (int i = 0; i < stepCount; i++) {
                if (hasSufficientDataForWindspeed(result.getDataCountPerAngleForWindspeed(i))) {
                    if (seriesMap.get(seriesId)[i] == null) {
                        createSeriesForWindspeed(seriesId, i, result.getStepping().getRawStepping()[i]);
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

    private boolean hasSufficientDataForWindspeed(Integer[] dataCountPerAngleForWindspeed) {
        int sum = 0;
        for (int count : dataCountPerAngleForWindspeed) {
            sum = sum + count;
        }
        if (sum >= settings.getMinimumDataCountPerGraph()) {
            return true;
        }
        return false;
    }

    private Point[] createPointsWithMarkerAlphaAccordingToDataCount(PolarSheetsData result, int windspeed) {
        Point[] points = new Point[360];
        List<Integer> dataCountList = Arrays.asList(result.getDataCountPerAngleForWindspeed(windspeed));
        Integer max = Collections.max(dataCountList);
        if (max <= 0) {
            return null;
        }
        for (int i = 0; i < 360; i++) {
            if (result.getHistogramDataMap().get(windspeed) == null
                    || result.getHistogramDataMap().get(windspeed).get(i) == null
                    || result.getHistogramDataMap().get(windspeed).get(i).getConfidenceMeasure() < settings
                            .getMinimumConfidenceMeasure()) {
                continue;
            }
            if (points[i] == null) {
                points[i] = new Point(result.getAveragedPolarDataByWindSpeed()[windspeed][i]);
            }
            double alpha = (double) dataCountList.get(i) / (double) max;
            int blue;
            int red;
            int radius;
            if (alpha > 0.2) {
                red = (int) (alpha * 255);
                blue = 0;
                radius = 2;
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
            newSeriesArray(id, result.getStepping().getRawStepping().length);
        }
        addValuesToSeries(id, result);
        chart.redraw();
    }

    public void setSeriesPointMouseOverHandler(PointMouseOverEventHandler pointMouseOverHandler) {
        chart.setSeriesPlotOptions(new SeriesPlotOptions().setPointMouseOverEventHandler(pointMouseOverHandler));
    }

    public Series[] getSeriesPerWindspeedForName(String name) {
        return seriesMap.get(name);
    }
    
    public String getNameForSeries(Series series) {
        return nameForSeries.get(series);
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

    public void showLoadingInfo() {
        // TODO string messages
        chart.showLoading("Generating Polar Sheets");
        
    }

    public void hideLoadingInfo() {
        chart.hideLoading();
        
    }

    public void setSettings(PolarSheetGenerationSettings newSettings) {
        settings = newSettings;   
    }

}
