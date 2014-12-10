package com.sap.sailing.gwt.ui.polarsheets;

import java.util.List;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.PlotLine;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;
import org.moxieapps.gwt.highcharts.client.plotOptions.Marker;
import org.moxieapps.gwt.highcharts.client.plotOptions.SplinePlotOptions;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.PolarSheetsXYDiagramData;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;

public class PolarSheetsXYDiagramPopupPanel extends DialogBox {


    private final Chart chart;
    private final StringMessages stringMessages;

    public PolarSheetsXYDiagramPopupPanel(StringMessages stringMessages, PolarSheetsXYDiagramData result) {
        this.stringMessages = stringMessages;
        chart = createChart();
        VerticalPanel containerPanel = new VerticalPanel();
        this.add(containerPanel);
        containerPanel.add(chart);
        
        
        Button closeButton = new Button(stringMessages.close());
        closeButton.addClickHandler(new ClickHandler() {
            
            @Override
            public void onClick(ClickEvent event) {
                PolarSheetsXYDiagramPopupPanel.this.hide(false);
            }
        });
        containerPanel.add(closeButton);
        
        Point[] pointsForUpwindStarboardAverageAngle = toPointArray(result.getPointsForUpwindStarboardAverageAngle());
        Series angleSeries = chart.createSeries();
        angleSeries.setPoints(pointsForUpwindStarboardAverageAngle);
        //chart.addSeries(angleSeries);
        
        Point[] pointsForUpwindStarboardAverageSpeed = toPointArray(result.getPointsForUpwindStarboardAverageSpeed());
        Series speedSeries = chart.createSeries();
        speedSeries.setName("Upwind Starboard Speed - LinearRegressions");
        speedSeries.setPoints(pointsForUpwindStarboardAverageSpeed);
        chart.addSeries(speedSeries);
        
        Point[] pointsForUpwindStarboardAverageSpeedMovingAverage = toPointArray(result.getPointsForUpwindStarboardAverageSpeedMovingAverage());
        Series speedSeriesMovingAverage = chart.createSeries();
        speedSeriesMovingAverage.setName("Upwind Starboard Speed - MovingAverage");
        speedSeriesMovingAverage.setPoints(pointsForUpwindStarboardAverageSpeedMovingAverage);
        chart.addSeries(speedSeriesMovingAverage);
        
        Point[] pointsForUpwindStarboardAverageConfidence = toPointArray(result.getPointsForUpwindStarboardAverageConfidence());
        Series confidenceSeriesMovingAverage = chart
                .createSeries()
                .setYAxis(1)
                .setType(Series.Type.SPLINE)
                .setPlotOptions(
                        new SplinePlotOptions().setColor("#AA4643").setMarker(new Marker().setEnabled(false))
                                .setDashStyle(PlotLine.DashStyle.SHORT_DOT));
        confidenceSeriesMovingAverage.setName("Upwind Starboard Confidence");
        confidenceSeriesMovingAverage.setPoints(pointsForUpwindStarboardAverageConfidence);
        chart.addSeries(confidenceSeriesMovingAverage);
    }
    
    private Point[] toPointArray(List<Pair<Double, Double>> pointsForUpwindStarboardAverageAngle) {
        Point[] points = new Point[pointsForUpwindStarboardAverageAngle.size()];
        int i = 0;
        for (Pair<Double, Double> point : pointsForUpwindStarboardAverageAngle) {
            points[i] = new Point(point.getA(), point.getB());
            i++;    
        }
        return points;
    }

    private Chart createChart() {
        Chart chart = new Chart().setType(Type.LINE);
        chart.setWidth(1000);
        chart.setTitle(stringMessages.xyDiagram());
        chart.getXAxis().setAxisTitle(new AxisTitle().setText(stringMessages.windSpeed()));
        chart.getYAxis(0).setAxisTitle(new AxisTitle().setText(stringMessages.boatSpeed()));
        chart.getYAxis(1).setAxisTitle(new AxisTitle().setText(stringMessages.confidence()));
        return chart;
    }

}
