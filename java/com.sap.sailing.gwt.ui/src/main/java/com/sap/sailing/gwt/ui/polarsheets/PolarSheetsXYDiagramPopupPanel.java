package com.sap.sailing.gwt.ui.polarsheets;

import java.util.List;

import org.moxieapps.gwt.highcharts.client.AxisTitle;
import org.moxieapps.gwt.highcharts.client.Chart;
import org.moxieapps.gwt.highcharts.client.Point;
import org.moxieapps.gwt.highcharts.client.Series;
import org.moxieapps.gwt.highcharts.client.Series.Type;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.PolarSheetsXYDiagramData;
import com.sap.sailing.domain.common.Tack;
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
        
        
        addSpeedAndConfidenceSeriesForTackAndLegType(result, Tack.STARBOARD, LegType.UPWIND, true);
        addSpeedAndConfidenceSeriesForTackAndLegType(result, Tack.PORT, LegType.UPWIND, false);
        addSpeedAndConfidenceSeriesForTackAndLegType(result, Tack.STARBOARD, LegType.DOWNWIND, false);
        addSpeedAndConfidenceSeriesForTackAndLegType(result, Tack.PORT, LegType.DOWNWIND, false);
        
        chart.redraw();
        
        
    }

    private void addSpeedAndConfidenceSeriesForTackAndLegType(PolarSheetsXYDiagramData result, Tack tack, LegType legType, boolean showByDefault) {
        Point[] pointsForUpwindStarboardAverageSpeedRegression = toPointArray(result.getPointsForAverageSpeedRegression(tack, legType));
        Series speedSeriesRegression = chart.createSeries();
        speedSeriesRegression.setName(tack + " " + legType + " Speed - Regression");
        speedSeriesRegression.setPoints(pointsForUpwindStarboardAverageSpeedRegression);
        chart.addSeries(speedSeriesRegression, false, false);
 
        if (!showByDefault) {
            speedSeriesRegression.setVisible(false, false);
        }
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
