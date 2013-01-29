package com.sap.sailing.gwt.ui.polarsheets;

import java.util.HashMap;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;

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
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE).setLinePlotOptions(new LinePlotOptions().setLineWidth(1)).setZoomType(Chart.ZoomType.X_AND_Y)
                .setPolar(true).setSize(700, 700);
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

    private void addValuesToSeries(String seriesId, Number[] values) {
        if (seriesMap.containsKey(seriesId)) {
            Series series = seriesMap.get(seriesId);
            series.setPoints(values, false);
        }
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

    public void setData(Number[] values, String id) {
        if (values == null) {
            // TODO Exception handling
            return;
        }
        if (!seriesMap.containsKey(id)) {
            newSeries(id);
        }
        addValuesToSeries(id, values);
        chart.redraw();
    }

}
