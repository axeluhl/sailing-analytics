package com.sap.sailing.gwt.ui.simulator;


import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.TimeZone;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.sap.sailing.domain.common.Mile;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.gwt.ui.client.Timer;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;

/**
 * This class implements the layer which displays the optimal path on the path. Currently there is a single path to be
 * displayed.
 * 
 * @author D054070
 * 
 */
public class PathCanvasOverlay extends WindFieldCanvasOverlay {

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    public String pathColor = "Green";
    /**
     * Whether or not to display the wind directions for the points on the optimal path.
     */
    public boolean displayWindAlongPath = true;

    public PathCanvasOverlay() {
        super();
    }


    public PathCanvasOverlay(Timer timer) {
        super(timer);
    }
    
    @Override
    protected void drawWindField() {
        logger.fine("In PathCanvasOverlay.drawWindField");
        List<WindDTO> windDTOList = wl.getMatrix();
        drawWindField(windDTOList);
    }
    
    protected void drawWindField(final List<WindDTO> windDTOList) {

        int numPoints = windDTOList.size();
        String title = "Path at " + numPoints + " points.";
        long totalTime = windDTOList.get(numPoints-1).timepoint - windDTOList.get(0).timepoint;

        LatLng start = LatLng.newInstance(windDTOList.get(0).position.latDeg, windDTOList.get(0).position.latDeg);
        LatLng end = LatLng.newInstance(windDTOList.get(numPoints-1).position.latDeg, windDTOList.get(numPoints-1).position.latDeg);
        
        double distance = start.distanceFrom(end)/Mile.METERS_PER_NAUTICAL_MILE;
        
        Point startPx = getMap().convertLatLngToDivPixel(start);
        Point endPx = getMap().convertLatLngToDivPixel(end);
        double rcLengthPx = Math.sqrt(Math.pow(startPx.getX()-endPx.getX(),2) + Math.pow(startPx.getY()-endPx.getY(),2));
        //System.out.print("Race Course Pixel Length: "+rcLengthPx+"\n");

        double arrowDistPx = 60;
        long arrowInterleave = Math.max(1, Math.round(arrowDistPx * numPoints / rcLengthPx));
        //System.out.print("Arrow Interleave: "+arrowInterleave+"\n");
        
        if (windDTOList != null && windDTOList.size() > 0) {
            Iterator<WindDTO> windDTOIter = windDTOList.iterator();
            int index = 0;
            WindDTO prevWindDTO = null;
            while (windDTOIter.hasNext()) {
                WindDTO windDTO = windDTOIter.next();
                if (prevWindDTO != null) {
                    drawLine(prevWindDTO, windDTO);
                }
                prevWindDTO = windDTO;

                if ((displayWindAlongPath)&&((index % arrowInterleave) == 0)) {
                    DegreeBearingImpl dbi = new DegreeBearingImpl(windDTO.trueWindBearingDeg);
                    //System.out.print("index: "+index+"\n");
                    drawScaledArrow(windDTO, dbi.getRadians(), index);
                }

                index++;
            }

            //MeterDistance meterDistance = new MeterDistance(distance);
            Date timeDiffDate = new Date(totalTime);
            TimeZone gmt = TimeZone.createTimeZone(0);
            title += " " + NumberFormat.getFormat("0.00").format(distance) + " nmi";
            title += " in " + DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.HOUR24_MINUTE_SECOND).format(timeDiffDate, gmt);
           
            //logger.info(title);
            getCanvas().setTitle(title);
        }
    }

    private void drawLine(WindDTO p1, WindDTO p2) {
        PositionDTO position = p1.position;

        LatLng positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        int x1 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        int y1 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        position = p2.position;
        positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);
        int x2 = canvasPositionInPx.getX() - this.getWidgetPosLeft();
        int y2 = canvasPositionInPx.getY() - this.getWidgetPosTop();

        this.pointColor = "Black";
        drawPoint(x1, y1);
        drawLine(x1, y1, x2, y2, 3/* weight */, pathColor);
        //System.out.print("x1:"+x1+" y1:"+y1+" x2:"+x2+" y2:"+y2+"\n");
        drawPoint(x2, y2);
    }
}
