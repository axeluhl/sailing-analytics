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
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.user.client.ui.Label;
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
    
    private Label noQuerySelectedLabel;

    public QueryResultsChart(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        series = new HashMap<GroupKey, Series>();
        noQuerySelectedLabel = new Label(this.stringMessages.noQuerySelected() + ".");
        noQuerySelectedLabel.setStyleName("chart-importantMessage");
        
        createChart();
        setWidget(noQuerySelectedLabel);
    }

    public void showResult(QueryResult<? extends Number> result) {
        reset();
        
        updateYAxisLabels(result);
        updateChartSubtitleAndSetChartAsWidget(result);
        
        List<GroupKey> sortedKeys = getSortedKeysFrom(result);
        buildGroupKeyValueMaps(sortedKeys);
        
        if (resultHasComplexKeys(result)) {
            displayComplexResult(result, sortedKeys);            
        } else {
            displaySimpleResult(result, sortedKeys);
        }

        chart.redraw();
    }

	private void updateYAxisLabels(QueryResult<? extends Number> result) {
		chart.getYAxis().setAxisTitleText(result.getResultSignifier());
		chart.setToolTip(new ToolTip().setValueDecimals(result.getValueDecimals()).setValueSuffix(UnitFormatter.format(result.getUnit())));
	}

    private boolean resultHasComplexKeys(QueryResult<? extends Number> result) {
        for (GroupKey key : result.getResults().keySet()) {
            if (key.hasSubKey()) {
                return true;
            }
        }
        return false;
    }

    private void displayComplexResult(QueryResult<? extends Number> result, List<GroupKey> sortedKeys) {
        Map<Series, Boolean> isInChart = new HashMap<Series, Boolean>();
        for (GroupKey key : sortedKeys) {
            Point point = new Point(mainKeyToValueMap.get(key.getMainKey()), result.getResults().get(key));
            point.setName(key.getMainKey().asString());
            Series series = getOrCreateSeries(key).addPoint(point, false, false, false);
            if (isInChart.get(series) == null || !isInChart.get(series)) {
                chart.addSeries(series, false, false);
                isInChart.put(series, true);
            }
        }
    }

    private void displaySimpleResult(QueryResult<? extends Number> result, List<GroupKey> sortedKeys) {
        Series series = chart.createSeries().setName("Results");
        for (GroupKey key : sortedKeys) {
            Point point = new Point(mainKeyToValueMap.get(key.getMainKey()), result.getResults().get(key));
            point.setName(key.getMainKey().asString());
            series.addPoint(point, false, false, false);
        }
        chart.addSeries(series, false, false);
    }
    
    private void updateChartSubtitleAndSetChartAsWidget(QueryResult<? extends Number> result) {
        chart.setChartSubtitle(new ChartSubtitle().setText(stringMessages.queryResultsChartSubtitle(result.getRetrievedDataAmount(), result.getFilteredDataAmount(), result.getCalculationTimeInSeconds())));
        //This is needed, so that the subtitle is updated. Otherwise the text would stay empty
        this.setWidget(null);
        this.setWidget(chart);
        
    }

    public List<GroupKey> getSortedKeysFrom(QueryResult<? extends Number> result) {
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
