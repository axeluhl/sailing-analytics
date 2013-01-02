package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.shared.MarkDTO;
import com.sap.sailing.gwt.ui.shared.PositionDTO;

/**
 * A google map overlay based on a HTML5 canvas for drawing buoys (images) and the buoy zone
 */
public class BuoyOverlay extends CanvasOverlay {

    /**
     * The mark to draw
     */
    private MarkDTO mark;
    
    private PositionDTO position;
    
    private double buoyZoneRadiusInMeter;
    
    public BuoyOverlay(MarkDTO mark, double buoyZoneRadiusInMeter) {
        super();
        this.mark = mark;
        this.position = mark.position;
        this.buoyZoneRadiusInMeter = buoyZoneRadiusInMeter;
    }

    @Override
    protected Overlay copy() {
        return new BuoyOverlay(mark, buoyZoneRadiusInMeter);
    }

    @Override
    protected void redraw(boolean force) {
        if (mark != null && position != null) {
            LatLng latLngPosition = LatLng.newInstance(position.latDeg, position.lngDeg);
            int buoyZoneRadiusInPixel = calculateRadiusOfBoundingBox(latLngPosition, buoyZoneRadiusInMeter);
            if(buoyZoneRadiusInPixel > 10) {
                setCanvasSize(buoyZoneRadiusInPixel * 2, buoyZoneRadiusInPixel * 2);
                
                Context2d context2d = getCanvas().getContext2d();
                context2d.clearRect(0, 0, buoyZoneRadiusInPixel * 2, buoyZoneRadiusInPixel * 2);
                CssColor grayTransparentColor = CssColor.make("rgba(50,90,135,0.75)");

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

    public MarkDTO getBuoy() {
        return mark;
    }

    public void setBuoyPosition(PositionDTO position) {
        this.position = position;
    }

    public double getBuoyZoneRadiusInMeter() {
        return buoyZoneRadiusInMeter;
    }

    public void setBuoyZoneRadiusInMeter(double buoyZoneRadiusInMeter) {
        this.buoyZoneRadiusInMeter = buoyZoneRadiusInMeter;
    }
}
