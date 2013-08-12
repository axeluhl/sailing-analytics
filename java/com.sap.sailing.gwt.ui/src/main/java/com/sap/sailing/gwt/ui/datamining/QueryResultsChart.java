package com.sap.sailing.gwt.ui.datamining;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.datamining.shared.GroupKey;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class QueryResultsChart extends SimplePanel {

    private StringMessages stringMessages;
    
    private Chart chart;
    private Map<GroupKey, Series> series;

    public QueryResultsChart(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        series = new HashMap<GroupKey, Series>();
        
        createChart();
        setWidget(chart);
    }

    public void showResult(QueryResult<Integer> result) {
        reset();
        
        List<GroupKey> keys = result.getSortedKeys();
        String[] categories = new String[keys.size()];
        int index = 0;
        for (GroupKey groupKey : keys) {
            categories[index] = groupKey.getMainKey().asString();
            index++;
        }
        chart.getXAxis().setCategories(categories);
        
        for (GroupKey key : keys) {
            Point point = new Point(key.getMainKey().asString(), result.getResults().get(key));
            getOrCreateSeries(key).addPoint(point);
        }
        
        for (Series series : this.series.values()) {
            chart.addSeries(series, false, false);
        }
        chart.redraw();
    }
    
    private Series getOrCreateSeries(GroupKey key) {
        if (!series.containsKey(key)) {
            series.put(key, chart.createSeries().setName(key.asString()));
        }
        return series.get(key);
    }
    
    private void reset() {
        chart.removeAllSeries(false);
        series = new HashMap<GroupKey, Series>();
    }

    private void createChart() {
        chart = new Chart().setType(Series.Type.COLUMN)
                .setMarginLeft(100)
                .setMarginRight(45)
                .setWidth100()
                .setCredits(new Credits().setEnabled(false))
                .setChartTitle(new ChartTitle().setText(stringMessages.dataMiningResult()));

        chart.getYAxis().setAxisTitleText("Result") //TODO
                .setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
                    @Override
                    public String format(AxisLabelsData axisLabelsData) {
                        try {
                            return axisLabelsData.getValueAsDouble() + "";
                        } catch (Exception e) {
                            return "";
                        }
                    }
                }));

//        chart.setToolTip(new ToolTip()
//                .setPointFormat("<span style=\"color:{series.color}\">{series.name}</span>: <b>{point.y}s</b><br/>"));
    }

}
