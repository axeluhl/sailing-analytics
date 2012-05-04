package com.sap.sailing.gwt.ui.simulator;

import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.MapDoubleClickHandler;
import com.google.gwt.maps.client.event.MapMouseMoveHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlay;

public class RaceCourseCanvasOverlay extends CanvasOverlay {

    public LatLng startPoint = null;
    public LatLng endPoint = null;
    private  Marker startMarker = null;
    private Marker endMarker = null;
    private int widgetPosLeft = 0;
    private int widgetPosTop  = 0;
    private RaceCourseMapMouseMoveHandler raceCourseMapMouseMoveHandler = new RaceCourseMapMouseMoveHandler();
    
    private static Logger logger = Logger.getLogger("com.sap.sailing");

    class RaceCourseMapMouseMoveHandler implements MapMouseMoveHandler {

        @Override
        public void onMouseMove(MapMouseMoveEvent event) {
            if (startPoint != null) {
                Point s = getMap().convertLatLngToDivPixel(startPoint);
                drawPointWithText(s.getX(),s.getY(), "Start");
                refreshLine(event.getLatLng(), "Grey");
            }
            
        }
        
    }
    
    public RaceCourseCanvasOverlay() {
        super();
    }

    public void reset() {
        startPoint = null;
        endPoint = null;
        widgetPosLeft = 0;
        widgetPosTop = 0;
        if (startMarker != null) {
            map.removeOverlay(startMarker);
            startMarker = null;
        }
        if (endMarker != null) { 
            map.removeOverlay(endMarker);
            endMarker = null;
        }
        setCanvasSettings();
        canvas.setVisible(true);
        //getMap().addMapMouseMoveHandler(raceCourseMapMouseMoveHandler);
        //getPane().setWidgetPosition(getCanvas(), 0, 0);
       
        
    }
    
    public boolean isCourseSet() {
        return startPoint != null && endPoint != null;
    }
    
    private void setStartEndPoint(LatLng startPoint, LatLng endPoint) {
        setStartPoint(startPoint);
        setEndPoint(endPoint);
    }
    
    
    private void setStartPoint(LatLng startPoint) {
        if (this.startPoint == null) {
            //setCanvasSettings();
        }
        this.startPoint = startPoint;
        
        if (startMarker != null) {
            map.removeOverlay(startMarker);
        }
        
        if (startPoint != null) {
            Point point = getMap().convertLatLngToDivPixel(startPoint);
            drawPointWithText(point.getX()-widgetPosLeft,point.getY()-widgetPosTop,"Start");
            startMarker = new Marker(startPoint);
            map.addOverlay(startMarker);
 
        }
    }
    
