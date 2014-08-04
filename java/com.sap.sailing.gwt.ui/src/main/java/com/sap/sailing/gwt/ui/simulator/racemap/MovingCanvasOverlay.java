package com.sap.sailing.gwt.ui.simulator.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.sap.sailing.gwt.ui.simulator.streamlets.Vector;

/**
 * This class extends @FullCanvasOverlay to provide the functionality that the canvas covers the
 * full viewable area of the map and moves previously drawn content according to drag and zoom
 * 
 * @author Christopher Ronnewinkel (D036654)
 *
 */
public abstract class MovingCanvasOverlay extends FullCanvasOverlay {

    private LatLng ne;
    private LatLng sw;

    public MovingCanvasOverlay(MapWidget map, int zIndex) {
        super(map, zIndex);
    }
    
    @Override
    public void setCanvasSettings() {
        int canvasWidth = getMap().getDiv().getClientWidth();
        int canvasHeight = getMap().getDiv().getClientHeight();
   
        Context2d ctxt = canvas.getContext2d();
        ImageData canvasContent = ctxt.getImageData(0, 0, canvas.getElement().getClientWidth(), canvas.getElement().getClientHeight());
        canvas.setWidth(String.valueOf(canvasWidth));
        canvas.setHeight(String.valueOf(canvasHeight));
        canvas.setCoordinateSpaceWidth(canvasWidth);
        canvas.setCoordinateSpaceHeight(canvasHeight);
        
        Point oldSWpx = mapProjection.fromLatLngToDivPixel(sw);
        Point oldNEpx = mapProjection.fromLatLngToDivPixel(ne);
        
        sw = getMap().getBounds().getSouthWest();
        ne = getMap().getBounds().getNorthEast();
        Point swPx = mapProjection.fromLatLngToDivPixel(sw);
        Point nePx = mapProjection.fromLatLngToDivPixel(ne);
        widgetPosLeft = Math.min(swPx.getX(), nePx.getX());
        widgetPosTop = Math.min(swPx.getY(), nePx.getY());

        double oldLeftpx;
        double oldToppx;
        if (oldSWpx == null) {
            oldLeftpx = widgetPosLeft;            
            oldToppx = widgetPosTop;
        } else {
            oldLeftpx = Math.min(oldSWpx.getX(), oldNEpx.getX());
            oldToppx = Math.min(oldSWpx.getY(), oldNEpx.getY());
        }

        diffPx = new Vector(oldLeftpx - widgetPosLeft, oldToppx - widgetPosTop);
        
        ctxt = canvas.getContext2d();
        ctxt.putImageData(canvasContent, diffPx.x, diffPx.y);

        setCanvasPosition(getWidgetPosLeft(), getWidgetPosTop());
    }

}
