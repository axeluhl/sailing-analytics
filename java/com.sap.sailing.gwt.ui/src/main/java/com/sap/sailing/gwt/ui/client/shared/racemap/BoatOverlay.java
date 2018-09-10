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
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTOWithSpeedWindTackAndLegType;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.racemap.BoatClassVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlayV3;
import com.sap.sse.common.Color;
import com.sap.sse.common.Distance;
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
    private GPSFixDTOWithSpeedWindTackAndLegType boatFix;

    /** 
     * The rotation angle of the original boat image in degrees
     */
    private static double ORIGINAL_BOAT_IMAGE_ROTATIION_ANGLE = 90.0;

    private int canvasWidth;
    private int canvasHeight;

    private Color color; 

    private Map<Integer, Util.Pair<Size, Size>> boatScaleAndSizePerZoomCache; 

    private final BoatClassVectorGraphics boatVectorGraphics;

    private LegType lastLegType;
    private Tack lastTack;
    private Boolean lastSelected;
    private Integer lastWidth;
    private Integer lastHeight;
    private Size lastScale;
    private Color lastColor;
    
    public static enum DisplayMode { DEFAULT, SELECTED, NOT_SELECTED };
    private DisplayMode displayMode;

    /**
     * Remembers the old drawing angle as passed to {@link #setCanvasRotation(double)} to minimize rotation angle upon
     * the next update. The rotation property will always be animated according to the magnitude of the values. A
     * transition from 5 to 355 will go through 180 and not from 5 to 0==360 and back to 355! Therefore, with 5 being
     * the last rotation angle, the new rotation angle of 355 needs to be converted to -5 to ensure that the transition
     * goes through 0.<p>
     */
    private Double boatDrawingAngle;

    public BoatOverlay(final MapWidget map, int zIndex, final BoatDTO boatDTO, Color color, CoordinateSystem coordinateSystem) {
        super(map, zIndex, coordinateSystem);
        this.boatClass = boatDTO.getBoatClass();
        this.color = color;
        boatScaleAndSizePerZoomCache = new HashMap<Integer, Util.Pair<Size,Size>>();
        boatVectorGraphics = BoatClassVectorGraphicsResolver.resolveBoatClassVectorGraphics(boatClass.getName());
    }
    
    @Override
    protected void draw() {
        if (mapProjection != null && boatFix != null) {
            // the possible zoom level range is 0 to 21 (zoom level 0 would show the whole world)
            int zoom = map.getZoom();
            Util.Pair<Size, Size> boatScaleAndSize = boatScaleAndSizePerZoomCache.get(zoom);
            if (boatScaleAndSize == null) {
                boatScaleAndSize = getBoatScaleAndSize(boatClass);
                boatScaleAndSizePerZoomCache.put(zoom, boatScaleAndSize);
            }
            Size boatSizeScaleFactor = boatScaleAndSize.getA();
            canvasWidth = (int) (boatScaleAndSize.getB().getWidth());
            canvasHeight = (int) (boatScaleAndSize.getB().getHeight());
            if (lastWidth == null || canvasWidth != lastWidth || lastHeight == null || canvasHeight != lastHeight) {
                setCanvasSize(canvasWidth, canvasHeight);
            }
            if (needToDraw(boatFix.legType, boatFix.tack, isSelected(), canvasWidth, canvasHeight, boatSizeScaleFactor, color)) {
                boatVectorGraphics.drawBoatToCanvas(getCanvas().getContext2d(), boatFix.legType, boatFix.tack, getDisplayMode(), 
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
            updateBoatDrawingAngle(coordinateSystem.mapDegreeBearing(speedWithBearing.bearingInDegrees - ORIGINAL_BOAT_IMAGE_ROTATIION_ANGLE));
            setCanvasRotation(boatDrawingAngle);
        }
    }
    
    /**
     * Compares the drawing parameters to {@link #lastLegType} and the other <code>last...</code>. If anything has
     * changed, the result is <code>true</code>.
     */
    private boolean needToDraw(LegType legType, Tack tack, boolean isSelected, double width, double height,
            Size scaleFactor, Color color) {
        return lastLegType == null || lastLegType != legType || lastTack == null || lastTack != tack
                || lastSelected == null || lastSelected != isSelected || lastWidth == null || lastWidth != width
                || lastHeight == null || lastHeight != height || lastScale == null || !lastScale.equals(scaleFactor)
                || lastColor == null || !lastColor.equals(color);
    }

    /**
     * Updates {@link #boatDrawingAngle} so that the CSS transition from the old {@link #boatDrawingAngle} to
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

    public void setBoatFix(GPSFixDTOWithSpeedWindTackAndLegType boatFix, long timeForPositionTransitionMillis) {
        updateTransition(timeForPositionTransitionMillis);
        this.boatFix = boatFix;
    }

    public Util.Pair<Size, Size> getBoatScaleAndSize(BoatClassDTO boatClass) {
        Size boatSizeInPixel = getCorrelatedBoatSize(boatClass.getHullLength(), boatClass.getHullBeam());
        double boatHullScaleFactor = boatSizeInPixel.getWidth() / (boatVectorGraphics.getHullLengthInPx());
        double boatBeamScaleFactor = boatSizeInPixel.getHeight() / (boatVectorGraphics.getBeamInPx());
        // as the canvas contains the whole boat the canvas size relates to the overall length, not the hull length
        double scaledWidthSize = (boatVectorGraphics.getOverallLengthInPx()) * boatHullScaleFactor;
        double scaledBeamSize = (boatVectorGraphics.getOverallLengthInPx()) * boatBeamScaleFactor;
        return new Util.Pair<Size, Size>(Size.newInstance(boatHullScaleFactor, boatBeamScaleFactor),
                Size.newInstance(scaledWidthSize + scaledWidthSize / 2.0, scaledBeamSize + scaledBeamSize / 2.0));
    }

    private Size getCorrelatedBoatSize(Distance hullLength, Distance hullBeam) {
        Size boatSizeInPixel = calculateBoundingBox(mapProjection, boatFix.position, hullLength, hullBeam);
        changeBoatSizeIfTooShortHull(boatSizeInPixel, hullLength, hullBeam);
        changeBoatSizeIfTooNarrowBeam(boatSizeInPixel, hullLength, hullBeam);
        return boatSizeInPixel;
    }

    private void changeBoatSizeIfTooShortHull(Size boatSizeInPixel, Distance hullLength, Distance hullBeam) {
        // the minimum boat length is related to the hull of the boat, not the overall length
        double minBoatHullLengthInPx = boatVectorGraphics.getMinHullLengthInPx();
        if (boatSizeInPixel.getWidth() < minBoatHullLengthInPx) {
            double ratioBeanHullLength = hullBeam.divide(hullLength);
            boatSizeInPixel.setHeight(minBoatHullLengthInPx * ratioBeanHullLength);
            boatSizeInPixel.setWidth(minBoatHullLengthInPx);
        }
    }

    private void changeBoatSizeIfTooNarrowBeam(Size boatSizeInPixel, Distance hullLength, Distance hullBeam) {
        // if the boat gets too narrow, use the minimum beam and scale the hull length according to aspect
        double minBoatBeamLengthInPx = boatVectorGraphics.getMinBeamLengthInPx();
        if (boatSizeInPixel.getHeight() < minBoatBeamLengthInPx) {
            double ratioHullBeanLength = hullLength.divide(hullBeam);
            boatSizeInPixel.setWidth(minBoatBeamLengthInPx * ratioHullBeanLength);
            boatSizeInPixel.setHeight(minBoatBeamLengthInPx);
        }
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

}
