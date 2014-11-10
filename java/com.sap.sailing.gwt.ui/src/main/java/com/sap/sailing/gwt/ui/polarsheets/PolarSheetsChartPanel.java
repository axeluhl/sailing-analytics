package com.sap.sailing.gwt.ui.polarsheets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.BaseChart;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.events.PointSelectEventHandler;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.SeriesPlotOptions;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.sap.sailing.domain.common.PolarSheetGenerationSettings;
import com.sap.sailing.domain.common.PolarSheetsData;
import com.sap.sailing.gwt.ui.client.StringMessages;

/**
 * Highcharts-driven chart for displaying polar sheets.
 * 
 * @author d054528 Frederik Petersen
 *
 */
public class PolarSheetsChartPanel extends DockLayoutPanel {

    private StringMessages stringMessages;
    private Chart chart;
    private Map<String, Series[]> seriesMap;
    
    private Map<Series, String> nameForSeries = new HashMap<Series, String>();
    private PolarSheetGenerationSettings settings;
    
    private final Map<String,PolarSheetsData> polarSheetsDataMap;
    
    private final AngleOverDataSizeHistogramPanel angleOverDataSizeHistogramPanel;

    public PolarSheetsChartPanel(StringMessages stringMessages, AngleOverDataSizeHistogramPanel angleOverDataSizeHistogramPanel) {
        super(Unit.PCT);
        this.stringMessages = stringMessages;
        this.angleOverDataSizeHistogramPanel = angleOverDataSizeHistogramPanel;
        polarSheetsDataMap = new HashMap<String, PolarSheetsData>();
        setSize("100%", "100%");
        chart = createPolarSheetChart();
        seriesMap = new HashMap<String, Series[]>();
        add(chart);
    }

