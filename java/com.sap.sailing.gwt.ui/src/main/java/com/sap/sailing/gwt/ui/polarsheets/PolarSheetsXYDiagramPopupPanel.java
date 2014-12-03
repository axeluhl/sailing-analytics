package com.sap.sailing.gwt.ui.polarsheets;

import java.util.List;

import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.PolarSheetsXYDiagramData;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sse.common.Util.Pair;

public class PolarSheetsXYDiagramPopupPanel extends DialogBox {


    private final Chart chart;

    public PolarSheetsXYDiagramPopupPanel(StringMessages stringMessages, PolarSheetsXYDiagramData result) {
        chart = createChart();
        VerticalPanel containerPanel = new VerticalPanel();
        containerPanel.setWidth("100%");
        containerPanel.setHeight("100%");
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
        
        this.setWidth("1200px");
        this.setHeight("800px");
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
        chart.setHeight("80%");
        return chart;
    }
    
    @Override
    protected void onLoad() {
        Timer timer = new Timer() {
            
            @Override
            public void run() {
                chart.setSizeToMatchContainer();
            }
        };
        timer.schedule(200);
        super.onLoad();
    }

}
