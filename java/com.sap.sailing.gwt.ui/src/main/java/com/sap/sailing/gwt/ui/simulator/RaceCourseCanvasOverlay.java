package com.sap.sailing.gwt.ui.simulator;

import java.util.logging.Logger;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.InfoWindow;
import com.google.gwt.maps.client.InfoWindowContent;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.MapDoubleClickHandler;
import com.google.gwt.maps.client.event.MapMouseMoveHandler;
import com.google.gwt.maps.client.event.MarkerMouseOutHandler;
import com.google.gwt.maps.client.event.MarkerMouseOverHandler;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.domain.common.Mile;
import com.sap.sailing.gwt.ui.shared.racemap.FullCanvasOverlay;

/**
 * This class implements the layer to display the race course on the map. Currently the course only consists of the
 * start(startPoint) and end point(endPoint) which are captured by a single and double click respectively.
 * 
 * @author Nidhi Sawhney(D054070)
 * 
 */
public class RaceCourseCanvasOverlay extends FullCanvasOverlay {

    public String racecourseColor = "White";
    public double racecourseBuoySize = 5;

    public LatLng startPoint;
    public LatLng endPoint;
    private Marker startMarker;
    private Marker endMarker;

    private RaceCourseMapMouseMoveHandler raceCourseMapMouseMoveHandler = new RaceCourseMapMouseMoveHandler();

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    private class RaceCourseMapMouseMoveHandler implements MapMouseMoveHandler {

        @Override
        public void onMouseMove(MapMouseMoveEvent event) {
            if (startPoint != null) {
                Point s = getMap().convertLatLngToDivPixel(startPoint);
                drawPointWithText(s.getX(), s.getY(), "Start");
                refreshLine(event.getLatLng(), "Grey");
            }

        }

    }

    private class RaceCourseMarkerMouseOverHandler implements MarkerMouseOverHandler {

        @Override
        public void onMouseOver(MarkerMouseOverEvent event) {
            Double distanceInNmi = startPoint.distanceFrom(endPoint)/ Mile.METERS_PER_NAUTICAL_MILE;
            final String sDistance = NumberFormat.getFormat("0.00").format(distanceInNmi);
            InfoWindowContent content = new InfoWindowContent("Race Target "
                    + sDistance + " nmi from Start");
            map.getInfoWindow().open(endMarker, content);
            
        }
        
    }
    
    public RaceCourseCanvasOverlay() {
        super();
        startPoint = null;
        endPoint = null;
        startMarker = null;
        endMarker = null;
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
    }

    public boolean isCourseSet() {
        return startPoint != null && endPoint != null;
    }

    private void setStartEndPoint(LatLng startPoint, LatLng endPoint) {
        setStartPoint(startPoint);
        setEndPoint(endPoint);
    }

    private void setStartPoint(LatLng startPoint) {
        this.startPoint = startPoint;

        if (startPoint != null) {
            Point point = getMap().convertLatLngToDivPixel(startPoint);
            //drawPointWithText(point.getX() - getWidgetPosLeft(), point.getY() - getWidgetPosTop(), "Start");
            drawCircleWithText(point.getX() - getWidgetPosLeft(),point.getY() - getWidgetPosTop(),racecourseBuoySize,racecourseColor,"Start");
            if (startMarker != null) {
                startMarker.setLatLng(startPoint);
            } else {
// default markers are too large cluttering race display
/*                startMarker = new Marker(startPoint);
                map.addOverlay(startMarker);
                /*
                 * startMarker.addMarkerMouseOverHandler(new MarkerMouseOverHandler () {
                 * 
                 * @Override public void onMouseOver(MarkerMouseOverEvent event) { InfoWindowContent content = new
                 * InfoWindowContent("Start"); map.getInfoWindow().open(startMarker, content); }
                 * 
                 * });
                 * 
                 * startMarker.addMarkerMouseOutHandler(new MarkerMouseOutHandler () {
                 * 
                 * @Override public void onMouseOut(MarkerMouseOutEvent event) { map.getInfoWindow().close(); }
                 * 
                 * });
                 */
            }

        }
    }

