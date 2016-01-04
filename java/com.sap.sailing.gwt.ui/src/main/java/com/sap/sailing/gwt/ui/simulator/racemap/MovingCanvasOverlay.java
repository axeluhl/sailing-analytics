package com.sap.sailing.gwt.ui.simulator.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.ImageData;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.sap.sailing.gwt.ui.client.shared.racemap.CoordinateSystem;
import com.sap.sailing.gwt.ui.simulator.streamlets.Vector;

/**
 * This class extends {@link FullCanvasOverlay} to provide the functionality that the canvas covers the
 * full viewable area of the map and moves previously drawn content according to drag and zoom.
 * 
 * @author Christopher Ronnewinkel (D036654)
 *
 */
public abstract class MovingCanvasOverlay extends FullCanvasOverlay {

    private LatLng nw;

    public MovingCanvasOverlay(MapWidget map, int zIndex, CoordinateSystem coordinateSystem) {
        super(map, zIndex, coordinateSystem);
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
        Point nwOldPx;
        if (nw == null) {
            nwOldPx = null;
        } else {
            nwOldPx = mapProjection.fromLatLngToDivPixel(nw);
        }
        nw = LatLng.newInstance(getMap().getBounds().getNorthEast().getLatitude(), getMap().getBounds().getSouthWest().getLongitude());
        Point nwNewPx = mapProjection.fromLatLngToDivPixel(nw);
        widgetPosLeft = Math.round(nwNewPx.getX());
        widgetPosTop = Math.round(nwNewPx.getY());

        // calculate the translation-vector between old and new origin in pixels
        if (nwOldPx == null) {
            diffPx = new Vector(0, 0);
        } else {
            double oldPosLeft = Math.round(nwOldPx.getX());
            double oldPosTop = Math.round(nwOldPx.getY());
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
        nw = null;
    }
}
