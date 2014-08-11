package com.sap.sailing.gwt.ui.simulator.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.sap.sailing.gwt.ui.simulator.streamlets.Vector;

/**
 * This class extends {@link FullCanvasOverlay} to provide the functionality that the canvas covers the
 * full viewable area of the map and moves previously drawn content according to drag and zoom.
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
        
        // do nothing, if mapProjection is not available
        if (mapProjection == null)
            return;
        
        int canvasWidth = getMap().getDiv().getClientWidth();
        int canvasHeight = getMap().getDiv().getClientHeight();

        // calculate pixel-positions of old and new canvas-bounds using the same, current mapProjection
        // start from LatLng, as the canvas might have jumped to new bounds that do not intersect with the old bounds
        Point swOldPx = mapProjection.fromLatLngToDivPixel(sw);
        Point neOldPx = mapProjection.fromLatLngToDivPixel(ne);
        sw = getMap().getBounds().getSouthWest();
        ne = getMap().getBounds().getNorthEast();
        Point swNewPx = mapProjection.fromLatLngToDivPixel(sw);
        Point neNewPx = mapProjection.fromLatLngToDivPixel(ne);
        widgetPosLeft = Math.min(swNewPx.getX(), neNewPx.getX());
        widgetPosTop = Math.min(swNewPx.getY(), neNewPx.getY());

        // calculate the translation-vector between old and new origin in pixels
        if (swOldPx == null) {
            diffPx = new Vector(0, 0);
        } else {
            double oldPosLeft = Math.min(swOldPx.getX(), neOldPx.getX());
            double oldPosTop = Math.min(swOldPx.getY(), neOldPx.getY());
            diffPx = new Vector(oldPosLeft - widgetPosLeft, oldPosTop - widgetPosTop);
        }
        
        // store canvas-content, because setWidth() and setHeight() will clear canvas and change Context2d 
        Context2d ctxt = canvas.getContext2d();
        ImageData canvasContent = ctxt.getImageData(0, 0, canvas.getElement().getClientWidth(), canvas.getElement().getClientHeight());
        canvas.setWidth(String.valueOf(canvasWidth));
        canvas.setHeight(String.valueOf(canvasHeight));
        canvas.setCoordinateSpaceWidth(canvasWidth);
        canvas.setCoordinateSpaceHeight(canvasHeight);
        // get updated Context2d and restore canvas-content moved by translation-vector
        ctxt = canvas.getContext2d();
        ctxt.putImageData(canvasContent, diffPx.x, diffPx.y);

        // update canvas position
        setCanvasPosition(widgetPosLeft, widgetPosTop);
    }

    @Override
    protected void draw() {
        // do nothing; setCanvasSettings() is called directly
    }
    
    public void initCanvasOrigin() {
        sw = null;
        ne = null;
    }
}
