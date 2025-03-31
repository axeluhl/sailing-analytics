package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.Size;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.BoatDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTOWithSpeedWindTackAndLegType;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlayV3;
import com.sap.sse.common.Color;
import com.sap.sse.common.Distance;
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
    protected GPSFixDTOWithSpeedWindTackAndLegType boatFix;

    /** 
     * The rotation angle of the original boat image in degrees
     */
    protected static double ORIGINAL_BOAT_IMAGE_ROTATIION_ANGLE = 90.0;

    protected int canvasWidth;
    protected int canvasHeight;

    protected Color color; 

    private Map<Long, Util.Pair<Size, Size>> boatScaleAndSizePerWorldWidthCache; 

    private Size lastScale;
    private DisplayMode lastDisplayMode;
    
    public static enum DisplayMode { DEFAULT, SELECTED, NOT_SELECTED };
    private DisplayMode displayMode;
    /**
     * Remembers the old drawing angle as passed to {@link #setCanvasRotation(double)} to minimize rotation angle upon
     * the next update. The rotation property will always be animated according to the magnitude of the values. A
     * transition from 5 to 355 will go through 180 and not from 5 to 0==360 and back to 355! Therefore, with 5 being
     * the last rotation angle, the new rotation angle of 355 needs to be converted to -5 to ensure that the transition
     * goes through 0.<p>
     */
    protected Double boatDrawingAngle;

    public BoatOverlay(final MapWidget map, int zIndex, final BoatDTO boatDTO, Color color, CoordinateSystem coordinateSystem) {
        super(map, zIndex, coordinateSystem);
        this.boatClass = boatDTO.getBoatClass();
        this.color = color;
        boatScaleAndSizePerWorldWidthCache = new HashMap<>();
    }
    
    protected abstract Util.Pair<Double, Size> getBoatScaleAndSize(BoatClassDTO boatClass);
            final long worldWidth = (long) mapProjection.getWorldWidth();
            final Util.Pair<Size, Size> boatScaleAndSize = boatScaleAndSizePerWorldWidthCache.computeIfAbsent(worldWidth, z->getBoatScaleAndSize(boatClass));
            final Size boatSizeScaleFactor = boatScaleAndSize.getA();
            if (needToDraw(boatFix.legType, boatFix.tack, isSelected(), canvasWidth, canvasHeight, boatSizeScaleFactor,
                    color, displayMode)) {
                boatVectorGraphics.drawBoatToCanvas(getCanvas().getContext2d(), boatFix.legType, boatFix.tack, getDisplayMode(), 
                lastDisplayMode = displayMode;
            final double trueHeadingInDegrees = boatFix.optionalTrueHeading != null
                    ? boatFix.optionalTrueHeading.getDegrees()
                    : (boatFix.speedWithBearing == null ? 0 : boatFix.speedWithBearing.bearingInDegrees);
            updateDrawingAngleAndSetCanvasRotation(coordinateSystem.mapDegreeBearing(trueHeadingInDegrees - ORIGINAL_BOAT_IMAGE_ROTATIION_ANGLE));
    
    protected abstract void draw();
            Size scaleFactor, Color color, DisplayMode displayMode) {
                || lastHeight == null || lastHeight != height || lastScale == null || !lastScale.equals(scaleFactor)
                || lastColor == null || !lastColor.equals(color) || lastDisplayMode == null
                || !lastDisplayMode.equals(displayMode);
    
    public void setBoatFix(GPSFixDTOWithSpeedWindTackAndLegType boatFix, long timeForPositionTransitionMillis) {
        updateTransition(timeForPositionTransitionMillis);
        this.boatFix = boatFix;
    }

    public Util.Pair<Size, Size> getBoatScaleAndSize(BoatClassDTO boatClass) {
        Size boatSizeInPixels = getCorrelatedBoatSize(boatClass.getHullLength(), boatClass.getHullBeam());
        double boatHullScaleFactor = boatSizeInPixels.getWidth() / (boatVectorGraphics.getHullLengthInPx());
        double boatBeamScaleFactor = boatSizeInPixels.getHeight() / (boatVectorGraphics.getBeamInPx());
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
    private void changeBoatSizeIfTooShortHull(Size boatSizeInPixel, Distance hullLength, Distance hullBeam) {
        // the minimum boat length is related to the hull of the boat, not the overall length
        if (boatSizeInPixel.getWidth() < minBoatHullLengthInPx) {
            double ratioBeamHullLength = hullBeam.divide(hullLength);
            boatSizeInPixel.setHeight(minBoatHullLengthInPx * ratioBeamHullLength);
            boatSizeInPixel.setWidth(minBoatHullLengthInPx);
    }

    private void changeBoatSizeIfTooNarrowBeam(Size boatSizeInPixel, Distance hullLength, Distance hullBeam) {
        // if the boat gets too narrow, use the minimum beam and scale the hull length according to aspect
        double minBoatBeamInPx = boatVectorGraphics.getMinBeamInPx();
        if (boatSizeInPixel.getHeight() < minBoatBeamInPx) {
            double ratioHullBeamLength = hullLength.divide(hullBeam);
            boatSizeInPixel.setWidth(minBoatBeamInPx * ratioHullBeamLength);
            boatSizeInPixel.setHeight(minBoatBeamInPx);
        }
    }

    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

}
