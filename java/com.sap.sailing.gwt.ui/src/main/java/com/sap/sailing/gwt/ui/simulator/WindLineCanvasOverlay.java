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
import com.google.gwt.canvas.dom.client.Context2d.Composite;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.client.TimeListenerWithStoppingCriteria;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindLinesDTO;
import com.sap.sailing.gwt.ui.shared.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.util.LineSegment;

public class WindLineCanvasOverlay extends FullCanvasOverlay implements TimeListenerWithStoppingCriteria {

    private WindLinesDTO windLinesDTO;

    protected String lineColor = "Black";
    protected final Timer timer;

    private PositionDTO[] corners;
    private LineSegment[] boundary;
    private PositionDTO center;
  
    private static Logger logger = Logger.getLogger(WindLineCanvasOverlay.class.getName());

    public WindLineCanvasOverlay(Timer timer) {
        super();
        this.timer = timer;
        windLinesDTO = null;
        corners = null;
        boundary = null;
    }

    @Override
    public void timeChanged(Date date) {
        Map<PositionDTO, SortedMap<Long, List<PositionDTO>>> windLinesMap = windLinesDTO.getWindLinesMap();

        if (windLinesMap == null) {
            return;
        }
         clear();
        
        Context2d context2d = canvas.getContext2d();
        context2d.setGlobalAlpha(0.2);
        //context2d.setGlobalCompositeOperation(Composite.LIGHTER) ;
        
        int index = 0;
        for (Entry<PositionDTO, SortedMap<Long, List<PositionDTO>>> entry : windLinesMap.entrySet()) {
            List<PositionDTO> positionDTOToDraw = new ArrayList<PositionDTO>();

            SortedMap<Long, List<PositionDTO>> headMap = (entry.getValue().headMap(date.getTime() + 1));

            if (!headMap.isEmpty()) {
                positionDTOToDraw = headMap.get(headMap.lastKey());
            }
            logger.info("In WindLineCanvasOverlay.drawWindField drawing " + positionDTOToDraw.size() + " points"
                    + " @ " + date);

            drawWindLine(positionDTOToDraw, ++index);
        }
    }

    @Override
    public int stop() {
        Map<PositionDTO, SortedMap<Long, List<PositionDTO>>> positionTimePointPositionDTOMap = windLinesDTO
                .getWindLinesMap();

        if (!this.isVisible() || positionTimePointPositionDTOMap == null || timer == null
                || positionTimePointPositionDTOMap.isEmpty()) {
            return 0;
        }
        Set<PositionDTO> positions = positionTimePointPositionDTOMap.keySet();
        if (positions != null && !positions.isEmpty()) {
            /**
             * Just check for one position as it would be the same for all the other positions
             */
            SortedMap<Long, List<PositionDTO>> timePointPositionDTOMap = positionTimePointPositionDTOMap.get(positions
                    .iterator().next());
            if (timePointPositionDTOMap.lastKey() < timer.getTime().getTime()) {
                return 0;
            } else {
                return 1;
            }
        }

        return 0;
    }

    @Override
    protected Overlay copy() {
        return new WindLineCanvasOverlay(this.timer);
    }

    public WindLinesDTO getWindLinesDTO() {
        return windLinesDTO;
    }

    public void setWindLinesDTO(WindLinesDTO windLinesDTO) {
        this.windLinesDTO = windLinesDTO;
    }

    @Override
    protected void initialize(MapWidget map) {
        super.initialize(map);
        if (timer != null) {
            this.timer.addTimeListener(this);
        }
        setVisible(true);
    }

    @Override
    protected void remove() {
        setVisible(false);
        if (timer != null) {
            this.timer.removeTimeListener(this);
        }
        super.remove();
    }

    @Override
    protected void redraw(boolean force) {
        super.redraw(force);
        if (this.windLinesDTO != null) {
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
            timeChanged(timer.getTime());
        }
    }

