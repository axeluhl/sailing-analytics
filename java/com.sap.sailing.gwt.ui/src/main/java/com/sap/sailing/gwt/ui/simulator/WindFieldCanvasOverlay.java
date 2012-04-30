package com.sap.sailing.gwt.ui.simulator;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.domain.common.impl.DegreeBearingImpl;
import com.sap.sailing.gwt.ui.shared.PositionDTO;
import com.sap.sailing.gwt.ui.shared.WindDTO;
import com.sap.sailing.gwt.ui.shared.WindFieldDTO;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlay;
import com.sap.sailing.gwt.ui.simulator.util.ToolTip;
import com.sap.sailing.gwt.ui.simulator.util.WindFieldMouseMoveHandler;

/**
 * A google map overlay based on a HTML5 canvas for drawing a wind field. The overlay covers the whole map and displays
 * the wind objects inside it.
 * 
 * @author Nidhi Sawhney(D054070)
 * 
 */
public class WindFieldCanvasOverlay extends CanvasOverlay {

    /* x coordinate where the widget is placed */
    private final int xPos = 0;
    /* y coordinate where the widget is placed */
    private final int yPos = 0;

    /* The wind field that is to be displayed in the overlay */
    private WindFieldDTO wl;
    
    /* The points where ToolTip is to be displayed */
    private Map<ToolTip, WindDTO> windFieldPoints = new HashMap<ToolTip, WindDTO>();
   

    private static Logger logger = Logger.getLogger("com.sap.sailing");

    public WindFieldCanvasOverlay() {
        super();

    }

    public void setWindField(WindFieldDTO wl) {
        this.wl = wl;
    }

    @Override
    protected void initialize(MapWidget map) {
        super.initialize(map);
        
        WindFieldMouseMoveHandler mmHandler = new WindFieldMouseMoveHandler(getCanvas());
        mmHandler.setWindFieldPoints(windFieldPoints);
        getCanvas().addMouseMoveHandler(mmHandler);
    }

    @Override
    protected Overlay copy() {
        return new WindFieldCanvasOverlay();
    }

    @Override
    protected void redraw(boolean force) {

        if (wl != null) {
            int canvasWidth = getMap().getSize().getWidth();
            int canvasHeight = getMap().getSize().getHeight();
            canvas.setWidth(String.valueOf(canvasWidth));
            canvas.setHeight(String.valueOf(canvasHeight));
            canvas.setCoordinateSpaceWidth(canvasWidth);
            canvas.setCoordinateSpaceHeight(canvasHeight);

            clear();

            drawWindField();

            getPane().setWidgetPosition(getCanvas(), xPos, yPos);
        }

    }

    private void clear() {
        windFieldPoints.clear();
    }

    private void drawWindField() {
        logger.fine("In WindFieldCanvasOverlay.drawWindField");
        List<WindDTO> windDTOList = wl.getMatrix();

        if (windDTOList != null && windDTOList.size() > 0) {
            Iterator<WindDTO> windDTOIter = windDTOList.iterator();
            int index = 0;
            while (windDTOIter.hasNext()) {
                WindDTO windDTO = windDTOIter.next();
                double length = 12;
                int width = (int) Math.round(windDTO.trueWindSpeedInMetersPerSecond);
                DegreeBearingImpl dbi = new DegreeBearingImpl(windDTO.trueWindBearingDeg);
                drawArrow(windDTO, dbi.getRadians(), length, width, ++index);
            }
            String title = "Wind Field at " + windDTOList.size() + " points.";
            getCanvas().setTitle(title);
        }
    }

    private void drawArrow(WindDTO windDTO, double angle, double length, double weight, int index) {

        PositionDTO position = windDTO.position;
        Context2d context2d = canvas.getContext2d();
        context2d.setStrokeStyle("Blue");
        // context2d.setLineCap(LineCap.SQUARE);
        context2d.setLineWidth(weight);
        context2d.beginPath();

        LatLng positionLatLng = LatLng.newInstance(position.latDeg, position.lngDeg);
        Point canvasPositionInPx = getMap().convertLatLngToDivPixel(positionLatLng);
        windFieldPoints.put(new ToolTip(canvasPositionInPx.getX(), canvasPositionInPx.getY()), windDTO);
      
        int x = canvasPositionInPx.getX();
        int y = canvasPositionInPx.getY();

        double dx = length * Math.cos(angle);
        double dy = length * Math.sin(angle);

        context2d.moveTo(x - dx / 2, y - dy / 2);
        double x1 = x + dx / 2;
        double y1 = y + dy / 2;
        context2d.lineTo(x1, y1);
        context2d.closePath();
        context2d.stroke();

        double theta = Math.atan2(-dy, dx);

        drawHead(x1, y1, theta, length / 2, weight);
        String text = "P" + index;// + "(" + position.latDeg + "," + position.lngDeg + ")";
        drawPointWithText(x, y, text);
    }

    private void drawPoint(double x, double y) {
        Context2d context2d = canvas.getContext2d();
        context2d.setStrokeStyle("Red");
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
