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
import com.google.gwt.canvas.dom.client.ImageData;
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
    // private Point centerInPixel;

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
        /**
         * Debug code
         */
        // lineColor = "Black";
        // drawCanvas();
         lineColor = "Red";
         drawLine(corners[0], corners[1]);
         drawLine(corners[1], corners[2]);
         drawLine(corners[2], corners[3]);
         drawLine(corners[3], corners[0]);
         lineColor = "Grey";
         
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
       //setWidgetOnMap();
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

        Context2d context2d = canvas.getContext2d();
        context2d.setGlobalAlpha(0.2);

        Iterator<PositionDTO> positionDTOIter = positionDTOList.iterator();
        PositionDTO prevPositionDTO = null;
        while (positionDTOIter.hasNext()) {
            PositionDTO positionDTO = positionDTOIter.next();
            if (prevPositionDTO != null) {
                if (positionDTOIter.hasNext()) {
                    drawLine(prevPositionDTO, positionDTO);
                } else { /* it is the last point so we need to check if its in the boundary */
                    PositionDTO pointOnBoundary = getPointOnBoundary(prevPositionDTO, positionDTO);
                    if (pointOnBoundary != null) {
                        drawLine(prevPositionDTO, pointOnBoundary);
                        /**
                         * Debug code
                         */
                        LatLng positionLatLng = LatLng.newInstance(pointOnBoundary.latDeg, pointOnBoundary.lngDeg);
                        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

                        int x = canvasPositionInPx.getX() - this.getWidgetPosLeft();
                        int y = canvasPositionInPx.getY() - this.getWidgetPosTop();
                        drawPointWithText(x, y, "P" + index);

                    } else {
                        /**
                         * Debug code
                         */
                        Point pointInPx = getPointInDivPixel(prevPositionDTO);
                        drawPointWithText(pointInPx.getX()-this.getWidgetPosLeft(), pointInPx.getY()-this.getWidgetPosTop(), "PP" + index);
                        System.out.println("pointOnBoundary is null for line " + index);
                        System.out.println("p1 " + prevPositionDTO);
                        System.out.println("p2 " + positionDTO);
                        System.out.println("c1 " + corners[0]);
                        System.out.println("c2 " + corners[1]);
                        System.out.println("c3 " + corners[2]);
                        System.out.println("c4 " + corners[3]);
                    }
                }
            }
            prevPositionDTO = positionDTO;
        }

        /**
         * Debug code, to be removed
         */
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

        double weight = 3.0;

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

        }

    }

    private Point getPointInDivPixel(PositionDTO p) {
        LatLng pLatLng = LatLng.newInstance(p.latDeg, p.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(pLatLng);
        int x = canvasPositionInPx.getX();
        int y = canvasPositionInPx.getY();
        return Point.newInstance(x, y);
    }

    protected void notsetCanvasSettings() {
        if (corners != null && corners.length == 4) {

            Point corner0 = getPointInDivPixel(corners[0]);
            Point corner1 = getPointInDivPixel(corners[1]);
            Point corner2 = getPointInDivPixel(corners[2]);
            Point corner3 = getPointInDivPixel(corners[3]);

            int canvasWidth = (int) Math.sqrt(Math.pow(corner0.getX() - corner1.getX(), 2)
                    + Math.pow(corner0.getY() - corner1.getY(), 2));
            int canvasHeight = (int) Math.sqrt(Math.pow(corner3.getX() - corner0.getX(), 2)
                    + Math.pow(corner3.getY() - corner0.getY(), 2));

            canvas.setWidth(String.valueOf(canvasWidth));
            canvas.setHeight(String.valueOf(canvasHeight));
            canvas.setCoordinateSpaceWidth(canvasWidth);
            canvas.setCoordinateSpaceHeight(canvasHeight);

            /*
             * int canvasRadius = (int) Math.sqrt(canvasWidth * canvasWidth / 4 + canvasHeight * canvasHeight / 4);
             * canvas.setSize("" + 2 * canvasRadius + "px", "" + 2 * canvasRadius + "px");
             * canvas.setCoordinateSpaceWidth(2 * canvasRadius); canvas.setCoordinateSpaceHeight(2 * canvasRadius);
             */
            double dy = corner1.getY() - corner0.getY();
            double dx = corner1.getX() - corner0.getX();
            double angleInRadians = Math.atan2(dy, dx); // 180;
            double angleInDegrees = angleInRadians * 180. / Math.PI;
            System.out.println("Angle in degrees " + angleInDegrees + " in radians " + angleInRadians);

            PositionDTO center = getCenter(corners[0], corners[1], corners[2], corners[3]);
            LatLng pLatLng = LatLng.newInstance(center.latDeg, center.lngDeg);
            Point centerInPixel = getMap().convertLatLngToDivPixel(pLatLng);

            Context2d context = canvas.getContext2d();
            context.save();
            context.translate(canvasWidth / 2.0, canvasHeight / 2.0);

            context.rotate(angleInRadians);
            drawCircleWithText(0, 0, 3, "Blue", "C");
            drawCanvas();
            context.restore();

            setWidgetPosLeft(centerInPixel.getX() - canvasWidth / 2);
            setWidgetPosTop(centerInPixel.getY() - canvasHeight / 2);
            //setWidgetPosLeft(centerInPixel.getX());
            //setWidgetPosTop(centerInPixel.getY());

            getPane().setWidgetPosition(getCanvas(), centerInPixel.getX()-canvasWidth/2,
             centerInPixel.getY()-canvasHeight/2);

        }

    }
    
    protected void setCanvasSettings() {
        if (corners != null && corners.length == 4) {

            Point corner0 = getPointInDivPixel(corners[0]);
            Point corner1 = getPointInDivPixel(corners[1]);
            Point corner2 = getPointInDivPixel(corners[2]);
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


    private PositionDTO getCenter(PositionDTO a, PositionDTO b, PositionDTO c, PositionDTO d) {
        PositionDTO center = new PositionDTO();
        center.latDeg = (a.latDeg + b.latDeg + c.latDeg + d.latDeg) / 4.0;
        center.lngDeg = (a.lngDeg + b.lngDeg + c.lngDeg + d.lngDeg) / 4.0;
        return center;
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

    private void setWidgetOnMap() {

        Context2d context = canvas.getContext2d();
        int canvasWidth = canvas.getCoordinateSpaceWidth();
        int canvasHeight = canvas.getCoordinateSpaceHeight();
        
        Point anchorPointInPxl = getAnchorPoint();
      
        int canvasRadius = (int) Math.sqrt(canvasWidth * canvasWidth / 4 + canvasHeight * canvasHeight / 4);
        
        ImageData imageData = context.getImageData(anchorPointInPxl.getX() - this.getWidgetPosLeft(), 
                anchorPointInPxl.getY() - this.getWidgetPosTop(), 2*canvasRadius, 2*canvasRadius);
        //context.restore();
        canvas.setSize("" + 2 * canvasRadius + "px", "" + 2 * canvasRadius + "px");
        canvas.setCoordinateSpaceWidth(2 * canvasRadius);
        canvas.setCoordinateSpaceHeight(2 * canvasRadius);
        context.putImageData(imageData, 0, 0);
        drawCircleWithText(0,0,3,"Green","A");
        getPane().setWidgetPosition(getCanvas(), anchorPointInPxl.getX(), anchorPointInPxl.getY());
    }

   
    protected void notdrawCanvas() {

        Context2d context = canvas.getContext2d();
        context.setStrokeStyle("Black");
        context.setLineWidth(3);

        context.beginPath();
        context.moveTo(-canvas.getCoordinateSpaceWidth() / 2, +canvas.getCoordinateSpaceHeight() / 2);
        context.lineTo(+canvas.getCoordinateSpaceWidth() / 2, +canvas.getCoordinateSpaceHeight() / 2);
        context.lineTo(+canvas.getCoordinateSpaceWidth() / 2, -canvas.getCoordinateSpaceHeight() / 2);
        context.lineTo(-canvas.getCoordinateSpaceWidth() / 2, -canvas.getCoordinateSpaceHeight() / 2);
        context.closePath();
        context.stroke();

    }

}
