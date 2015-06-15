package com.sap.sailing.dashboards.gwt.client.startlineadvantage;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.moxieapps.gwt.highcharts.client.Animation;
import org.moxieapps.gwt.highcharts.client.Animation.Easing;
import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
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
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import com.sap.sailing.dashboards.gwt.client.RibDashboardServiceAsync;
import com.sap.sailing.dashboards.gwt.client.actions.GetStartlineAdvantagesAction;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;
import com.sap.sse.gwt.client.async.AsyncActionsExecutor;
import com.sap.sse.gwt.client.player.TimeListener;
import com.sap.sse.gwt.client.player.Timer;
import com.sap.sse.gwt.client.player.Timer.PlayModes;

/**
 * @author Alexander Ries (D062114)
 *
 */
public class StartlineAdvantagesOnLineChart extends Composite implements HasWidgets, TimeListener {

    private static StartlineAdvantagesOnLineChartUiBinder uiBinder = GWT.create(StartlineAdvantagesOnLineChartUiBinder.class);

    interface StartlineAdvantagesOnLineChartUiBinder extends UiBinder<Widget, StartlineAdvantagesOnLineChart> {
    }
    
    interface StartlineAdvantagesOnLineChartStyle extends CssResource {
    }

    @UiField
    HTMLPanel chartContainer;

    private RibDashboardServiceAsync ribDashboardService;
    private AsyncActionsExecutor asyncActionsExecutor;
    private String leaderboardName;
    private Chart chart;
    private Series series;
    
    private static final String PARAM_LEADERBOARD_NAME = "leaderboardName";
    
    public StartlineAdvantagesOnLineChart(RibDashboardServiceAsync ribDashboardService) {
        this.ribDashboardService = ribDashboardService;
        this.asyncActionsExecutor = new AsyncActionsExecutor();
        this.leaderboardName = Window.Location.getParameter(PARAM_LEADERBOARD_NAME);
        initWidget(uiBinder.createAndBindUi(this));
        initAndAddChart();
        
        //TODO Remove and centralize data retrieving
        initSampleTimer();
    }
    
    private void initSampleTimer(){
        Timer timer = new Timer(PlayModes.Live);
        timer.setRefreshInterval(3000);
        timer.addTimeListener(this);
        timer.play();
    }
    
    private void initAndAddChart() {  
        chart = initChart();
        setCorrectChartSpacing(chart);
        setXAxisOfChart(chart);
        setYAxisOfChart(chart);
        series = createSeries(chart);
        chart.addSeries(createSeries(chart));
        chartContainer.add(chart);
    }
    
    private Chart initChart(){
        Chart chart = new Chart()  
        .setType(Series.Type.AREA) 
        .setChartTitleText(null)
        .setLegend(new Legend().setEnabled(false))
        .setToolTip(new ToolTip().setEnabled(false))
        .setCredits(new Credits().setEnabled(false))
        .setStyle(new Style().setPosition("absolute").setTop("0px").setBottom("0px").setLeft("0px").setRight("0px"))
        .setAnimation(true);
        chart.setSize("100%", "100%");
        chart.setSizeToMatchContainer();
        return chart;
    }
    
    /**
     * Highcharts default chart element spacing is not consistent
     * so there is a need to correct the right spacing 
     * of the chart to make it look visually right.
     * */
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
        .setTickWidth(1)
        .setReversed(true);
    }
    
    private void setYAxisOfChart(Chart chart){
        chart.getYAxis()  
        .setAxisTitle(new AxisTitle().setText(StringMessages.INSTANCE.dashboardAdvantageInSeconds()).setStyle(new Style().setFontFamily("Open Sans")
                    .setFontSize("14")
                    .setColor("black").setFontWeight("bold")))
        .setLabels(new YAxisLabels()
                   .setFormatter(new AxisLabelsFormatter() {  
                       public String format(AxisLabelsData axisLabelsData) {  
                        return String.valueOf(axisLabelsData.getValueAsLong());
                    }  
                    }  
                    ).setStyle(new Style().setFontFamily("Open Sans").setFontSize("12")
                            .setColor("grey"))).setLineColor("grey").setTickWidth(1).setTickColor("grey").setLineWidth(1).setGridLineWidth(0);          
    }

    private Series createSeries(Chart chart){
        Series series = chart
                .createSeries()
                .setName(null)
                .setPlotOptions(
                        new AreaPlotOptions().setDashStyle(DashStyle.SOLID).setLineWidth(1)
                                .setMarker(new Marker().setEnabled(false)).setShadow(false).setHoverStateEnabled(false)
                                .setLineColor("#008FFF").setFillColor("#008FFF"));
        return series;
    }
    
    private String getAxisLabelValueForLabelData(double labelData) {
        String result = String.valueOf(labelData);
        com.sap.sse.common.Util.Pair<Double, Double> firstAndLastXValue = getFirstnAndLastXValueOfSeries();
        if (firstAndLastXValue != null && firstAndLastXValue.getA() != null && firstAndLastXValue.getB() != null) {
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
    
    /**
     * Removed all Points and adds new ones
     * */
    private void updateSeriesPoints(List<Pair<Double, Double>> xYPairs) {
        if (series.getPoints() != null && series.getPoints().length  > 0) {
            updateExistingPointsOfSeriesWithXYPairs(xYPairs, series);
        } else {
            addXYPairsToSeries(xYPairs, series);
            chart.addSeries(series , true, new Animation().setDuration(20000).setEasing(Easing.SWING));
        }
        chart.setSizeToMatchContainer();
    }
    
    private void updateExistingPointsOfSeriesWithXYPairs(List<Pair<Double, Double>> xYPairs, Series series){
        int counter = 0;
        if (series != null) {
            for (Point point : series.getPoints()) {
                Pair<Double, Double> xYPair = xYPairs.get(counter);
                point.update(xYPair.getA(), xYPair.getB(), false);
                counter++;
            }
        }
        chart.redraw();
    }
    
    private void addXYPairsToSeries(List<Pair<Double, Double>> xYPairs, Series series){
        for (Pair<Double, Double> xYPair : xYPairs) {
            series.addPoint(new Point(xYPair.getA(), xYPair.getB()), true, false, new Animation().setDuration(2000));
        }
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
    
    private void loadData() {
        GetStartlineAdvantagesAction getRibDashboardRaceInfoAction = new GetStartlineAdvantagesAction(
                ribDashboardService, leaderboardName);
        asyncActionsExecutor.execute(getRibDashboardRaceInfoAction, new AsyncCallback<List<Pair<Double, Double>>>() {

            @Override
            public void onSuccess(List<Pair<Double, Double>> result) {
                updateSeriesPoints(result);
            }

            @Override
            public void onFailure(Throwable caught) {
            }
        });
    }

    @Override
    public void timeChanged(Date newTime, Date oldTime) {
        loadData();
    }
}
