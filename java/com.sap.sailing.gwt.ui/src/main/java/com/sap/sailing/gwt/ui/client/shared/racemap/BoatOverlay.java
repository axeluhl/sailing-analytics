package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.Size;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlayV3;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;

/**
 * A google map overlay based on a HTML5 canvas for drawing boats (images)
 * The boats will be zoomed/scaled according to the current map state and rotated according to the bearing of the boat.
 */
public abstract class BoatOverlay extends CanvasOverlayV3 {

    /** 
     * The boat class
     */
    protected final BoatClassDTO boatClass;
    
    /**
     * The current GPS fix used to draw the boat.
     */
    protected GPSFixDTO boatFix;

    /** 
     * The rotation angle of the original boat image in degrees
     */
    protected static double ORIGINAL_BOAT_IMAGE_ROTATIION_ANGLE = 90.0;

    protected int canvasWidth;
    protected int canvasHeight;

    protected Color color; 

    protected Map<Integer, Util.Pair<Double, Size>> boatScaleAndSizePerZoomCache; 

    /**
     * Remembers the old drawing angle as passed to {@link #setCanvasRotation(double)} to minimize rotation angle upon
     * the next update. The rotation property will always be animated according to the magnitude of the values. A
     * transition from 5 to 355 will go through 180 and not from 5 to 0==360 and back to 355! Therefore, with 5 being
     * the last rotation angle, the new rotation angle of 355 needs to be converted to -5 to ensure that the transition
     * goes through 0.<p>
     */
    protected Double boatDrawingAngle;

    public BoatOverlay(final MapWidget map, int zIndex, final CompetitorDTO competitorDTO, Color color, CoordinateSystem coordinateSystem) {
        super(map, zIndex, coordinateSystem);
        this.boatClass = competitorDTO.getBoatClass();
        this.color = color;
        boatScaleAndSizePerZoomCache = new HashMap<Integer, Util.Pair<Double,Size>>();
    }
    
    protected abstract Util.Pair<Double, Size> getBoatScaleAndSize(BoatClassDTO boatClass);
    
    protected abstract void draw();
    
    /**
     * Updates {@link #boatDrawingAngle} so that the CSS transition from the old {@link #boatDrawingAngle} to
     * <code>newBoatDrawingAngle</code> is minimal.
     */
    protected void updateBoatDrawingAngle(double newBoatDrawingAngle) {
        if (boatDrawingAngle == null) {
            boatDrawingAngle = newBoatDrawingAngle;
        } else {
            double newMinusOld;
            while (Math.abs(newMinusOld = newBoatDrawingAngle - boatDrawingAngle) > 180) {
                newBoatDrawingAngle -= Math.signum(newMinusOld)*360;
            }
            boatDrawingAngle = boatDrawingAngle+newMinusOld;
        }
    }

    public void setBoatFix(GPSFixDTO boatFix, long timeForPositionTransitionMillis) {
        if (timeForPositionTransitionMillis == -1) {
            removeCanvasPositionAndRotationTransition();
        } else {
            setCanvasPositionAndRotationTransition(timeForPositionTransitionMillis);
        }
        this.boatFix = boatFix;
    }
}