    private void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;
        if (endMarker != null) {
            map.removeOverlay(endMarker);
        }
        if (endPoint != null) {
            Point point = getMap().convertLatLngToDivPixel(endPoint);
            drawPointWithText(point.getX()-widgetPosLeft,point.getY()-widgetPosTop,"End");
            endMarker = new Marker(endPoint);
            map.addOverlay(endMarker);
            
        }
    }
    
    private void setCanvasSettings() {
        int canvasWidth = getMap().getSize().getWidth();
        int canvasHeight = getMap().getSize().getHeight();
        logger.info("Canvas Width : " + canvasWidth);
        logger.info("Canvas Height : " + canvasHeight);
        canvas.setWidth(String.valueOf(canvasWidth));
        canvas.setHeight(String.valueOf(canvasHeight));
        canvas.setCoordinateSpaceWidth(canvasWidth);
        canvas.setCoordinateSpaceHeight(canvasHeight);
      
        if (startPoint != null) {
            //Point point = getMap().convertLatLngToDivPixel(startPoint);
            //widgetPosLeft = point.getX();
            //widgetPosTop = point.getY();
            Point sw = getMap().convertLatLngToDivPixel(getMap().getBounds().getSouthWest());
            Point ne = getMap().convertLatLngToDivPixel(getMap().getBounds().getNorthEast());
            widgetPosLeft = Math.min(sw.getX(), ne.getX());
            widgetPosTop = Math.min(sw.getY(), ne.getY());
            logger.info("WidgetPos Left,Top " + widgetPosLeft + "," + widgetPosTop);

        }
        getPane().setWidgetPosition(getCanvas(), widgetPosLeft, widgetPosTop);
        
    }
    
    @Override
    protected Overlay copy() {
        return new RaceCourseCanvasOverlay();
    }

    @Override
    protected void initialize(MapWidget map) {
        logger.info("In RaceCourseCanvasOverlay.initialize");
        super.initialize(map);
   
        getMap().addMapClickHandler(new MapClickHandler() {
            @Override
            public void onClick(MapClickEvent event) {
             
                if (startPoint == null) {
                    startPoint = event.getLatLng();
                    logger.info("Clicked startPoint here " + startPoint);   
                    if (startPoint != null) {
                       
                        setCanvasSettings();
                        drawCanvas();
                        setStartPoint(startPoint);
                        getMap().addMapMouseMoveHandler(raceCourseMapMouseMoveHandler); 
                    }
                }
            }
        });
        
        getMap().addMapDoubleClickHandler(new MapDoubleClickHandler() {

            @Override
            public void onDoubleClick(MapDoubleClickEvent event) {
                endPoint = event.getLatLng();
                logger.info("Clicked endPoint " + "here " + endPoint);
                if (endPoint != null) {
                    setEndPoint(endPoint);
                    getMap().removeMapMouseMoveHandler(raceCourseMapMouseMoveHandler);
                    //setSelected(false);
                    //canvas.setEnabled(false);
                }
            }

        });
        
        //raceCourseMapMouseMoveHandler = new RaceCourseMapMouseMoveHandler();
        
    }

    @Override
    protected void redraw(boolean force) {
        //setCanvasSettings();
        //drawCanvas();
        if (startPoint != null && endPoint != null) {
            logger.info("In RaceCourseCanvasOverlay.redraw");
            
            setCanvasSettings();
            drawCanvas();
            setStartEndPoint(startPoint,endPoint);
            drawLine(endPoint, "White");
            //getPane().setWidgetPosition(getCanvas(), 0, 0);
        }    
    }   

    private void drawPoint(double x, double y) {
        Context2d context2d = canvas.getContext2d();
        context2d.setStrokeStyle("Red");
        context2d.setLineWidth(3);
        context2d.beginPath();
        context2d.moveTo(x, y);
        context2d.lineTo(x, y);
        context2d.closePath();
        context2d.stroke();
        //context2d.fill();
        
    }
    
    private void drawPointWithText(double x, double y, String text) {
        
        Context2d context2d = canvas.getContext2d();
        drawPoint(x, y);
        context2d.setFillStyle("Black");
        context2d.fillText(text, x, y);
       
    }
    
    /*
     * Draw a line on the canvas from the startPoint to current point given by mouse location 
     * with a canvas refresh
     */
    private void refreshLine(LatLng currentPoint, String color)  {
           setCanvasSettings();
           drawLine(currentPoint, color);
           //getPane().setWidgetPosition(getCanvas(), 0, 0);
    }
    
    private void drawLine(LatLng currentPoint, String color) {
        if (startPoint != null) {
            Point s = map.convertLatLngToDivPixel(startPoint);
            Context2d context2d  = canvas.getContext2d();
            context2d.setStrokeStyle(color);
            context2d.beginPath();
            context2d.moveTo(s.getX()-widgetPosLeft, s.getY()-widgetPosTop);
            Point e = map.convertLatLngToDivPixel(currentPoint);
            context2d.lineTo(e.getX()-widgetPosLeft, e.getY()-widgetPosTop);
            context2d.closePath();
            context2d.stroke();
            //context2d.fill();
        }
    }
    
    private void drawCanvas() {
        logger.info("In drawCanvas");
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
}
