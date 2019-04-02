package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.TextMetrics;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.controls.ControlPosition;
import com.sap.sailing.gwt.ui.client.StringMessages;
import com.sap.sailing.gwt.ui.client.shared.racemap.CoordinateSystem;
import com.sap.sailing.gwt.ui.shared.PathDTO;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;

public class DetailTypeMetricOverlay extends FullCanvasOverlay {
    
    private final String textColor = "Black";
    private final String textFont = "10pt 'Open Sans'";
    private int xOffset = 0;
    private int yOffset = 0; //150;
    private double rectWidth = 20;
    private double rectHeight = 20;
    private final StringMessages stringMessages;
    private Canvas detailLegend;

    public DetailTypeMetricOverlay(MapWidget map, int zIndex, CoordinateSystem coordinateSystem, StringMessages stringMessages) {
        super(map, zIndex, coordinateSystem);
        this.stringMessages = stringMessages;
    }
    
    protected void createDetailLegend(MapWidget map) {
        detailLegend = Canvas.createIfSupported();
        detailLegend.setStyleName("MapSimulationLegend");
        detailLegend.setTitle("Metric Legend");
        map.setControls(ControlPosition.TOP_LEFT, detailLegend);
        detailLegend.getParent().setStyleName("MapSimulationLegendParentDiv");
    }
    
    public void clearCanvas() {
        if (detailLegend != null) {
            Context2d g = this.getCanvas().getContext2d();
            double w = detailLegend.getOffsetWidth();
            double h = detailLegend.getOffsetHeight();
            g = detailLegend.getContext2d();
            g.clearRect(0, 0, w, h);            
        }
    }
    
    public void drawLegend(Canvas canvas) {
        int index = 0;
        Context2d context2d = canvas.getContext2d();
        context2d.setFont(textFont);
        TextMetrics txtmet;
        txtmet = context2d.measureText("00:00:00");
        double timewidth = txtmet.getWidth();
        double txtmaxwidth = 0.0;
        boolean containsTimeOut = false;
        boolean containsMixedLeg = false;
        double newwidth = 0;
        double deltaTime = 0;
        double deltaMixedLeg = 0;
        double deltaTimeOut = 0;
        double mixedLegWidth = 0;
        //canvas.setSize(xOffset + rectWidth + txtmaxwidth + timewidth + 10.0+"px", rectHeight*(paths.length+1)+"px");
        int canvasWidth = (int)Math.ceil(xOffset + rectWidth + txtmaxwidth + timewidth + 20.0);
        int canvasHeight = (int)Math.ceil(yOffset + rectHeight + 20.0);
        setCanvasSize(canvas, canvasWidth, canvasHeight);
        /*drawRectangleWithText(context2d, xOffset, yOffset, null, stringMessages.raceLeader(),
            getFormattedTime(racePath.getPathTime()), txtmaxwidth, timewidth, deltaTime, true);*/
    }
    
    protected void setCanvasSize(Canvas canvas, int canvasWidth, int canvasHeight) {
        canvas.setWidth(String.valueOf(canvasWidth));
        canvas.setHeight(String.valueOf(canvasHeight));
        canvas.setCoordinateSpaceWidth(canvasWidth);
        canvas.setCoordinateSpaceHeight(canvasHeight);
    }
    
    protected void drawRectangle(Context2d context2d, double x, double y, String color) {
        context2d.setFillStyle(color);
        context2d.setLineWidth(3);
        context2d.fillRect(x, y, rectWidth, rectHeight);
    }

    protected void drawRectangleWithText(Context2d context2d, double x, double y, String color, String text, String time, double textmaxwidth, double timewidth, double xdelta, boolean visible) {
        double offset = 3.0;
        double crossOffset = 5.0;
        context2d.setFont(textFont);
        if (color != null) {
            drawRectangle(context2d, x, y, color);
        }
        if ((visible) && (color != null)) {
            context2d.setGlobalAlpha(1.0);
            context2d.setLineWidth(3.0);
            context2d.setStrokeStyle("white");
            context2d.beginPath();
            context2d.moveTo(x + crossOffset,y + crossOffset);
            context2d.lineTo(x + rectWidth - crossOffset, y + rectHeight - crossOffset);
            context2d.stroke();
            context2d.beginPath();
            context2d.moveTo(x + crossOffset, y + rectHeight - crossOffset);
            context2d.lineTo(x + rectWidth - crossOffset,y + crossOffset);
            context2d.stroke();
            context2d.setStrokeStyle("black");            
        }
        context2d.setGlobalAlpha(0.80);
        context2d.setFillStyle("white");
        context2d.fillRect(x + (color==null?0:rectWidth), y, 20.0 + textmaxwidth + timewidth + (color==null?rectWidth:0), rectHeight);
        context2d.setGlobalAlpha(1.0);
        context2d.setFillStyle(textColor);
        context2d.fillText(text, x + rectWidth + 5.0, y + 12.0 + offset);
        context2d.fillText(time, x + rectWidth + textmaxwidth + xdelta + 15.0, y + 12.0 + offset);
    }
}
