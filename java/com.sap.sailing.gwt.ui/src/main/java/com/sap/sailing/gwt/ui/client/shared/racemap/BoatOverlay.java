package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.base.Size;
import com.sap.sailing.domain.common.LegType;
import com.sap.sailing.domain.common.Tack;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.racemap.BoatClassVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlayV3;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;

/**
 * A google map overlay based on a HTML5 canvas for drawing boats (images)
 * The boats will be zoomed/scaled according to the current map state and rotated according to the bearing of the boat.
 */
public class BoatOverlay extends CanvasOverlayV3 {

    /** 
     * The boat class
     */
    private final BoatClassDTO boatClass;
    
    /**
     * The current GPS fix used to draw the boat.
     */
    private GPSFixDTO boatFix;

    /** 
     * The rotation angle of the original boat image in degrees
     */
    private static double ORIGINAL_BOAT_IMAGE_ROTATIION_ANGLE = 90.0;

    private int canvasWidth;
    private int canvasHeight;

    private Color color; 

    private Map<Integer, Util.Pair<Double, Size>> boatScaleAndSizePerZoomCache; 

    private final BoatClassVectorGraphics boatVectorGraphics;

    private LegType lastLegType;
    private Tack lastTack;
    private Boolean lastSelected;
    private Integer lastWidth;
    private Integer lastHeight;
    private Double lastScale;
    private Color lastColor;

    /**
     * Remembers the old drawing angle as passed to {@link #setCanvasRotation(double)} to minimize rotation angle upon
     * the next update. The rotation property will always be animated according to the magnitude of the values. A
     * transition from 5 to 355 will go through 180 and not from 5 to 0==360 and back to 355! Therefore, with 5 being
     * the last rotation angle, the new rotation angle of 355 needs to be converted to -5 to ensure that the transition
     * goes through 0.<p>
     */
    private Double boatDrawingAngle;

    public BoatOverlay(final MapWidget map, int zIndex, final CompetitorDTO competitorDTO, Color color, CoordinateSystem coordinateSystem) {
        super(map, zIndex, coordinateSystem);
        this.boatClass = competitorDTO.getBoatClass();
        this.color = color;

        boatScaleAndSizePerZoomCache = new HashMap<Integer, Util.Pair<Double,Size>>();
        boatVectorGraphics = BoatClassVectorGraphicsResolver.resolveBoatClassVectorGraphics(boatClass.getName());
    }
    
    @Override
    protected void draw() {
        if (mapProjection != null && boatFix != null) {
            // the possible zoom level range is 0 to 21 (zoom level 0 would show the whole world)
            int zoom = map.getZoom();
            Util.Pair<Double, Size> boatScaleAndSize = boatScaleAndSizePerZoomCache.get(zoom);
            if (boatScaleAndSize == null) {
                boatScaleAndSize = getBoatScaleAndSize(boatClass);
                boatScaleAndSizePerZoomCache.put(zoom, boatScaleAndSize);
            }
            double boatSizeScaleFactor = boatScaleAndSize.getA();
            canvasWidth = (int) (boatScaleAndSize.getB().getWidth());
            canvasHeight = (int) (boatScaleAndSize.getB().getHeight());
            if (lastWidth == null || canvasWidth != lastWidth || lastHeight == null || canvasHeight != lastHeight) {
                setCanvasSize(canvasWidth, canvasHeight);
            }
            if (needToDraw(boatFix.legType, boatFix.tack, isSelected(), canvasWidth, canvasHeight, boatSizeScaleFactor, color)) {
                boatVectorGraphics.drawBoatToCanvas(getCanvas().getContext2d(), boatFix.legType, boatFix.tack, isSelected(), 
                        canvasWidth, canvasHeight, boatSizeScaleFactor, color);
                lastLegType = boatFix.legType;
                lastTack = boatFix.tack;
                lastSelected = isSelected();
                lastWidth = canvasWidth;
                lastHeight = canvasHeight;
                lastScale = boatSizeScaleFactor;
                lastColor = color;
            }
            LatLng latLngPosition = coordinateSystem.toLatLng(boatFix.position);
            Point boatPositionInPx = mapProjection.fromLatLngToDivPixel(latLngPosition);
            setCanvasPosition(boatPositionInPx.getX() - getCanvas().getCoordinateSpaceWidth() / 2,
                    boatPositionInPx.getY() - getCanvas().getCoordinateSpaceHeight() / 2);
            // now rotate the canvas accordingly
            SpeedWithBearingDTO speedWithBearing = boatFix.speedWithBearing;
            if (speedWithBearing == null) {
                speedWithBearing = new SpeedWithBearingDTO(0, 0);
            }
            updateBoatDrawingAngle(speedWithBearing.bearingInDegrees - ORIGINAL_BOAT_IMAGE_ROTATIION_ANGLE);
            setCanvasRotation(boatDrawingAngle);
        }
    }
    
    /**
     * Compares the drawing parameters to {@link #lastLegType} and the other <code>last...</code>. If anything has
     * changed, the result is <code>true</code>.
     */
    private boolean needToDraw(LegType legType, Tack tack, boolean isSelected, double width, double height,
            double scaleFactor, Color color) {
        return lastLegType == null || lastLegType != legType || lastTack == null || lastTack != tack
                || lastSelected == null || lastSelected != isSelected || lastWidth == null || lastWidth != width
                || lastHeight == null || lastHeight != height || lastScale == null || lastScale != scaleFactor
                || lastColor == null || !lastColor.equals(color);
    }

    /**
     * Updates {@link #boatDrawingAngle} to that the CSS transition from the old {@link #boatDrawingAngle} to
     * <code>newBoatDrawingAngle</code> is minimal.
     */
    private void updateBoatDrawingAngle(double newBoatDrawingAngle) {
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

    public Util.Pair<Double, Size> getBoatScaleAndSize(BoatClassDTO boatClass) {
        // the minimum boat length is related to the hull of the boat, not the overall length 
        double minBoatHullLengthInPx = boatVectorGraphics.getMinHullLengthInPx();
        double boatHullLengthInPixel = calculateDistanceAlongX(mapProjection,
                LatLng.newInstance(boatFix.position.getLatDeg(), boatFix.position.getLngDeg()), boatClass.getHullLengthInMeters());
        if (boatHullLengthInPixel < minBoatHullLengthInPx) {
            boatHullLengthInPixel = minBoatHullLengthInPx;
        }
        double boatSizeScaleFactor = boatHullLengthInPixel / (boatVectorGraphics.getHullLengthInPx());
        // as the canvas contains the whole boat the canvas size relates to the overall length, not the hull length 
        double scaledCanvasSize = (boatVectorGraphics.getOverallLengthInPx()) * boatSizeScaleFactor; 
        return new Util.Pair<Double, Size>(boatSizeScaleFactor, Size.newInstance(scaledCanvasSize + scaledCanvasSize / 2.0, scaledCanvasSize + scaledCanvasSize / 2.0));
    }
}
