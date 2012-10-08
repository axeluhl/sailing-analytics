package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.shared.BuoyDTO;

/**
 * A google map overlay based on a HTML5 canvas for drawing buoys (images) and the buoy zone
 */
public class BuoyOverlay extends CanvasOverlay {

    /**
     * The buoy to draw
     */
    private BuoyDTO buoy;
    
    private int canvasWidth;
    private int canvasHeight;
    
    private double buoyZoneRadius;

    public BuoyOverlay(BuoyDTO buoy) {
        super();
        this.buoy = buoy;
        
        canvasWidth = 40;
        canvasHeight = 40;
        buoyZoneRadius = 20.0;

        if(getCanvas() != null) {
            getCanvas().setWidth(String.valueOf(canvasWidth));
            getCanvas().setHeight(String.valueOf(canvasHeight));
            getCanvas().setCoordinateSpaceWidth(canvasWidth);
            getCanvas().setCoordinateSpaceHeight(canvasHeight);
        }
    }

    @Override
    protected Overlay copy() {
        return new BuoyOverlay(buoy);
    }

    @Override
    protected void redraw(boolean force) {
        if (buoy != null && buoy.position != null) {
            
            Context2d context2d = getCanvas().getContext2d();
            CssColor grayTransparentColor = CssColor.make("rgba(255,255,255,0.75)");

            // this translation is important for drawing lines with a real line width of 1 pixel
            context2d.setStrokeStyle(grayTransparentColor);
            context2d.setLineWidth(1.0);
            context2d.beginPath();
            context2d.arc(canvasWidth / 2.0, canvasHeight / 2.0, buoyZoneRadius, 0, Math.PI*2, true); 
            context2d.closePath();
            context2d.stroke();
            
            LatLng latLngPosition = LatLng.newInstance(buoy.position.latDeg, buoy.position.lngDeg);
            Point buoyPositionInPx = getMap().convertLatLngToDivPixel(latLngPosition);
            getPane().setWidgetPosition(getCanvas(),
                    buoyPositionInPx.getX() - getCanvas().getCoordinateSpaceWidth() / 2,
                    buoyPositionInPx.getY() - getCanvas().getCoordinateSpaceHeight() / 2);
        }
    }

    public BuoyDTO getBuoy() {
        return buoy;
    }

    public void setBuoy(BuoyDTO buoy) {
        this.buoy = buoy;
    }

}
