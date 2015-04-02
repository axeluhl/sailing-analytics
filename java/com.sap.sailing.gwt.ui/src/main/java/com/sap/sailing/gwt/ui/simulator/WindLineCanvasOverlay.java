package com.sap.sailing.gwt.ui.simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.sap.sailing.domain.common.Position;
import com.sap.sailing.domain.common.impl.DegreePosition;
import com.sap.sailing.gwt.ui.shared.WindLinesDTO;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.util.LineSegment;
import com.sap.sse.gwt.client.player.Timer;

public class WindLineCanvasOverlay extends FullCanvasOverlay implements TimeListenerWithStoppingCriteria {

    private WindLinesDTO windLinesDTO;

    private String lineColor = "Black";
    private final Timer timer;

    private Position[] corners;
    private LineSegment[] boundary;
    private Position center;
  
    private static Logger logger = Logger.getLogger(WindLineCanvasOverlay.class.getName());

    public WindLineCanvasOverlay(MapWidget map, int zIndex, final Timer timer) {
        super(map, zIndex);
        
        this.timer = timer;
        windLinesDTO = null;
        corners = null;
        boundary = null;
    }

    @Override
    public void timeChanged(final Date newTime, Date oldTime) {
        final Map<DegreePosition, SortedMap<Long, List<DegreePosition>>> windLinesMap = windLinesDTO.getWindLinesMap();

        if (windLinesMap == null) {
            return;
        }
         clear();
        
        final Context2d context2d = canvas.getContext2d();
        context2d.setGlobalAlpha(0.2);
        //context2d.setGlobalCompositeOperation(Composite.LIGHTER) ;
        
        int index = 0;
        for (final Entry<DegreePosition, SortedMap<Long, List<DegreePosition>>> entry : windLinesMap.entrySet()) {
            List<DegreePosition> positionDTOToDraw = new ArrayList<DegreePosition>();

            final SortedMap<Long, List<DegreePosition>> headMap = (entry.getValue().headMap(newTime.getTime() + 1));

            if (!headMap.isEmpty()) {
                positionDTOToDraw = headMap.get(headMap.lastKey());
            }
            logger.info("In WindLineCanvasOverlay.drawWindField drawing " + positionDTOToDraw.size() + " points"
                    + " @ " + newTime);

            drawWindLine(positionDTOToDraw, ++index);
        }
    }

