package com.sap.sailing.gwt.ui.datamining;

import java.util.Map.Entry;

import org.moxieapps.gwt.highcharts.client.Axis;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;

import com.google.gwt.user.client.ui.SimplePanel;
import com.sap.sailing.datamining.shared.QueryResult;
import com.sap.sailing.gwt.ui.client.StringMessages;

public class QueryResultsChart extends SimplePanel {

    private StringMessages stringMessages;
    
    private Chart chart;
    private Series resultSeries;

    public QueryResultsChart(StringMessages stringMessages) {
        super();
        this.stringMessages = stringMessages;
        
        createChart();
        setWidget(chart);
    }

    public void showResult(QueryResult result) {
        reset();
        for (Entry<String, Double> resultEntry : result.getResults().entrySet()) {
            Point point = new Point(resultEntry.getKey(), resultEntry.getValue());
            resultSeries.addPoint(point);
        }
        ensureChartContainsSeries();
    }
    
    private void ensureChartContainsSeries() {
        for (Series containedSeries : chart.getSeries()) {
            if (resultSeries.equals(containedSeries)) {
                return;
            }
        }
        
        chart.addSeries(resultSeries);
    }
    
    private void reset() {
        resultSeries.setPoints(new Point[0]);
    }

    private void createChart() {
        chart = new Chart().setType(Series.Type.COLUMN)
                .setMarginLeft(100)
                .setMarginRight(45)
                .setWidth100()
                .setCredits(new Credits().setEnabled(false))
                .setChartTitle(new ChartTitle().setText(stringMessages.dataMiningResult()));

        chart.getXAxis().setType(Axis.Type.LINEAR).setAllowDecimals(false)
                .setLabels(new XAxisLabels().setFormatter(new AxisLabelsFormatter() {
                    @Override
                    public String format(AxisLabelsData axisLabelsData) {
                        return "X";
                    }
                }));

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
        
        resultSeries = chart.createSeries().setName("Result");

//        chart.setToolTip(new ToolTip()
//                .setPointFormat("<span style=\"color:{series.color}\">{series.name}</span>: <b>{point.y}s</b><br/>"));
    }

}
