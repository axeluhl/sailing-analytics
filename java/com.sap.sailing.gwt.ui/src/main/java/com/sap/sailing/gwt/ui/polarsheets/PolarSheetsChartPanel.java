package com.sap.sailing.gwt.ui.polarsheets;

import java.util.HashMap;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;

import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class PolarSheetsChartPanel extends SimplePanel {

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
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE).setZoomType(Chart.ZoomType.X_AND_Y)
                .setPolar(true).setSize(700, 700);
        polarSheetChart.setChartTitleText(stringMessages.polarSheetChart());
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

    private void addDataToSeries(String seriesName, int deg, double value) {
        if (seriesMap.containsKey(seriesName)) {
            Series series = seriesMap.get(seriesName);
            series.addPoint(deg, value);
        }
    }

    public void removeSeries(String seriesName) {
        if (seriesMap.containsKey(seriesName)) {
            Series series = seriesMap.get(seriesName);
            chart.removeSeries(series);
            seriesMap.remove(seriesName);
        }
    }

    public void removeAllSeries() {
        chart.removeAllSeries();
        seriesMap.clear();
    }

    public void setData(Map<Integer, Double> data, String id) {
        if (data == null) {
            //TODO Exception handling
            return;
        }
        if (!seriesMap.containsKey(id)) {
            newSeries(id);
        }
        for (int i = 0; i < 360; i++) {
            addDataToSeries(id, i, data.get(i));
        }
        chart.redraw();
    }

}
