package com.sap.sailing.gwt.ui.datamining;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.datamining.shared.GroupKey;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class QueryResultsChart extends SimplePanel {

    private StringMessages stringMessages;
    
    private Chart chart;
    private Map<GroupKey, Series> series;
    
    private Map<GroupKey, Integer> mainKeyToValueMap;
    private Map<Integer, GroupKey> valueToGroupKeyMap;

    public QueryResultsChart(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        series = new HashMap<GroupKey, Series>();
        
        createChart();
        setWidget(chart);
    }

    public void showResult(QueryResult<Integer> result) {
        reset();
        
        chart.getYAxis().setAxisTitleText(result.getResultSignifier());
        updateChartSubtitle(result);
        
        List<GroupKey> keys = getSortedKeysFrom(result);
        buildGroupKeyValueMaps(keys);
        
        Map<Series, Boolean> isInChart = new HashMap<Series, Boolean>();
        for (GroupKey key : keys) {
            Point point = new Point(mainKeyToValueMap.get(key.getMainKey()), result.getResults().get(key));
            point.setName(key.getMainKey().asString());
            Series series = getOrCreateSeries(key).addPoint(point, false, false, false);
            if (isInChart.get(series) == null || !isInChart.get(series)) {
                chart.addSeries(series, false, false);
                isInChart.put(series, true);
            }
        }

        chart.redraw();
    }
    
    private void updateChartSubtitle(QueryResult<Integer> result) {
        chart.setChartSubtitle(new ChartSubtitle().setText(stringMessages.queryResultsChartSubtitle(result.getFilteredDataAmount(), result.getCalculationTimeInSeconds())));
        //This is needed, so that the subtitle is updated. Otherwise the text would stay empty
        this.setWidget(null);
        this.setWidget(chart);
        
    }

    public List<GroupKey> getSortedKeysFrom(QueryResult<Integer> result) {
        List<GroupKey> sortedKeys = new ArrayList<GroupKey>(result.getResults().keySet());
        Collections.sort(sortedKeys, new Comparator<GroupKey>() {
            @Override
            public int compare(GroupKey key1, GroupKey key2) {
                return key1.asString().compareTo(key2.asString());
            }
        });
        return sortedKeys;
    }

    private void buildGroupKeyValueMaps(Collection<GroupKey> keys) {
        mainKeyToValueMap = new HashMap<GroupKey, Integer>();
        valueToGroupKeyMap = new HashMap<Integer, GroupKey>();
        int index = 0;
        for (GroupKey groupKey : keys) {
            if (!mainKeyToValueMap.containsKey(groupKey.getMainKey())) {
                mainKeyToValueMap.put(groupKey.getMainKey(), index);
                valueToGroupKeyMap.put(index, groupKey.getMainKey());
                index++;
            }
        }
    }
    
    private Series getOrCreateSeries(GroupKey groupKey) {
        GroupKey key = groupKey.hasSubKey() ? groupKey.getSubKey() : groupKey;
        
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
        
        chart.getXAxis().setAllowDecimals(false).setLabels(new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                try {
                    Integer value = (int) axisLabelsData.getValueAsDouble();
                    return valueToGroupKeyMap.get(value).asString();
                } catch (Exception e) {
                    return "error formatting label";
                }
            }
        }));

        chart.getYAxis().setAxisTitleText("Result")
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
