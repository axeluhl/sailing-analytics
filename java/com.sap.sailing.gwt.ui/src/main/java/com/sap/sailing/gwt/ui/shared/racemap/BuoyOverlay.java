package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.BuoyDTO;

/**
 * A google map overlay based on a HTML5 canvas for drawing buoys (images) and the buoy zone
 */
public class BuoyOverlay extends CanvasOverlay {

    /**
     * The buoy to draw
     */
    private BuoyDTO buoy;
    
    private double buoyZoneRadiusInMeter;

    public BuoyOverlay(BuoyDTO buoy, BoatClassDTO boatClass) {
        super();
        this.buoy = buoy;
        this.buoyZoneRadiusInMeter = boatClass.getHullLengthInMeters() * 3;
    }
    
    public BuoyOverlay(BuoyDTO buoy, double buoyZoneRadiusInMeter) {
        super();
        this.buoy = buoy;
        this.buoyZoneRadiusInMeter = buoyZoneRadiusInMeter;
    }

    @Override
    protected Overlay copy() {
        return new BuoyOverlay(buoy, buoyZoneRadiusInMeter);
    }

    @Override
    protected void redraw(boolean force) {
        if (buoy != null && buoy.position != null) {
            LatLng latLngPosition = LatLng.newInstance(buoy.position.latDeg, buoy.position.lngDeg);
            int buoyZoneRadiusInPixel = calculateRadiusOfBoundingBox(latLngPosition, buoyZoneRadiusInMeter);
            if(buoyZoneRadiusInPixel > 10) {
                setCanvasSize(buoyZoneRadiusInPixel * 2, buoyZoneRadiusInPixel * 2);
                
                Context2d context2d = getCanvas().getContext2d();
                context2d.clearRect(0, 0, buoyZoneRadiusInPixel * 2, buoyZoneRadiusInPixel * 2);
                CssColor grayTransparentColor = CssColor.make("rgba(255,255,255,0.75)");

                // this translation is important for drawing lines with a real line width of 1 pixel
                context2d.setStrokeStyle(grayTransparentColor);
                context2d.setLineWidth(1.0);
                context2d.beginPath();
                context2d.arc(buoyZoneRadiusInPixel, buoyZoneRadiusInPixel, buoyZoneRadiusInPixel, 0, Math.PI*2, true); 
                context2d.closePath();
                context2d.stroke();
                
                Point buoyPositionInPx = getMap().convertLatLngToDivPixel(latLngPosition);
                getPane().setWidgetPosition(getCanvas(),
                        buoyPositionInPx.getX() - buoyZoneRadiusInPixel,
                        buoyPositionInPx.getY() - buoyZoneRadiusInPixel);
            } else {
                setCanvasSize(0, 0);
            }
        }
    }

    public BuoyDTO getBuoy() {
        return buoy;
    }

    public void setBuoy(BuoyDTO buoy) {
        this.buoy = buoy;
    }

    public double getBuoyZoneRadiusInMeter() {
        return buoyZoneRadiusInMeter;
    }

    public void setBuoyZoneRadiusInMeter(double buoyZoneRadiusInMeter) {
        this.buoyZoneRadiusInMeter = buoyZoneRadiusInMeter;
    }
}