    @Override
    public boolean shallStop() {
        final Map<DegreePosition, SortedMap<Long, List<DegreePosition>>> positionTimePointPositionDTOMap = windLinesDTO
                .getWindLinesMap();

        if (!this.isVisible() || positionTimePointPositionDTOMap == null || timer == null
                || positionTimePointPositionDTOMap.isEmpty()) {
            return true;
        }
        final Set<DegreePosition> positions = positionTimePointPositionDTOMap.keySet();
        if (positions != null && !positions.isEmpty()) {
            /**
             * Just check for one position as it would be the same for all the other positions
             */
            final SortedMap<Long, List<DegreePosition>> timePointPositionDTOMap = positionTimePointPositionDTOMap.get(positions
                    .iterator().next());
            if (timePointPositionDTOMap.lastKey() < timer.getTime().getTime()) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }

    public WindLinesDTO getWindLinesDTO() {
        return windLinesDTO;
    }

    public void setWindLinesDTO(final WindLinesDTO windLinesDTO) {
        this.windLinesDTO = windLinesDTO;
    }

    @Override
    public void addToMap() {
        super.addToMap();

        if (timer != null) {
            timer.addTimeListener(this);
        }
    }

    @Override
    public void removeFromMap() {
        super.removeFromMap();
        
        if (timer != null) {
            timer.removeTimeListener(this);
        }
    }

    @Override
    protected void draw() {
        super.draw();
        if (mapProjection != null && windLinesDTO != null) {
            clear();
            drawWindLine();
        }
    }

    private void clear() {
        canvas.getContext2d().clearRect(0.0 /* canvas.getAbsoluteLeft() */, 0.0/* canvas.getAbsoluteTop() */,
                canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight());
    }

    protected void drawWindLine() {
        if (timer != null) {
            timeChanged(timer.getTime(), null);
        }
    }

    protected void drawWindLine(final List<DegreePosition> positionDTOList, final int index) {
        if (positionDTOList == null) {
            return;
        }
        final int numPoints = positionDTOList.size();
        if (numPoints < 1) {
            return;
        }

        final String title = "Wind line at " + numPoints + " points.";
        getCanvas().setTitle(title);

        final Iterator<DegreePosition> positionDTOIter = positionDTOList.iterator();
        Position prevPositionDTO = null;
        while (positionDTOIter.hasNext()) {
            final Position positionDTO = positionDTOIter.next();
            if (prevPositionDTO != null) {
                if (checkPointInGrid(prevPositionDTO)  && checkPointInGrid(positionDTO) ) {
                    drawLine(prevPositionDTO, positionDTO);
                } else { 
                    final Position pointOnBoundary = getPointOnBoundary(prevPositionDTO, positionDTO);
                    if (pointOnBoundary != null) {
                        drawLine(prevPositionDTO, pointOnBoundary);
                    }
                }
            }
            prevPositionDTO = positionDTO;
        }

        /**
         * Debug code, to be removed
         */
        /*
        PositionDTO p1 = positionDTOList.get(0);

        LatLng positionLatLng = LatLng.newInstance(p1.latDeg, p1.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        int x = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        int y = canvasPositionInPx.getY() - this.getWidgetPosTop();
        drawPointWithText(x, y, "S" + index);

        PositionDTO p2 = positionDTOList.get(numPoints - 1);
        positionLatLng = LatLng.newInstance(p2.latDeg, p2.lngDeg);
        canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        x = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        y = canvasPositionInPx.getY() - this.getWidgetPosTop();

        drawPointWithText(x, y, "E" + index);
        */
    }

    private Position getPointOnBoundary(final Position p1, final Position p2) {
        if (boundary != null) {
            final LineSegment line = new LineSegment(p1.getLatDeg(), p1.getLngDeg(), p2.getLatDeg(), p2.getLngDeg());

            com.sap.sailing.gwt.ui.simulator.util.LineSegment.Point p = line.intersect(boundary[0]);
            if (p != null) {
                final Position pDTO = new DegreePosition(p.getX(), p.getY());
                return pDTO;
            }
            p = line.intersect(boundary[2]);
            if (p != null) {
                final Position pDTO = new DegreePosition(p.getX(), p.getY());
                return pDTO;
            }
            p = line.intersect(boundary[1]);
            if (p != null) {
                final Position pDTO = new DegreePosition(p.getX(), p.getY());
                return pDTO;
            }
            p = line.intersect(boundary[3]);
            if (p != null) {
                final Position pDTO = new DegreePosition(p.getX(), p.getY());
                return pDTO;
            }
        }
        return null;
    }

    private void drawLine(final Position p1, final Position p2) {
        final double weight = 1.0;

        LatLng positionLatLng = LatLng.newInstance(p1.getLatDeg(), p1.getLngDeg());
        Point canvasPositionInPx = mapProjection.fromLatLngToDivPixel(positionLatLng);

        final double x1 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        final double y1 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        positionLatLng = LatLng.newInstance(p2.getLatDeg(), p2.getLngDeg());
        canvasPositionInPx = mapProjection.fromLatLngToDivPixel(positionLatLng);
        final double x2 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        final double y2 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        drawLine(x1, y1, x2, y2, weight, lineColor);
    }

    public void setGridCorners(final Position[] gridCorners) {
        this.corners = gridCorners;
        if (corners != null && corners.length == 4) {
            this.boundary = new LineSegment[4];

            boundary[0] = new LineSegment(corners[0].getLatDeg(), corners[0].getLngDeg(), corners[1].getLatDeg(), corners[1].getLngDeg());
            boundary[1] = new LineSegment(corners[1].getLatDeg(), corners[1].getLngDeg(), corners[2].getLatDeg(), corners[2].getLngDeg());
            boundary[2] = new LineSegment(corners[2].getLatDeg(), corners[2].getLngDeg(), corners[3].getLatDeg(), corners[3].getLngDeg());
            boundary[3] = new LineSegment(corners[3].getLatDeg(), corners[3].getLngDeg(), corners[0].getLatDeg(), corners[0].getLngDeg());
            
            center = getCenter();
        }
    }

    private Point getPointInDivPixel(final Position p) {
        LatLng pLatLng = LatLng.newInstance(p.getLatDeg(), p.getLngDeg());
        Point canvasPositionInPx = mapProjection.fromLatLngToDivPixel(pLatLng);
        return Point.newInstance(canvasPositionInPx.getX(), canvasPositionInPx.getY());
    }
    
    @Override
    public void setCanvasSettings() {
        if (corners != null && corners.length == 4) {
            final Point corner0 = getPointInDivPixel(corners[0]);
            final Point corner1 = getPointInDivPixel(corners[1]);
            final Point corner3 = getPointInDivPixel(corners[3]);

            final int canvasWidth = (int) Math.sqrt(Math.pow(corner0.getX() - corner1.getX(), 2)
                    + Math.pow(corner0.getY() - corner1.getY(), 2));
            final int canvasHeight = (int) Math.sqrt(Math.pow(corner3.getX() - corner0.getX(), 2)
                    + Math.pow(corner3.getY() - corner0.getY(), 2));
            
            final int canvasRadius = (int) Math.sqrt(canvasWidth * canvasWidth / 4 + canvasHeight * canvasHeight / 4);
            canvas.setSize("" + 2 * canvasRadius + "px", "" + 2 * canvasRadius + "px");
            canvas.setCoordinateSpaceWidth(2 * canvasRadius); canvas.setCoordinateSpaceHeight(2 * canvasRadius);
             
            final Point anchorPoint = getAnchorPoint();

            setWidgetPosLeft(anchorPoint.getX());
            setWidgetPosTop(anchorPoint.getY());

            setCanvasPosition(anchorPoint.getX(), anchorPoint.getY());
        }
    }

    private Point getAnchorPoint() {
        if (corners != null) {
            final List<Double> xlist = new LinkedList<Double>();
            final List<Double> ylist = new LinkedList<Double>();
            for (int i = 0; i < corners.length; ++i) {
                final Point corner = getPointInDivPixel(corners[i]);
                xlist.add(corner.getX());
                ylist.add(corner.getY());
            }
            return Point.newInstance(Collections.min(xlist), Collections.min(ylist));
        }
        return null;

    }

    private Position getCenter() {
        final Position center;
        if (corners != null && corners.length == 4) {
            center = new DegreePosition(
                    (corners[0].getLatDeg() + corners[1].getLatDeg() + corners[2].getLatDeg() + corners[3].getLatDeg()) / 4.0,
                    (corners[0].getLngDeg() + corners[1].getLngDeg() + corners[2].getLngDeg() + corners[3].getLngDeg()) / 4.0);
        } else {
            center = null;
        }
        return center;
    }

    private boolean checkPointInGrid(final Position point) {
        if (center != null ) {
            final Position pointOnBoundary = getPointOnBoundary(center,point);
            if (pointOnBoundary == null) { // Line through center and the point does not intersect
                return true;
            } else {
                if (pointOnBoundary.equals(point)) {
                    return true;
                }
                return false;
            }
        }
        return false;
    }

}
