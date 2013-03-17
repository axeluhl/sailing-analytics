package com.sap.sailing.gwt.ui.shared.racemap;

import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.geom.LatLngBounds;
import com.google.gwt.maps.client.geom.Point;
import com.google.gwt.maps.client.geom.Size;
import com.google.gwt.maps.client.overlay.Overlay;
import com.sap.sailing.gwt.ui.shared.BoatClassDTO;
import com.sap.sailing.gwt.ui.shared.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;

/**
 * A google map overlay based on a HTML5 canvas for drawing boats (images)
 * The boats will be zoomed/scaled according to the current map state and rotated according to the bearing of the boat.
 */
public class BoatOverlay extends CanvasOverlay {

    /**
     * The competitor the boat belongs too.
     */
    private final CompetitorDTO competitorDTO;

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

    private final BoatClassImageData boatClassImageData;    

    public BoatOverlay(CompetitorDTO competitorDTO) {
        super();
        this.competitorDTO = competitorDTO;
        this.boatClass = competitorDTO.boatClass;
        this.boatClassImageData = BoatClassImageDataResolver.resolveBoatClassImages(boatClass.name);
    }

    @Override
    protected Overlay copy() {
        return new BoatOverlay(competitorDTO);
    }

    @Override
    protected void redraw(boolean force) {
        if (boatFix != null) {
            getCanvas().setTitle(getTitle());

            ImageTransformer boatImageTransformer;
            if (boatFix.legType != null) {
                boatImageTransformer = boatClassImageData.getBoatImageTransformerByLegTypeAndTack(boatFix.legType,
                        boatFix.tack, isSelected());
            } else {
                boatImageTransformer = boatClassImageData.getBoatImageTransformerByTack(boatFix.tack, isSelected());
            }
            double realBoatSizeScaleFactor = getRealBoatSizeScaleFactor(boatImageTransformer.getImageSize());
            double boatDrawingAngle = boatFix.speedWithBearing.bearingInDegrees - ORIGINAL_BOAT_IMAGE_ROTATIION_ANGLE;
            if (boatDrawingAngle < 0) {
                boatDrawingAngle += 360;
            }
            boatImageTransformer.drawToCanvas(getCanvas(), boatDrawingAngle, realBoatSizeScaleFactor);
            LatLng latLngPosition = LatLng.newInstance(boatFix.position.latDeg, boatFix.position.lngDeg);
            Point boatPositionInPx = getMap().convertLatLngToDivPixel(latLngPosition);
            getPane().setWidgetPosition(getCanvas(),
                    boatPositionInPx.getX() - getCanvas().getCoordinateSpaceWidth() / 2,
                    boatPositionInPx.getY() - getCanvas().getCoordinateSpaceHeight() / 2);
        }
    }

    private String getTitle() {
        return competitorDTO.sailID + ", " + competitorDTO.name;
    }
    
    public GPSFixDTO getBoatFix() {
        return boatFix;
    }

    public void setBoatFix(GPSFixDTO boatFix) {
        this.boatFix = boatFix;
    }
    
    public double getRealBoatSizeScaleFactor(Size imageSize) {
        // the possible zoom level range is 0 to 21 (zoom level 0 would show the whole world)
        int zoomLevel = map == null ? 1 : map.getZoomLevel();
        int boatLengthInPixel = boatClassImageData.getBoatClassImageLengthInPx();
        double minScaleFactor = 0.45;
        double maxScaleFactor = 2.0;
        if (boatLengthInPixel > 50 && boatLengthInPixel <= 100) {
            minScaleFactor = 0.40;
        } else if (boatLengthInPixel > 100) {
            minScaleFactor = 0.33;
        }

        double realBoatSizeScaleFactor = minScaleFactor;
        double hullLengthInMeters = boatClass.getHullLengthInMeters();
        // to scale the boats to a realistic size we need the length of the boat in pixel, 
        // but it does not work to just take the image size, because the images for the different boat states can be different
        if (zoomLevel > 5) {
            LatLngBounds bounds = map.getBounds();
            if (bounds != null) {
                LatLng upperRight = bounds.getNorthEast();
                LatLng bottomLeft = bounds.getSouthWest();
                LatLng upperLeft = LatLng.newInstance(upperRight.getLatitude(), bottomLeft.getLongitude());
                double distXInMeters = upperLeft.distanceFrom(upperRight);
                int widthInPixel = map.getSize().getWidth();
                double realBoatSizeInPixel  = (widthInPixel * hullLengthInMeters) / distXInMeters;
                realBoatSizeScaleFactor = realBoatSizeInPixel / (double) boatLengthInPixel;
                if (realBoatSizeScaleFactor < minScaleFactor) {
                    realBoatSizeScaleFactor = minScaleFactor;
                }
                if (realBoatSizeScaleFactor > maxScaleFactor) {
                    realBoatSizeScaleFactor = maxScaleFactor;
                }
            }
        }
        return realBoatSizeScaleFactor;
    }
}
