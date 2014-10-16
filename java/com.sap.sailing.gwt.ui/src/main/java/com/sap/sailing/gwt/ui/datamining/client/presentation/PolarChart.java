package com.sap.sailing.gwt.ui.datamining.client.presentation;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;

import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;

/**
 * Expects the inner key in the group key to be the angle for the polar chart. (x-value) Maybe the angle should be
 * transfered as part of the result value in the future. But in the backend the angle is treated as a key (for polar
 * sheets) for grouping all polar fixes with the same rounded angle
 * 
 * @author Frederik Petersen D054528
 * 
 */
public class PolarChart implements ResultsPresenter<Number> {

    private final Chart chart = createPolarChartWidget();

    private final Map<String, Series> dataByKeyStringAndAngle = new HashMap<String, Series>();

    @Override
    public Widget getWidget() {
        return chart;
    }

    /**
     * Creates a polar diagram chart of the Line type.
     */
    private Chart createPolarChartWidget() {
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE)
                .setLinePlotOptions(new LinePlotOptions().setLineWidth(1))
                .setPolar(true).setHeight100().setWidth100();
        polarSheetChart.getYAxis().setMin(0);
        return polarSheetChart;
    }

    @Override
    public void showResult(QueryResult<Number> result) {
        resetChartData();
        Map<GroupKey, Number> results = result.getResults();
        if (!results.isEmpty()) {
            for (Entry<GroupKey, Number> resultEntry : results.entrySet()) {
                GroupKey key = resultEntry.getKey();
                StringBuilder keyStringBuilder = new StringBuilder();
                GroupKey currentKey = key;
                while (currentKey.hasSubKey()) {
                    keyStringBuilder.append(currentKey.getMainKey());
                    currentKey = currentKey.getSubKey();
                }
                int angle = Integer.parseInt(currentKey.toString());
                String keyString = keyStringBuilder.toString();
                Series series = dataByKeyStringAndAngle.get(keyString);
                if (series == null) {
                    series = chart.createSeries();
                    dataByKeyStringAndAngle.put(keyString, series);
                }
                series.addPoint(angle, resultEntry.getValue());
                series.setName(keyString);
            }
        }
    }

    private void resetChartData() {
        dataByKeyStringAndAngle.clear();
        chart.removeAllSeries();
    }

    @Override
    public void showError(String error) {
        // TODO Auto-generated method stub

    }

    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        // TODO Auto-generated method stub

    }

}
