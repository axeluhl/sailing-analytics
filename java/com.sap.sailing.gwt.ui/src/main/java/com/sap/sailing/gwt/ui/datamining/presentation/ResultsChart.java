package com.sap.sailing.gwt.ui.datamining.presentation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.panels.ResizingSimplePanel;
import com.sap.sailing.gwt.ui.datamining.ResultsPresenter;
import com.sap.sse.datamining.shared.GroupKey;
import com.sap.sse.datamining.shared.QueryResult;

public class ResultsChart implements ResultsPresenter<Number> {

    private final StringMessages stringMessages;
    private final SimplePanel mainPanel;

    private final Chart chart;
    private Map<GroupKey, Series> series;

    private Map<GroupKey, Integer> mainKeyToValueMap;
    private Map<Integer, GroupKey> valueToGroupKeyMap;

    private final HTML errorLabel;

    private final HTML labeledBusyIndicator;

    public ResultsChart(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        mainPanel = new ResizingSimplePanel() {
            @Override
            public void onResize() {
                chart.setSizeToMatchContainer();
                chart.redraw();
            }
        };
        chart = createChart();
        series = new HashMap<GroupKey, Series>();
        
        errorLabel = new HTML();
        errorLabel.setStyleName("chart-importantMessage");
        
        labeledBusyIndicator = new HTML(stringMessages.runningQuery());
        labeledBusyIndicator.setStyleName("chart-busyMessage");

        showError(this.stringMessages.invalidSelection());
    }

    @Override
    public void showError(String error) {
        errorLabel.setHTML(error);
        mainPanel.setWidget(errorLabel);
    }
    
    @Override
    public void showError(String mainError, Iterable<String> detailedErrors) {
        StringBuilder errorBuilder = new StringBuilder(mainError + ":<br /><ul>");
        for (String detailedError : detailedErrors) {
            errorBuilder.append("<li>" + detailedError + "</li>");
        }
        errorBuilder.append("</ul>");
        showError(errorBuilder.toString());
    }
    
    @Override
    public void showBusyIndicator() {
        mainPanel.setWidget(labeledBusyIndicator);
    }

    @Override
    public void showResult(QueryResult<Number> result) {
        if (!result.isEmpty()) {
            resetChart();
            
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
        } else {
            showError(stringMessages.noDataFound() + ".");
        }
    }

    private void updateYAxisLabels(QueryResult<? extends Number> result) {
        chart.getYAxis().setAxisTitleText(result.getResultSignifier());
        chart.setToolTip(new ToolTip().setValueDecimals(result.getValueDecimals()).setValueSuffix(
                result.getUnitSignifier()));
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
        chart.setChartSubtitle(new ChartSubtitle().setText(stringMessages.queryResultsChartSubtitle(
                result.getRetrievedDataAmount(), result.getCalculationTimeInSeconds())));
        // This is needed, so that the subtitle is updated. Otherwise the text would stay empty
        mainPanel.setWidget(null);
        mainPanel.setWidget(chart);
    }

    public List<GroupKey> getSortedKeysFrom(QueryResult<? extends Number> result) {
        List<GroupKey> sortedKeys = new ArrayList<GroupKey>(result.getResults().keySet());
        Collections.sort(sortedKeys);
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

    private void resetChart() {
        chart.removeAllSeries(false);
        series = new HashMap<GroupKey, Series>();
    }

    private Chart createChart() {
        Chart chart = new Chart()
                .setType(Series.Type.COLUMN)
                .setMarginLeft(100)
                .setMarginRight(45)
                .setWidth100()
                .setHeight100()
                .setBorderColor(new Color("#F0AB00"))
                .setPlotBorderWidth(0)
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

        chart.getYAxis().setAxisTitleText("Result").setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
            @Override
            public String format(AxisLabelsData axisLabelsData) {
                try {
                    return axisLabelsData.getValueAsDouble() + "";
                } catch (Exception e) {
                    return "";
                }
            }
        }));
        
        return chart;
    }
    
    @Override
    public Widget getWidget() {
        return mainPanel;
    }

}
