package com.sap.sailing.dashboards.gwt.client.startlineadvantage;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Color;
import org.moxieapps.gwt.highcharts.client.Credits;
import org.moxieapps.gwt.highcharts.client.Legend;
import org.moxieapps.gwt.highcharts.client.PlotLine.DashStyle;
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
import com.sap.sailing.dashboards.gwt.client.util.HighchartsUtil;
import com.sap.sailing.gwt.ui.client.StringMessages;

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
    private static final Logger logger = Logger.getLogger(StartlineAdvantagesByWindComponent.class.getName());
    
    public StartlineAdvantagesOnLineChart() {
        initWidget(uiBinder.createAndBindUi(this));
        initAndAddChart();
        setConfidenceColorBarLabelValues();
    }
    
    public void setStartlineAdvantagesAndConfidences(Number [][] distanceToRCBoatToStartlineAdvantages, Number [][] distanceToRCBoatToConfidences) {
        if (distanceToRCBoatToStartlineAdvantages != null) {
            series.setPoints(distanceToRCBoatToStartlineAdvantages, true);
        }
        setConfidenceGradientColorToSeries(series, distanceToRCBoatToConfidences);
        chart.redraw();
    }
    
    private void initAndAddChart() {  
        chart = initChart();
        HighchartsUtil.correctSpacingOfChart(chart);
        setXAxisOfChart(chart);
        setYAxisOfChart(chart);
        series = createSeries(chart);
        chart.addSeries(series);
        chartContainer.add(chart);
        HighchartsUtil.setSizeToMatchContainerDelayed(chart);
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
        .setWidth100()
        .setHeight100()
        .setPlotBorderColor("#FFFFFF")
        .setOption("/exporting/enabled", false)
        .setShadow(false)
        .setStyle(new Style().setPosition("absolute").setTop("0px").setBottom("0px").setLeft("0px").setRight("0px"))
        .setAnimation(true);
        return chart;
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
                .setPlotOptions(new AreaPlotOptions().setDashStyle(DashStyle.SOLID).setLineWidth(1)
                                .setMarker(new Marker().setEnabled(false)).setShadow(false).setHoverStateEnabled(false)
                                .setLineColor("white").setFillColor("white"));
        return series;
    }
    
    private void setConfidenceColorBarLabelValues() {
        confidenceBarHeader.setInnerText("CONFIDENCE");
        confidenceBarMaxLabel.setInnerText("High");
        confidenceBarMinLabel.setInnerText("Low");
    }
    
    private String getAxisLabelValueForLabelData(double labelData) {
        if(labelData != 0) {
            return labelData+"";
        }else {
            return "RC Boat";
        }
    }
    
    private void setConfidenceGradientColorToSeries(Series series, Number [][] distanceToRCBoatToConfidences) {
        Color confidenceGradientColor = ConfidenceColorCalculator.getColorWithConfidenceGradients(distanceToRCBoatToConfidences);
        String fillColor = confidenceGradientColor.getOptions().toString();
        logger.log(Level.INFO, fillColor);
        series.updateSeriesFillColor(fillColor);
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
        
        public static Color getColorWithConfidenceGradients(Number [][] distanceToRCBoatToConfidences) {
            Color color = new Color().setLinearGradient(200, 0, 0, 0);
            int previousIndex  = 0;
            for (int i = 0; i < distanceToRCBoatToConfidences.length; i++) {
                double previousConfidence = distanceToRCBoatToConfidences[previousIndex][1].doubleValue();
                double currentConfidence = distanceToRCBoatToConfidences[i][1].doubleValue();
                if (previousConfidence != currentConfidence || i == 0) {
                    logger.log(Level.INFO, previousConfidence+" "+currentConfidence);
                    color.addColorStop(i, getColorAsHexStringFromConfidence(distanceToRCBoatToConfidences[i][0].doubleValue()));
                }
                previousIndex++;
            }
            return color;
        }
        
        private static String getColorAsHexStringFromConfidence(double position) {
            if(position > 0.6) {
                return HIGHT_CONFIDENCE_COLOR_AS_HEX;
            }else {
                return LOW_CONFIDENCE_COLOR_AS_HEX;
            }
        }
    }
}