    /**
     * Creates a polar diagram chart of the Line type.
     */
    private Chart createPolarSheetChart() {
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE)
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(1)).setZoomType(BaseChart.ZoomType.X_AND_Y)
                .setPolar(true).setHeight100().setWidth100();
        polarSheetChart.setChartTitleText(stringMessages.polarSheetChart());
        polarSheetChart.getYAxis().setMin(0);
        return polarSheetChart;
    }

    /**
     * Adds a new Array of series to the map. Each Array represents one set of generated polar diagrams The array is
     * empty and needs to be filled.
     * 
     * @param name
     *            Unique name of the set of diagrams.
     * @param steppingCount
     *            Number of different wind speeds, the polar sheets are generated for.
     */
    private void newSeriesArray(String name, int steppingCount) {
        if (!seriesMap.containsKey(name)) {
            Series[] seriesPerWindSpeed = new Series[steppingCount];
            seriesMap.put(name, seriesPerWindSpeed);
        } else {
            Window.alert(stringMessages.errorWhileAddingSeriesToChart());
        }
    }

    /**
     * Creates a series for a specific windspeed and also creates the unique name needed to identify the series.
     * 
     * @param name Unique name of the set of diagrams.
     * @param windSpeedLevel The id of the windspeed level in the windspeed steppings
     * @param windSpeed The actual windspeed
     */
    private void createSeriesForWindspeed(String name, int windSpeedLevel, int windSpeed) {
        Series[] seriesPerWindSpeed = seriesMap.get(name);
        Number[] forEachDeg = initializeDataForNewSeries();
        seriesPerWindSpeed[windSpeedLevel] = chart.createSeries().setPoints(forEachDeg);
        String actualSeriesName = name + "-" + windSpeed;
        seriesPerWindSpeed[windSpeedLevel].setName(actualSeriesName);
        nameForSeries.put(seriesPerWindSpeed[windSpeedLevel],actualSeriesName);
        chart.addSeries(seriesPerWindSpeed[windSpeedLevel]);
    }

    /**
     * @return An array of 360 numbers. One for each angle.
     */
    private Number[] initializeDataForNewSeries() {
        Number[] forEachDeg = new Number[360];
        return forEachDeg;
    }

    /**
     * Adds the actual data to the series for each windspeed index. Series array needs to be initialized before calling
     * this method.
     */
    private void addValuesToSeries(String seriesId, PolarSheetsData result) {
        int stepCount = result.getStepping().getRawStepping().length;
        if (seriesMap.containsKey(seriesId)) {
            for (int i = 0; i < stepCount; i++) {
                if (hasSufficientDataForWindspeed(result.getDataCountPerAngleForWindspeed(i))) {
                    if (seriesMap.get(seriesId)[i] == null) {
                        createSeriesForWindspeed(seriesId, i, result.getStepping().getRawStepping()[i]);
                    }
                    Series series = seriesMap.get(seriesId)[i];
                    //series.setPoints(result.getAveragedPolarDataByWindSpeed()[i], false);
                    Point[] points = createPointsWithMarkerAlphaAccordingToDataCount(result, i);
                    if (points != null) {
                        series.setPoints(points);
                        angleOverDataSizeHistogramPanel.addData(result.getHistogramDataMap().get(i));
                    }
                }
            }
        }
    }

    /**
     * @return true if the minimum amount of datapoints is reached as defined in the settings.
     * (for the complete graph)
     */
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

    /**
     * Points are created using the polar sheets generation results. Points color and alpha in the graph is calculated
     * using statistical measures.
     */
    private Point[] createPointsWithMarkerAlphaAccordingToDataCount(PolarSheetsData result, int windspeed) {
        Point[] points = new Point[360];
        List<Integer> dataCountList = Arrays.asList(result.getDataCountPerAngleForWindspeed(windspeed));
        Integer max = Collections.max(dataCountList);
        if (max < settings.getMinimumDataCountPerAngle()) {
            return null;
        }
        for (int i = 0; i < 360; i++) {
            if (result.getHistogramDataMap().get(windspeed) == null
                    || result.getHistogramDataMap().get(windspeed).get(i) == null
                    || result.getHistogramDataMap().get(windspeed).get(i).getConfidenceMeasure() < settings
                            .getMinimumConfidenceMeasure()
                    || result.getHistogramDataMap().get(windspeed).get(i).getDataCount() < settings
                            .getMinimumDataCountPerAngle()) {
                points[i] = new Point(0);
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
            points[i].setMarker(new Marker().setFillColor(new Color(red, 0, blue, alpha)).setRadius(radius));
        }

        return points;
    }

    /**
     * Removes the set of series from the chart and the id-series[] map
     * 
     * @param seriesId Identifier for the set of series for one polar sheet generation result.
     */
    public void removeSeries(String seriesId) {
        if (seriesMap.containsKey(seriesId)) {
            Series[] seriesPerWindSpeed = seriesMap.get(seriesId);
            for (Series series : seriesPerWindSpeed) {
                chart.removeSeries(series);
            }
            seriesMap.remove(seriesId);
            polarSheetsDataMap.remove(seriesId);
        }
    }

    /**
     * Remove every series from the chart and the id-series[] map.
     */
    public void removeAllSeries() {
        chart.removeAllSeries();
        seriesMap.clear();
        polarSheetsDataMap.clear();
    }

    /**
     * Adds a the results of a polar sheet generation to the chart panel. Performs all necessary steps including adding
     * the set of series to the map and redrawing the chart.
     * 
     * @param id Set of series identifier. 
     * @param result Generation results.
     */
    public void setData(String id, PolarSheetsData result) {
        if (id == null) {
            stringMessages.errorWhileAddingSeriesToChart();
            return;
        } else {
            if (!seriesMap.containsKey(id)) {
                newSeriesArray(id, result.getStepping().getRawStepping().length);
            }
            addValuesToSeries(id, result);
            polarSheetsDataMap.put(id, result);
            chart.redraw();
        }
    }

    /**
     * Allows setting the point select handler for the chart.
     */
    public void setPointSelectHandler(PointSelectEventHandler pointSelectEventHandler) {
        chart.setSeriesPlotOptions(new SeriesPlotOptions().setAllowPointSelect(true).setPointSelectEventHandler(
                pointSelectEventHandler));
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
        chart.showLoading(stringMessages.generatingPolarSheet());
        
    }

    public void hideLoadingInfo() {
        chart.hideLoading();
        
    }

    public void setSettings(PolarSheetGenerationSettings newSettings) {
        settings = newSettings;   
    }
    
    public List<String> getAllSeriesNames() {
        List<String> result = new ArrayList<String>();
        for (Entry<String, Series[]> entry : seriesMap.entrySet()) {
            result.add(entry.getKey());
        }
        return result;
    }

    public Map<String, PolarSheetsData> getPolarSheetsDataMap() {
        return polarSheetsDataMap;
    }

}
