package com.sap.sailing.gwt.ui.simulator;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
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

    @Override
    protected void drawWindField() {
        logger.fine("In PathCanvasOverlay.drawWindField");
        List<WindDTO> windDTOList = wl.getMatrix();

        if (windDTOList != null && windDTOList.size() > 0) {
            Iterator<WindDTO> windDTOIter = windDTOList.iterator();
            int index = 0;
            WindDTO prevWindDTO = null;
            double length = 12;
            while (windDTOIter.hasNext()) {
                WindDTO windDTO = windDTOIter.next();
                if (prevWindDTO != null) {
                    drawLine(prevWindDTO, windDTO);
                }

                WindDTO windDTONext = null;
                if (windDTOIter.hasNext()) {
                    windDTONext = windDTOIter.next();
                    drawLine(windDTO, windDTONext); 
                    prevWindDTO = windDTONext;
                }
                
                if (displayWindAlongPath) {
                    int width = (int) Math.max(1, Math.min(2, Math.round(windDTO.trueWindSpeedInMetersPerSecond)));
                    DegreeBearingImpl dbi = new DegreeBearingImpl(windDTO.trueWindBearingDeg);
                    drawArrow(windDTO, dbi.getRadians(), length, width, ++index);
                    if (windDTONext != null) {
                        width = (int) Math.max(1, Math.min(2, Math.round(windDTO.trueWindSpeedInMetersPerSecond)));
                        dbi = new DegreeBearingImpl(windDTO.trueWindBearingDeg);
                        drawArrow(windDTONext, dbi.getRadians(), length, width, ++index);
                    }
                }
            }
            String title = "Path at " + windDTOList.size() + " points.";
            getCanvas().setTitle(title);
        }
    }

    private void drawLine(WindDTO p1, WindDTO p2) {
        PositionDTO position = p1.position;

        LatLng positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        int x1 = canvasPositionInPx.getX() - this.widgetPosLeft;
        int y1 = canvasPositionInPx.getY() - this.widgetPosTop;

        position = p2.position;
        positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);
        int x2 = canvasPositionInPx.getX() - this.widgetPosLeft;
        int y2 = canvasPositionInPx.getY() - this.widgetPosTop;

        this.pointColor = pathColor;
        drawPoint(x1, y1);
        drawLine(x1, y1, x2, y2, 1/* weight */, pathColor);
        drawPoint(x2, y2);
    }
}
