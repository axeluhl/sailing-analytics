/**
 * 
 */
package com.sap.sailing.gwt.ui.simulator.util;

import java.util.Map;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.sap.sailing.gwt.ui.shared.WindDTO;

/**
 * To handle MouseMoveEvent to display tool tips around WindDTO objects in a wind field
 * 
 * @author Nidhi Sawhney(D054070)
 * 
 */
public class WindFieldMouseMoveHandler implements MouseMoveHandler {

    private Canvas canvas;
    private Map<ToolTip, WindDTO> windFieldPoints = null;
    private ImageData lastImageData = null;
    private double lastToolx;
    private double lastTooly;

    public WindFieldMouseMoveHandler(Canvas canvas) {
        super();
        this.canvas = canvas;
    }

    public void clear() {
        lastImageData = null;
    }
    
    public void setWindFieldPoints(Map<ToolTip, WindDTO> windFieldPoints) {
        this.windFieldPoints = windFieldPoints;
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        //logger.info("In MouseMove");
        
        if (windFieldPoints == null) {
            return;
        }

        double x = event.getX();
        double y = event.getY();
        ToolTip point = new ToolTip(x, y);
        Context2d context2d = canvas.getContext2d();
        if (lastImageData != null) {
            context2d.putImageData(lastImageData, lastToolx, lastTooly);
        }
        
        if (windFieldPoints.containsKey(point)) {
            //logger.info("Found Point");
            lastToolx = x;
            lastTooly = y;
            lastImageData = context2d.getImageData(x, y, ToolTip.toolRectW, ToolTip.toolRectH);
           
            
            WindDTO windDTO = windFieldPoints.get(point);
            context2d.setFillStyle("#4f4f4f");
            context2d.fillRect(x, y, ToolTip.toolRectW, ToolTip.toolRectH);

            context2d.setFillStyle("#FFFFFF");
            context2d.fillRect(x + 3, y + 3, ToolTip.toolRectW - 6, ToolTip.toolRectH - 6);
           
            context2d.setFillStyle("Blue");
            String speedStr = NumberFormat.getFormat("#.00").format(windDTO.trueWindSpeedInKnots);
            String bearingStr = NumberFormat.getFormat("#.00").format(windDTO.trueWindFromDeg);
            String ttMsg = speedStr + " kn " + bearingStr + "°";
            context2d.fillText(ttMsg, x+10,y+15,60);
        }

    }

}
