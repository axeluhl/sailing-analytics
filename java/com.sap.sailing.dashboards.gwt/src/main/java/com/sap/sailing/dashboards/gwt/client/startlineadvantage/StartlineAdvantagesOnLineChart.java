package com.sap.sailing.dashboards.gwt.client.startlineadvantage;

import java.util.Iterator;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.Animation;
import org.moxieapps.gwt.highcharts.client.Animation.Easing;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.PlotLine.DashStyle;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Style;
import org.moxieapps.gwt.highcharts.client.ToolTip;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsData;
import org.moxieapps.gwt.highcharts.client.labels.AxisLabelsFormatter;
import org.moxieapps.gwt.highcharts.client.labels.XAxisLabels;
import org.moxieapps.gwt.highcharts.client.labels.YAxisLabels;
import org.moxieapps.gwt.highcharts.client.plotOptions.AreaPlotOptions;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.shared.dto.StartLineAdvantageDTO;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantagesOnLineChart extends Composite implements HasWidgets {

    private static StartlineAdvantagesOnLineChartUiBinder uiBinder = GWT.create(StartlineAdvantagesOnLineChartUiBinder.class);

    interface StartlineAdvantagesOnLineChartUiBinder extends UiBinder<Widget, StartlineAdvantagesOnLineChart> {
    }
    
    interface StartlineAdvantagesOnLineChartStyle extends CssResource {
    }

    @UiField
    HTMLPanel chartContainer;
    
    @UiField
    DivElement confidenceBarHeader;
    
    @UiField
    DivElement confidenceBarMinLabel;
    
    @UiField
    DivElement confidenceBarMaxLabel;

    private Chart chart;
    private Series series;
    
    private static final int POINT_ADDING_ANIMATION_DURATION_IN_MILLIS = 2000;
    
    public StartlineAdvantagesOnLineChart() {
        initWidget(uiBinder.createAndBindUi(this));
        initAndAddChart();
        setConfidenceColorBarLabelValues();
        //TODO Remove and centralize data retrieving
    }
    
    private void initAndAddChart() {  
        chart = initChart();
        /**
         * Highcharts default chart element spacing is not consistent
         * so there is a need to correct the right spacing 
         * of the chart to make it look visually right.
         * */
        setCorrectChartSpacing(chart);
        setXAxisOfChart(chart);
        setYAxisOfChart(chart);
        series = createSeries(chart);
        chartContainer.add(chart);
    }
    
    private Chart initChart() {
        Chart chart = new Chart()  
        .setType(Series.Type.AREA) 
        .setChartTitleText(null)
        .setLegend(new Legend().setEnabled(false))
        .setToolTip(new ToolTip().setEnabled(false))
        .setCredits(new Credits().setEnabled(false))
        .setBackgroundColor("#FFFFFF")
        .setBorderColor("#FFFFFF")
        .setPlotShadow(false)
        .setPlotBorderColor("#FFFFFF")
        .setOption("/exporting/enabled", false)
        .setShadow(false)
        .setStyle(new Style().setPosition("absolute").setTop("0px").setBottom("0px").setLeft("0px").setRight("0px"))
        .setAnimation(true);
        chart.setSize("100%", "100%");
        chart.setSizeToMatchContainer();
        return chart;
    }
    
    private void setCorrectChartSpacing(Chart chart){
        chart.setSpacingTop(17)
        .setSpacingBottom(13)
        .setSpacingRight(35);
    }
    
    private void setXAxisOfChart(Chart chart){
        chart.getXAxis()
        .setAxisTitle(new AxisTitle().setText(StringMessages.INSTANCE.dashboardDistanceToRCBoat()).setStyle(new Style().setFontFamily("Open Sans")
                    .setFontSize("14")
                    .setColor("black").setFontWeight("bold")))
        .setLabels(new XAxisLabels()
                   .setFormatter(new AxisLabelsFormatter() {  
                    public String format(AxisLabelsData axisLabelsData) {
                        return getAxisLabelValueForLabelData(axisLabelsData.getValueAsLong());
                    }  
                }  
          ).setStyle(new Style().setFontFamily("Open Sans")
                    .setFontSize("12")
                    .setColor("grey")))
                    .setLineWidth(1)
        .setLineColor("grey")
        .setTickColor("grey")
        .setTickInterval(50)
        .setGridLineColor("white")
        .setTickWidth(1)
        .setGridLineWidth(0)
        .setReversed(true);
    }
    
    private void setYAxisOfChart(Chart chart) {
        chart.getYAxis()
                .setAxisTitle(
                        new AxisTitle().setText(StringMessages.INSTANCE.dashboardAdvantageInSeconds()).setStyle(
                                new Style().setFontFamily("Open Sans").setFontSize("14").setColor("black")
                                        .setFontWeight("bold")))
                .setLabels(new YAxisLabels().setFormatter(new AxisLabelsFormatter() {
                    public String format(AxisLabelsData axisLabelsData) {
                        return String.valueOf(axisLabelsData.getValueAsLong());
                    }
                }).setStyle(new Style().setFontFamily("Open Sans").setFontSize("12").setColor("grey")))
                .setLineColor("grey").setGridLineWidth(0).setMinorGridLineWidth(0).setGridLineColor("white")
                .setTickWidth(1).setTickColor("grey").setLineWidth(1).setGridLineWidth(0).setExtremes(0, 150).setStartOnTick(true).setEndOnTick(true);
    }

    private Series createSeries(Chart chart){
        Series series = chart
                .createSeries()
                .setName(null)
                .setPlotOptions(
                        new AreaPlotOptions().setDashStyle(DashStyle.SOLID).setLineWidth(1)
                                .setMarker(new Marker().setEnabled(false)).setShadow(false).setHoverStateEnabled(false)
                                .setLineColor("#FFFFFF").setFillColor("#FFFFFF"));
        return series;
    }
    
    private void setConfidenceColorBarLabelValues() {
        confidenceBarHeader.setInnerText("CONFIDENCE");
        confidenceBarMaxLabel.setInnerText("High");
        confidenceBarMinLabel.setInnerText("Low");
    }
    
    private String getAxisLabelValueForLabelData(double labelData) {
        String result = String.valueOf(labelData);
        com.sap.sse.common.Util.Pair<Double, Double> firstAndLastXValue = getFirstnAndLastXValueOfSeries();
        if (firstAndLastXValue != null && firstAndLastXValue.getA() != null && firstAndLastXValue.getB() != null) {
            chart.getXAxis().setTickInterval(firstAndLastXValue.getB()/2);
            if (labelData == firstAndLastXValue.getA().doubleValue()) {
                result = StringMessages.INSTANCE.dashboardRCBoat();
            } else if (labelData == firstAndLastXValue.getB().doubleValue()) {
                result = StringMessages.INSTANCE.dashboardPinEnd();
            }
        }
        return result;
    }
    
    private com.sap.sse.common.Util.Pair<Double, Double> getFirstnAndLastXValueOfSeries(){
        if(series != null &&
           series.getPoints() != null &&
           series.getPoints().length > 1){
            Double firstValue = new Double(series.getPoints()[0].getX().doubleValue());
            Double lastValue = new Double(series.getPoints()[series.getPoints().length-1].getX().doubleValue());
            com.sap.sse.common.Util.Pair<Double, Double> firstAndLastXValue = new Pair<Double, Double>(firstValue, lastValue);
            return firstAndLastXValue;
        }else{
            return null;
        }
    }
    
    public void setStartlineAdvantages(List<StartLineAdvantageDTO> startlineAdvantages) {
        if (startlineAdvantages != null) {
            if (series.getPoints() != null && series.getPoints().length > 0) {
                updateStartlineAdvantages(startlineAdvantages, series);
            } else {
                addStartlineAdvantages(startlineAdvantages, series);
                chart.addSeries(series, true, new Animation().setDuration(POINT_ADDING_ANIMATION_DURATION_IN_MILLIS).setEasing(Easing.SWING));
            }
        }
        chart.setSizeToMatchContainer();
    }
    
    private void updateStartlineAdvantages(List<StartLineAdvantageDTO> startlineAdvantages, Series series) {
        removeAllSeriesPoints(chart.getSeries()[0]);
        addStartlineAdvantages(startlineAdvantages, series);
    }
    
    private void removeAllSeriesPoints(Series series) {
        for (Point point : series.getPoints()) {
            series.removePoint(point);
        }
    }
    
    private void addStartlineAdvantages(List<StartLineAdvantageDTO> startlineAdvantages, Series series){
        //TODO Build this with forEach consumer as soon as the bundle uses GWT 2.8 with Java 8 Support.
        for (StartLineAdvantageDTO startlineAdvantage : startlineAdvantages) {
            Point point = new Point(startlineAdvantage.distanceToRCBoatInMeters, startlineAdvantage.startLineAdvantage);
            series.addPoint(point, false, false, false);
        }
        setConfidenceGradientColorToSeries(series, startlineAdvantages);
        chart.redraw();
    }
    
    private void setConfidenceGradientColorToSeries(Series series, List<StartLineAdvantageDTO> startlineAdvantages) {
        Color confidenceGradientColor = ConfidenceColorCalculator.getColorWithConfidenceGradients(startlineAdvantages);
        series.setPlotOptions(
                new AreaPlotOptions().setDashStyle(DashStyle.SOLID).setLineWidth(1)
                .setMarker(new Marker().setEnabled(false)).setShadow(false).setHoverStateEnabled(false)
                .setLineColor("#FFFFFF").setFillColor(confidenceGradientColor));
    }
    
    @Override
    public void add(Widget w) {
        throw new UnsupportedOperationException("The method add(Widget w) is not supported.");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("The method clear() is not supported.");
    }

    @Override
    public Iterator<Widget> iterator() {
        return null;
    }

    @Override
    public boolean remove(Widget w) {
        return false;
    }
    
    private static class ConfidenceColorCalculator {
        private static final String LOW_CONFIDENCE_COLOR_AS_HEX = "#43cea2";
        private static final String HIGHT_CONFIDENCE_COLOR_AS_HEX = "#008FFF";
        
        public static Color getColorWithConfidenceGradients(List<StartLineAdvantageDTO> startlineAdvantages) {
            Color color = new Color().setLinearGradient(200, 0, 0, 0);
            double lineLenght = getBiggestDistanceToRCBoat(startlineAdvantages);
            //TODO Need to find a prettier way of at adding color stops
            double lastConfidence = 2;
            for (StartLineAdvantageDTO startlineAdvantageDTO : startlineAdvantages) {
                if (lastConfidence != startlineAdvantageDTO.confidence) {
                    lastConfidence = startlineAdvantageDTO.confidence;
                    double gradientPosition = getGradientStopPositionFromStartlineLenghtAndDistanceToRCBoat(lineLenght,
                            startlineAdvantageDTO.distanceToRCBoatInMeters);
                    color.addColorStop(gradientPosition, getColorAsHexStringFromConfidence(startlineAdvantageDTO.confidence));
                }
            }
            return color;
        }
        
        private static double getGradientStopPositionFromStartlineLenghtAndDistanceToRCBoat(double startlineLenght, double distanceToRCBoat) {
            return distanceToRCBoat/startlineLenght;
        }
        
        private static String getColorAsHexStringFromConfidence(double position) {
            if(position > 0.6) {
                return HIGHT_CONFIDENCE_COLOR_AS_HEX;
            }else {
                return LOW_CONFIDENCE_COLOR_AS_HEX;
            }
        }
        
        private static double getBiggestDistanceToRCBoat(List<StartLineAdvantageDTO> startlineAdvantages) {
            StartLineAdvantageDTO startlineAdvantageDTO = startlineAdvantages.get(startlineAdvantages.size()-1);
            return startlineAdvantageDTO.distanceToRCBoatInMeters;
        }
    }
}
