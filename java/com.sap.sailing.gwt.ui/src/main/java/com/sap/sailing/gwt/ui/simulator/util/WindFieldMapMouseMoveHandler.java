/**
 * 
 */
package com.sap.sailing.gwt.ui.simulator.util;

import java.util.Map;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapEvent;
import com.google.gwt.maps.client.events.mousemove.MouseMoveMapHandler;
import com.sap.sailing.gwt.ui.shared.SimulatorWindDTO;
import com.sap.sailing.gwt.ui.simulator.racemap.FullCanvasOverlay;

/**
 * To handle MouseMoveEvent to display tool tips around SimulatorWindDTO objects in a wind field
 * 
 * @author Nidhi Sawhney(D054070)
 * 
 */
public class WindFieldMapMouseMoveHandler implements MouseMoveMapHandler {

    private final FullCanvasOverlay canvasOverlay;
    private Map<ToolTip, SimulatorWindDTO> windFieldPoints = null;
    private ImageData lastImageData = null;
    private double lastToolx;
    private double lastTooly;

    public WindFieldMapMouseMoveHandler(final FullCanvasOverlay canvasOverlay) {
        super();
        this.canvasOverlay = canvasOverlay;
    }

    public void clear() {
        lastImageData = null;
    }

    public void setWindFieldPoints(final Map<ToolTip, SimulatorWindDTO> windFieldPoints) {
        this.windFieldPoints = windFieldPoints;
    }

    @Override
    public void onEvent(final MouseMoveMapEvent event) {
        if (windFieldPoints != null) {
            final LatLng latLng = event.getMouseEvent().getLatLng();
            final Point pointP = canvasOverlay.getMapProjection().fromLatLngToDivPixel(latLng);
            final double x = pointP.getX() - canvasOverlay.getWidgetPosLeft();
            final double y = pointP.getY() - canvasOverlay.getWidgetPosTop();
            final ToolTip point = new ToolTip(x, y);
            final Context2d context2d = canvasOverlay.getCanvas().getContext2d();
            if (lastImageData != null) {
                context2d.putImageData(lastImageData, lastToolx, lastTooly);
            }
    
            if (windFieldPoints.containsKey(point)) {
                lastToolx = x;
                lastTooly = y;
                lastImageData = context2d.getImageData(x, y, ToolTip.toolRectW, ToolTip.toolRectH);
       
                final SimulatorWindDTO windDTO = windFieldPoints.get(point);
                context2d.setFillStyle("#4f4f4f");
                context2d.fillRect(x, y, ToolTip.toolRectW, ToolTip.toolRectH);
    
                context2d.setFillStyle("#FFFFFF");
                context2d.fillRect(x + 3, y + 3, ToolTip.toolRectW - 6, ToolTip.toolRectH - 6);
    
                context2d.setFillStyle("Blue");
                final String speedStr = NumberFormat.getFormat("#.00").format(windDTO.trueWindSpeedInKnots);
                final String bearingStr = NumberFormat.getFormat("#.00").format(windDTO.trueWindBearingDeg);
                final String ttMsg = speedStr + " kn " + bearingStr + "�";
                context2d.fillText(ttMsg, x+10,y+15,60);
            }
        }
    }

}