    private void setEndPoint(LatLng endPoint) {
        this.endPoint = endPoint;

        if (endPoint != null) {
            Point point = getMap().convertLatLngToDivPixel(endPoint);
            //drawPointWithText(point.getX() - getWidgetPosLeft(), point.getY() - getWidgetPosTop(), "End");
            drawCircleWithText(point.getX() - getWidgetPosLeft(),point.getY() - getWidgetPosTop(),racecourseBuoySize,racecourseColor,"End");
            double cLat = (startPoint.getLatitude() + ((endPoint.getLatitude() - startPoint.getLatitude())%180.)/2. + 90.)%180. - 90.;
            double cLon = (startPoint.getLongitude() + ((endPoint.getLongitude() - startPoint.getLongitude())%360.)/2. + 180.)%360. -180;
            LatLng centerPoint = LatLng.newInstance(cLat, cLon);
            //System.out.println("center: "+cLat+","+cLon);
            getMap().panTo(centerPoint);
            if (endMarker != null) {
                endMarker.setLatLng(endPoint);
            } else {
// default markers are too large cluttering race display
/*                endMarker = new Marker(endPoint);
                map.addOverlay(endMarker);
                
                endMarker.addMarkerMouseOverHandler(new RaceCourseMarkerMouseOverHandler());

                endMarker.addMarkerMouseOutHandler(new MarkerMouseOutHandler() {

                    @Override
                    public void onMouseOut(MarkerMouseOutEvent event) {
                        map.getInfoWindow().close();
                    }

                });*/
            }

        }
    }

    @Override
    protected Overlay copy() {
        return new RaceCourseCanvasOverlay();
    }

    @Override
    protected void initialize(MapWidget map) {

        super.initialize(map);

        getMap().addMapClickHandler(new MapClickHandler() {
            @Override
            public void onClick(MapClickEvent event) {

                if (startPoint == null) {
                    startPoint = event.getLatLng();
                    logger.fine("Clicked startPoint here " + startPoint);
                    if (startPoint != null) {

                        setCanvasSettings();
                        // drawCanvas();
                        setStartPoint(startPoint);
                        getMap().addMapMouseMoveHandler(raceCourseMapMouseMoveHandler);
                    }
                }
            }
        });

        getMap().addMapDoubleClickHandler(new MapDoubleClickHandler() {

            @Override
            public void onDoubleClick(MapDoubleClickEvent event) {
                if (isSelected) {
                    endPoint = event.getLatLng();
                    logger.info("Clicked endPoint " + "here " + endPoint);
                    if (endPoint != null) {
                        setEndPoint(endPoint);
                        getMap().removeMapMouseMoveHandler(raceCourseMapMouseMoveHandler);
                    }
                }
            }

        });

    }

    @Override
    protected void redraw(boolean force) {
        System.out.println("method: RaceCourseCanvasOverlay.redraw().");
        if (startPoint != null && endPoint != null) {
            setCanvasSettings();
            // drawCanvas();
            setStartEndPoint(startPoint, endPoint);
            drawLine(endPoint, racecourseColor);
        }
    }

    /*
     * Draw a line on the canvas from the startPoint to current point given by mouse location with a canvas refresh
     */
    private void refreshLine(LatLng currentPoint, String color) {
        setCanvasSettings();
        drawLine(currentPoint, color);
    }

    private void drawLine(LatLng currentPoint, String color) {
        if (startPoint != null) {
            Point s = map.convertLatLngToDivPixel(startPoint);
            Point e = map.convertLatLngToDivPixel(currentPoint);
            drawLine(s.getX() - getWidgetPosLeft(), s.getY() - getWidgetPosTop(), e.getX() - getWidgetPosLeft(),
                    e.getY() - getWidgetPosTop(), 1, color);
            double distanceInNmi = startPoint.distanceFrom(currentPoint) / Mile.METERS_PER_NAUTICAL_MILE;
            canvas.setTitle("Distance (nmi)  " + NumberFormat.getFormat("0.00").format(distanceInNmi));
        }
    }

}
