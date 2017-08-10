package com.sap.sailing.gwt.ui.datamining.presentation;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.ChartSubtitle;
import org.moxieapps.gwt.highcharts.client.ChartTitle;
import org.moxieapps.gwt.highcharts.client.Exporting;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.LinePlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.sap.sailing.gwt.ui.client.StringMessages;

public class ChartFactory extends Chart {
    
    public static Chart createPolarChart(boolean setYMinValueZero) {
        LinePlotOptions linePlotOptions = new LinePlotOptions().setLineWidth(1).setMarker(new Marker().setEnabled(false));
        Chart polarSheetChart = new Chart().setType(Series.Type.LINE)
                .setLinePlotOptions(linePlotOptions)
                .setPolar(true).setHeight100().setWidth100();
        polarSheetChart.setTitle(new ChartTitle().setText(""), new ChartSubtitle().setText(""));
        if(setYMinValueZero) {
            polarSheetChart.getYAxis().setMin(0);
        }
        polarSheetChart.getXAxis().setMin(-179).setMax(180).setTickInterval(45);
        polarSheetChart.setOption("/pane/startAngle", 180);
        polarSheetChart.setExporting(new Exporting().setEnabled(false));
        return polarSheetChart;
    }
    
    public static Chart createDataCountHistogramChart(String xAxisLabel, StringMessages stringMessages) {
        Chart histogramChart = new Chart().setType(Type.COLUMN).setHeight100().setWidth100();
        histogramChart.setTitle(new ChartTitle().setText(""), new ChartSubtitle().setText(""));
        histogramChart.getYAxis().setMin(0).setAxisTitle(new AxisTitle().setText(stringMessages.numberOfDataPoints()));
        histogramChart.getXAxis().setLabels(new XAxisLabels().setRotation(-90f).setY(30).setEnabled(true))
                .setAxisTitle(new AxisTitle().setText(xAxisLabel));
        histogramChart.setLegend(new Legend().setEnabled(false));
        histogramChart.setExporting(new Exporting().setEnabled(false));
        return histogramChart;
    }
    
    public static Chart createSpeedChart(StringMessages stringMessages) {
        Chart speedChart = new Chart().setType(Type.LINE).setHeight100().setWidth100();
        speedChart.setTitle(new ChartTitle().setText(""), new ChartSubtitle().setText(""));
        speedChart.setExporting(new Exporting().setEnabled(false));
        speedChart.getYAxis().setExtremes(0, speedChart.getYAxis().getExtremes().getMax())
                .setAxisTitleText(stringMessages.boatSpeed() + " (" +  stringMessages.knotsUnit() + ")");
        speedChart.getXAxis().setAxisTitleText(stringMessages.windSpeed());
        return speedChart;
    }
    
    public static Chart createAngleChart(StringMessages stringMessages) {
        Chart angleChart = new Chart().setType(Type.LINE).setHeight100().setWidth100();
        angleChart.setTitle(new ChartTitle().setText(""), new ChartSubtitle().setText(""));
        angleChart.setExporting(new Exporting().setEnabled(false));
        angleChart.getYAxis().setAxisTitleText(stringMessages.beatAngle() + " (" + stringMessages.degreesShort() + ")");
        angleChart.getXAxis().setAxisTitleText(stringMessages.windSpeed());
        return angleChart;
    }
    
    public static Chart createLineChartForPolarData(StringMessages stringMessages) {
        Chart histogramChart = new Chart().setType(Type.LINE).setHeight100().setWidth100();
        histogramChart.setTitle(new ChartTitle().setText(""), new ChartSubtitle().setText(""));
        histogramChart.getYAxis();
        histogramChart.getXAxis().setLabels(new XAxisLabels().setRotation(-90f).setY(30).setEnabled(true))
                .setAxisTitle(new AxisTitle().setText(stringMessages.beatAngle() + " (" + stringMessages.degreesShort() + ")"));
        histogramChart.setLegend(new Legend().setEnabled(false));
        histogramChart.setExporting(new Exporting().setEnabled(false));
        return histogramChart;
    }

}
