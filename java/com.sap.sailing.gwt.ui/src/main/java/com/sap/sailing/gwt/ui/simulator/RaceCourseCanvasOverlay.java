package com.sap.sailing.gwt.ui.simulator;

import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
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

    private LatLng startPoint = null;
    private LatLng endPoint = null;
    private  Marker startMarker = null;
    private Marker endMarker = null;
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
        getMap().addMapMouseMoveHandler(raceCourseMapMouseMoveHandler);
        getPane().setWidgetPosition(getCanvas(), 0, 0);
       
        
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
            drawPointWithText(point.getX(),point.getY(),"Start");
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
            drawPointWithText(point.getX(),point.getY(),"End");
            endMarker = new Marker(endPoint);
            map.addOverlay(endMarker);
            
        }
    }
    
    private void setCanvasSettings() {
        int canvasWidth = getMap().getSize().getWidth();
        int canvasHeight = getMap().getSize().getHeight();
        canvas.setWidth(String.valueOf(canvasWidth));
        canvas.setHeight(String.valueOf(canvasHeight));
        canvas.setCoordinateSpaceWidth(canvasWidth);
        canvas.setCoordinateSpaceHeight(canvasHeight);
        //getPane().setWidgetPosition(getCanvas(), 0, 0);
    
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
                    setStartPoint(startPoint);
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
        getMap().addMapMouseMoveHandler(raceCourseMapMouseMoveHandler); 
    }

    @Override
    protected void redraw(boolean force) {
        if (startPoint != null && endPoint != null) {
            logger.info("In RaceCourseCanvasOverlay.redraw");
            
            setCanvasSettings();
            setStartEndPoint(startPoint,endPoint);
            drawLine(endPoint, "White");
            getPane().setWidgetPosition(getCanvas(), 0, 0);
        }    
    }   

    private void drawPoint(double x, double y) {
        Context2d context2d = canvas.getContext2d();
        context2d.setStrokeStyle("Red");
        context2d.setLineWidth(3);
        context2d.moveTo(x, y);
        context2d.lineTo(x, y);
        context2d.closePath();
        context2d.stroke();
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
           getPane().setWidgetPosition(getCanvas(), 0, 0);
    }
    
    private void drawLine(LatLng currentPoint, String color) {
        if (startPoint != null) {
            Point s = map.convertLatLngToDivPixel(startPoint);
            Context2d context2d  = canvas.getContext2d();
            context2d.setStrokeStyle(color);
            context2d.moveTo(s.getX(), s.getY());
            Point e = map.convertLatLngToDivPixel(currentPoint);
            context2d.lineTo(e.getX(), e.getY());
            context2d.closePath();
            context2d.stroke();
        }
    }
}
