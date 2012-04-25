package com.sap.sailing.gwt.ui.simulator;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlay;

/**
 * A google map overlay based on a HTML5 canvas for drawing a wind field. The overlay covers the whole map and displays
 * the wind objects inside it.
 * 
 * @author Nidhi Sawhney
 * 
 */
public class WindFieldCanvasOverlay extends CanvasOverlay {

    /* x coordinate where the widget is placed */
    private final int xPos = 0;
    /* y coordinate where the widget is placed */
    private final int yPos = 0;

    /* The windfield that is to be displayed in the overlay */
    private WindFieldDTO wl;

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    public WindFieldCanvasOverlay() {
        super();

    }

    public void setWindField(WindFieldDTO wl) {
        this.wl = wl;
    }

    @Override
    protected Overlay copy() {
        return new WindFieldCanvasOverlay();
    }

    @Override
    protected void redraw(boolean force) {

        if (wl != null) {
            int canvasWidth = getMap().getSize().getWidth();
            int canvasHeight = getMap().getSize().getWidth();
            canvas.setWidth(String.valueOf(canvasWidth));
            canvas.setHeight(String.valueOf(canvasHeight));
            canvas.setCoordinateSpaceWidth(canvasWidth);
            canvas.setCoordinateSpaceHeight(canvasHeight);

            drawWindField();

            getPane().setWidgetPosition(getCanvas(), xPos, yPos);
        }

    }

    private void drawWindField() {
        logger.fine("In WindFieldCanvasOverlay.drawWindField");
        List<WindDTO> windDTOList = wl.getMatrix();

        if (windDTOList != null && windDTOList.size() > 0) {
            Iterator<WindDTO> windDTOIter = windDTOList.iterator();
            while (windDTOIter.hasNext()) {
                WindDTO windDTO = windDTOIter.next();
                double length = 10;
                int width = (int) Math.round(windDTO.trueWindSpeedInMetersPerSecond);
                DegreeBearingImpl dbi = new DegreeBearingImpl(windDTO.trueWindBearingDeg);
                drawArrow(windDTO.position, dbi.getRadians(), length, width);
            }
            String title = "Wind Field at " + windDTOList.size() + " points.";
            getCanvas().setTitle(title);
        }
    }

    private void drawArrow(PositionDTO position, double angle, double length, double weight) {

        Context2d context2d = canvas.getContext2d();
        context2d.setStrokeStyle("Blue");
        // context2d.setLineCap(LineCap.SQUARE);
        context2d.setLineWidth(weight);
        context2d.beginPath();

        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(
                LatLng.newInstance(position.latDeg, position.lngDeg));
        int x = canvasPositionInPx.getX();
        int y = canvasPositionInPx.getY();

        context2d.moveTo(x, y);
        double dx = length * Math.cos(angle);
        double dy = length * Math.sin(angle);
        double x1 = x + dx;
        double y1 = y + dy;
        context2d.lineTo(x1, y1);
        context2d.closePath();
        context2d.stroke();

        double theta = Math.atan2(-dy, dx);

        drawHead(x1, y1, theta, length / 2, weight);
    }

    private void drawHead(double x, double y, double theta, double headLength, double weight) {

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
        Context2d context2d = canvas.getContext2d();
        context2d.setStrokeStyle("Red");
        context2d.setLineWidth(weight);
        context2d.beginPath();
        context2d.moveTo(x, y);
        context2d.lineTo(x1, y1);
        context2d.closePath();
        context2d.stroke();

        context2d.beginPath();
        context2d.moveTo(x, y);
        context2d.lineTo(x2, y2);
        context2d.closePath();
        context2d.stroke();

    }

}
