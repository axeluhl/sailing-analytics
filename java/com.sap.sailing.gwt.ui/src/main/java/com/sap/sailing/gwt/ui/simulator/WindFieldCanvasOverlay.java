package com.sap.sailing.gwt.ui.simulator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.racemap.FullCanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.util.ToolTip;
import com.sap.sailing.gwt.ui.simulator.util.WindFieldMouseMoveHandler;

/**
 * A google map overlay based on a HTML5 canvas for drawing a wind field. The overlay covers the whole map and displays
 * the wind objects inside it.
 * 
 * @author Nidhi Sawhney(D054070)
 * 
 */
public class WindFieldCanvasOverlay extends FullCanvasOverlay {

    /* The wind field that is to be displayed in the overlay */
    protected WindFieldDTO wl;

    /* The points where ToolTip is to be displayed */
    protected Map<ToolTip, WindDTO> windFieldPoints;
    protected String arrowColor = "Blue";
    protected String arrowHeadColor = "Blue";
    protected  WindFieldMouseMoveHandler mmHandler;
    
    private static Logger logger = Logger.getLogger("com.sap.sailing");

    public WindFieldCanvasOverlay() {
        super();
        windFieldPoints = new HashMap<ToolTip, WindDTO>();
      
         mmHandler = new WindFieldMouseMoveHandler(getCanvas());
         
         mmHandler.setWindFieldPoints(windFieldPoints);
         getCanvas().addMouseMoveHandler(mmHandler);
    }

    public void setWindField(WindFieldDTO wl) {
        this.wl = wl;
    }

    public void setArrowColor(String arrowColor, String arrowHeadColor) {
        this.arrowColor = arrowColor;
        this.arrowHeadColor = arrowHeadColor;
    }


    @Override
    protected Overlay copy() {
        return new WindFieldCanvasOverlay();
    }

    @Override
    protected void redraw(boolean force) {

        super.redraw(force);
        if (wl != null) {
            clear();
            drawCanvas();
            drawWindField();
        }

    }

    private void clear() {
        canvas.getContext2d().clearRect(widgetPosLeft, widgetPosTop, canvas.getCoordinateSpaceWidth(),
                canvas.getCoordinateSpaceHeight());
        windFieldPoints.clear();
        mmHandler.clear();
    }

    protected void drawWindField() {
        logger.fine("In WindFieldCanvasOverlay.drawWindField");
        List<WindDTO> windDTOList = wl.getMatrix();

        if (windDTOList != null && windDTOList.size() > 0) {
            Iterator<WindDTO> windDTOIter = windDTOList.iterator();
            int index = 0;
            while (windDTOIter.hasNext()) {
                WindDTO windDTO = windDTOIter.next();
                double length = 12;
                int width = (int) Math.max(1, Math.min(2, Math.round(windDTO.trueWindSpeedInMetersPerSecond)));
                DegreeBearingImpl dbi = new DegreeBearingImpl(windDTO.trueWindBearingDeg);
                drawArrow(windDTO, dbi.getRadians(), length, width, ++index);
                String msg = "Wind @ P" + index + ":" + windDTO.trueWindSpeedInKnots + "knots "
                        + windDTO.trueWindSpeedInMetersPerSecond + "m/s " + dbi.toString();
                logger.fine(msg);
                
            }
            String title = "Wind Field at " + windDTOList.size() + " points.";
            getCanvas().setTitle(title);
        }
    }

    protected void drawArrow(WindDTO windDTO, double angle, double length, double weight, int index) {

        PositionDTO position = windDTO.position;

        LatLng positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);

        int x = canvasPositionInPx.getX() - this.widgetPosLeft;
        int y = canvasPositionInPx.getY() - this.widgetPosTop;

        windFieldPoints.put(new ToolTip(x, y), windDTO);

        // TODO check if the angles are correct
        double dx = length * Math.sin(angle);
        double dy = length * Math.cos(angle);

        double x1 = x + dx / 2;
        double y1 = y + dy / 2;

        drawLine(x - dx / 2, y - dy / 2, x1, y1, weight, arrowColor);

        double theta = Math.atan2(-dy, dx);

        drawHead(x1, y1, theta, length / 2, weight);
        //String text = "P" + index;// + "(" + position.latDeg + "," + position.lngDeg + ")";
        // drawPointWithText(x, y, text);
        drawPoint(x, y);
    }

    protected void drawHead(double x, double y, double theta, double headLength, double weight) {

        double t = theta + (Math.PI / 4);
        if (t > Math.PI)
            t -= 2 * Math.PI;
        double t2 = theta - (Math.PI / 4);
        if (t2 <= (-Math.PI))
            t2 += 2 * Math.PI;

        double x1 = Math.round(x - Math.cos(t) * headLength);
        double y1 = Math.round(y + Math.sin(t) * headLength);
        double x2 = Math.round(x - Math.cos(t2) * headLength);
        double y2 = Math.round(y + Math.sin(t2) * headLength);
        drawLine(x, y, x1, y1, weight, arrowHeadColor);
        drawLine(x, y, x2, y2, weight, arrowHeadColor);

    }

}
