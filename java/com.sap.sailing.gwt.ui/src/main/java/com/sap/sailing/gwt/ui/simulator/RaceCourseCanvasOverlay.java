package com.sap.sailing.gwt.ui.simulator;

import java.util.logging.Logger;

import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.event.MapClickHandler;
import com.google.gwt.maps.client.event.MapDoubleClickHandler;
import com.google.gwt.maps.client.event.MapMouseMoveHandler;
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

    public LatLng startPoint;
    public LatLng endPoint;
    private Marker startMarker;
    private Marker endMarker;

    private RaceCourseMapMouseMoveHandler raceCourseMapMouseMoveHandler = new RaceCourseMapMouseMoveHandler();

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    class RaceCourseMapMouseMoveHandler implements MapMouseMoveHandler {

        @Override
        public void onMouseMove(MapMouseMoveEvent event) {
            if (startPoint != null) {
                Point s = getMap().convertLatLngToDivPixel(startPoint);
                drawPointWithText(s.getX(), s.getY(), "Start");
                refreshLine(event.getLatLng(), "Grey");
            }

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
        if (startMarker != null) {
            map.removeOverlay(startMarker);
        }
        if (startPoint != null) {
            Point point = getMap().convertLatLngToDivPixel(startPoint);
            drawPointWithText(point.getX() - widgetPosLeft, point.getY() - widgetPosTop, "Start");
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
            drawPointWithText(point.getX() - widgetPosLeft, point.getY() - widgetPosTop, "End");
            endMarker = new Marker(endPoint);
            map.addOverlay(endMarker);

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
                    logger.fine("Clicked endPoint " + "here " + endPoint);
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

        if (startPoint != null && endPoint != null) {
            setCanvasSettings();
            // drawCanvas();
            setStartEndPoint(startPoint, endPoint);
            drawLine(endPoint, "White");
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
            drawLine(s.getX() - widgetPosLeft, s.getY() - widgetPosTop, e.getX() - widgetPosLeft, e.getY()
                    - widgetPosTop, 1, color);
            double distanceInNmi = startPoint.distanceFrom(currentPoint)/Mile.METERS_PER_NAUTICAL_MILE;
            canvas.setTitle("Distance (nmi)  " + NumberFormat.getFormat("0.00").format(distanceInNmi));
        }
    }

}