    protected void drawWindLine(final List<PositionDTO> positionDTOList, int index) {

        if (positionDTOList == null) {
            return;
        }
        int numPoints = positionDTOList.size();
        if (numPoints < 1) {
            return;
        }

        String title = "Wind line at " + numPoints + " points.";
        getCanvas().setTitle(title);

        Iterator<PositionDTO> positionDTOIter = positionDTOList.iterator();
        PositionDTO prevPositionDTO = null;
        while (positionDTOIter.hasNext()) {
            PositionDTO positionDTO = positionDTOIter.next();
            if (prevPositionDTO != null) {
                boolean previousPointInGrid = checkPointInGrid(prevPositionDTO);
                boolean currentPointInGrid = checkPointInGrid(positionDTO);
                if (previousPointInGrid  && currentPointInGrid) {
                    drawLine(prevPositionDTO, positionDTO);
                } else { 
                    PositionDTO pointOnBoundary = getPointOnBoundary(prevPositionDTO, positionDTO);
                    if (pointOnBoundary != null) {
                        if (previousPointInGrid) {
                            drawLine(prevPositionDTO, pointOnBoundary);
                        } else if (currentPointInGrid) {
                            drawLine(pointOnBoundary, positionDTO);
                        }
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

    private PositionDTO getPointOnBoundary(PositionDTO p1, PositionDTO p2) {
       
        if (boundary != null) {

            LineSegment line = new LineSegment(p1.latDeg, p1.lngDeg, p2.latDeg, p2.lngDeg);

            com.sap.sailing.gwt.ui.simulator.util.LineSegment.Point p = line.intersect(boundary[0]);
            if (p != null) {
                PositionDTO pDTO = new PositionDTO(p.getX(), p.getY());
                return pDTO;
            }
            p = line.intersect(boundary[2]);
            if (p != null) {
                PositionDTO pDTO = new PositionDTO(p.getX(), p.getY());
                return pDTO;
            }
            p = line.intersect(boundary[1]);
            if (p != null) {
                PositionDTO pDTO = new PositionDTO(p.getX(), p.getY());
                return pDTO;
            }
            p = line.intersect(boundary[3]);
            if (p != null) {
                PositionDTO pDTO = new PositionDTO(p.getX(), p.getY());
                return pDTO;
            }

        }
        return null;
       

    }

    private void drawLine(PositionDTO p1, PositionDTO p2) {

        double weight = 1.0;

        LatLng positionLatLng = LatLng.newInstance(p1.latDeg, p1.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        int x1 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        int y1 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        positionLatLng = LatLng.newInstance(p2.latDeg, p2.lngDeg);
        canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);
        int x2 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        int y2 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        drawLine(x1, y1, x2, y2, weight, lineColor);

    }

    public void setGridCorners(PositionDTO[] gridCorners) {
        this.corners = gridCorners;
        if (corners != null && corners.length == 4) {
            this.boundary = new LineSegment[4];

            boundary[0] = new LineSegment(corners[0].latDeg, corners[0].lngDeg, corners[1].latDeg, corners[1].lngDeg);
            boundary[1] = new LineSegment(corners[1].latDeg, corners[1].lngDeg, corners[2].latDeg, corners[2].lngDeg);
            boundary[2] = new LineSegment(corners[2].latDeg, corners[2].lngDeg, corners[3].latDeg, corners[3].lngDeg);
            boundary[3] = new LineSegment(corners[3].latDeg, corners[3].lngDeg, corners[0].latDeg, corners[0].lngDeg);
            
            center = getCenter();

        }

    }

    private Point getPointInDivPixel(PositionDTO p) {
        LatLng pLatLng = LatLng.newInstance(p.latDeg, p.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(pLatLng);
        int x = canvasPositionInPx.getX();
        int y = canvasPositionInPx.getY();
        return Point.newInstance(x, y);
    }

 
    
    protected void setCanvasSettings() {
        if (corners != null && corners.length == 4) {

            Point corner0 = getPointInDivPixel(corners[0]);
            Point corner1 = getPointInDivPixel(corners[1]);
            Point corner3 = getPointInDivPixel(corners[3]);

            int canvasWidth = (int) Math.sqrt(Math.pow(corner0.getX() - corner1.getX(), 2)
                    + Math.pow(corner0.getY() - corner1.getY(), 2));
            int canvasHeight = (int) Math.sqrt(Math.pow(corner3.getX() - corner0.getX(), 2)
                    + Math.pow(corner3.getY() - corner0.getY(), 2));

            
            int canvasRadius = (int) Math.sqrt(canvasWidth * canvasWidth / 4 + canvasHeight * canvasHeight / 4);
            canvas.setSize("" + 2 * canvasRadius + "px", "" + 2 * canvasRadius + "px");
            canvas.setCoordinateSpaceWidth(2 * canvasRadius); canvas.setCoordinateSpaceHeight(2 * canvasRadius);
             
            Point anchorPoint = getAnchorPoint();

            setWidgetPosLeft(anchorPoint.getX());
            setWidgetPosTop(anchorPoint.getY());
           

            getPane().setWidgetPosition(getCanvas(), anchorPoint.getX(),
                    anchorPoint.getY());

        }

    }


    private Point getAnchorPoint() {

        if (corners != null) {
            List<Integer> xlist = new LinkedList<Integer>();
            List<Integer> ylist = new LinkedList<Integer>();
            for (int i = 0; i < corners.length; ++i) {
                Point corner = getPointInDivPixel(corners[i]);
                xlist.add(corner.getX());
                ylist.add(corner.getY());
            }
            return Point.newInstance(Collections.min(xlist), Collections.min(ylist));
        }
        return null;

    }

    private PositionDTO getCenter() {
        if (corners != null && corners.length == 4) {
            PositionDTO center = new PositionDTO();
            center.latDeg = (corners[0].latDeg + corners[1].latDeg + corners[2].latDeg + corners[3].latDeg) / 4.0;
            center.lngDeg = (corners[0].lngDeg + corners[1].lngDeg + corners[2].lngDeg + corners[3].lngDeg) / 4.0;
            return center;
        }
        return null;
    }

    private boolean checkPointInGrid(PositionDTO point) {
        if (center != null ) {
            PositionDTO pointOnBoundary = getPointOnBoundary(center,point);
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
