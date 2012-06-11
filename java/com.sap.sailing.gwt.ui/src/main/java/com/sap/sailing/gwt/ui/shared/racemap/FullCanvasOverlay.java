package com.sap.sailing.gwt.ui.shared.racemap;

import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.geom.Point;

/**
 * This class extends @CanvasOverlay to provide the functionality that the canvas always covers the
 * full viewable area of the map
 * 
 * @author Nidhi Sawhney(D054070)
 *
 */
public abstract class FullCanvasOverlay extends CanvasOverlay {

    /* x coordinate where the widget is placed */
    private int widgetPosLeft = 0;
    /* y coordinate where the widget is placed */
    private int widgetPosTop = 0;

    public String pointColor = "Red";
    
    public String textColor = "Black";
    
    protected static Logger logger = Logger.getLogger("com.sap.sailing");
    /* Set the canvas to be the size of the map and set it to the top left corner of the map */
    protected void setCanvasSettings() {
        int canvasWidth = getMap().getSize().getWidth();
        int canvasHeight = getMap().getSize().getHeight();
   
        canvas.setWidth(String.valueOf(canvasWidth));
        canvas.setHeight(String.valueOf(canvasHeight));
        canvas.setCoordinateSpaceWidth(canvasWidth);
        canvas.setCoordinateSpaceHeight(canvasHeight);
   
        Point sw = getMap().convertLatLngToDivPixel(getMap().getBounds().getSouthWest());
        Point ne = getMap().convertLatLngToDivPixel(getMap().getBounds().getNorthEast());
        setWidgetPosLeft(Math.min(sw.getX(), ne.getX()));
        setWidgetPosTop(Math.min(sw.getY(), ne.getY()));
          
        getPane().setWidgetPosition(getCanvas(), getWidgetPosLeft(), getWidgetPosTop());
        
    }
    
    @Override
    protected void redraw(boolean force) {
        logger.info("In FullCanvasOverlay.redraw" + force);
        /*
         * Reset the canvas only if the pixel coordinates need to be recomputed
         */
        //if (force) {
            setCanvasSettings();
        //}
    }
    
    /**
     * Draw a point on the canvas
     * @param x coordinate wrt the canvas
     * @param y coordinate wrt the canvas
     */
    protected void drawPoint(double x, double y) {
        Context2d context2d = canvas.getContext2d();
        context2d.setStrokeStyle(pointColor);
        context2d.setLineWidth(3);
        context2d.beginPath();
        context2d.moveTo(x-1, y-1);
        context2d.lineTo(x+1, y+1);
        context2d.closePath();
        context2d.stroke();  
        
    }
    
    /**
     * Draw a point and a text on the canvas
     * @param x coordinate wrt the canvas
     * @param y coordinate wrt the canvas
     * @param text to be displayed at the point on the canvas
     */
    protected void drawPointWithText(double x, double y, String text) {
        
        Context2d context2d = canvas.getContext2d();
        drawPoint(x, y);
        context2d.setFillStyle(textColor);
        context2d.fillText(text, x, y);
       
    }
    
    /**
     * Draw a circle centred at x,y with given radius
     * @param x
     * @param y
     * @param radius
     */
    protected void drawCircle(double x, double y, double radius, String color) {
        Context2d context2d = canvas.getContext2d();
        context2d.setLineWidth(3);
        context2d.setStrokeStyle(color);
        context2d.beginPath();
        context2d.arc(x,y,radius,0,2*Math.PI);
        context2d.closePath();
        context2d.stroke();
    }
        
    /**
     * Draw a circle centred at x,y with given radius
     * @param x
     * @param y
     * @param text
     */
    protected void drawCircleWithText(double x, double y, double radius, String color, String text) {
        
        Context2d context2d = canvas.getContext2d();
        drawCircle(x, y,radius,color);
        context2d.setFillStyle(textColor);
        context2d.setFont("normal 12pt Calibri");
        context2d.fillText(text, x + 10., y + 4.);
       
    }
    
    /**
     * Draw a line from (x,y) to (x1,y1) on the canvas
     * @param x
     * @param y
     * @param x1
     * @param y1
     */
    protected void drawLine(double x, double y, double x1, double y1, double weight, String color) {
        Context2d context2d  = canvas.getContext2d();
        context2d.setLineWidth(weight);
        context2d.setStrokeStyle(color);
        context2d.beginPath();
        context2d.moveTo(x, y);
        context2d.lineTo(x1, y1);
        context2d.closePath();
        context2d.stroke();
    }
    
    /**
     * Debug function to draw the boundaries of the canvas
     */
    protected void drawCanvas() {
       
        Context2d context2d  = canvas.getContext2d();
        context2d.setStrokeStyle("Black");
        context2d.setLineWidth(3);
       
        context2d.beginPath();
        context2d.moveTo(0,0);
        context2d.lineTo(0, canvas.getCoordinateSpaceHeight());
        context2d.closePath();
        context2d.stroke();
        
        context2d.beginPath();
        context2d.moveTo(0,0);
        context2d.lineTo(canvas.getCoordinateSpaceWidth(),0);
        context2d.closePath();
        context2d.stroke();
        
        context2d.beginPath();   
        context2d.moveTo(canvas.getCoordinateSpaceWidth(),0);
        context2d.lineTo(canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
        context2d.closePath();
        context2d.stroke();
        
        context2d.beginPath();   
        context2d.moveTo(0, canvas.getCoordinateSpaceHeight());
        context2d.lineTo(canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
        context2d.closePath();
        context2d.stroke();
        
    }

    public int getWidgetPosLeft() {
        return widgetPosLeft;
    }

    public void setWidgetPosLeft(int widgetPosLeft) {
        this.widgetPosLeft = widgetPosLeft;
    }

    public int getWidgetPosTop() {
        return widgetPosTop;
    }

    public void setWidgetPosTop(int widgetPosTop) {
        this.widgetPosTop = widgetPosTop;
    }
}
