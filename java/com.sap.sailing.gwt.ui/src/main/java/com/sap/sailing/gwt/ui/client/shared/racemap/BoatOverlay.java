package com.sap.sailing.gwt.ui.client.shared.racemap;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.base.Size;
import com.sap.sailing.domain.common.Color;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.domain.common.impl.Util.Pair;
import com.sap.sailing.gwt.ui.shared.GPSFixDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.racemap.BoatClassVectorGraphics;
import com.sap.sailing.gwt.ui.shared.racemap.CanvasOverlayV3;

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

    private Map<Integer, Pair<Double, Size>> boatScaleAndSizePerZoomCache; 

    private final BoatClassVectorGraphics boatVectorGraphics;

    public BoatOverlay(final MapWidget map, int zIndex, final CompetitorDTO competitorDTO, Color color) {
        super(map, zIndex);
        this.boatClass = competitorDTO.getBoatClass();
        this.color = color;

        boatScaleAndSizePerZoomCache = new HashMap<Integer, Pair<Double,Size>>();
        boatVectorGraphics = BoatClassVectorGraphicsResolver.resolveBoatClassVectorGraphics(boatClass.getName());
    }
    
    @Override
    protected void draw() {
        if (mapProjection != null && boatFix != null) {
            // the possible zoom level range is 0 to 21 (zoom level 0 would show the whole world)
            int zoom = map.getZoom();
            Pair<Double, Size> boatScaleAndSize = boatScaleAndSizePerZoomCache.get(zoom);
            if (boatScaleAndSize == null) {
                boatScaleAndSize = getBoatScaleAndSize(boatClass);
                boatScaleAndSizePerZoomCache.put(zoom, boatScaleAndSize);
            }
            double boatSizeScaleFactor = boatScaleAndSize.getA();
            SpeedWithBearingDTO speedWithBearing = boatFix.speedWithBearing;
            if (speedWithBearing == null) {
                speedWithBearing = new SpeedWithBearingDTO(0, 0);
            }
            double boatDrawingAngle = speedWithBearing.bearingInDegrees - ORIGINAL_BOAT_IMAGE_ROTATIION_ANGLE;
            if (boatDrawingAngle < 0) {
                boatDrawingAngle += 360;
            }
            canvasWidth = (int) (boatScaleAndSize.getB().getWidth());
            canvasHeight = (int) (boatScaleAndSize.getB().getHeight());
            setCanvasSize(canvasWidth, canvasHeight);
            boatVectorGraphics.drawBoatToCanvas(getCanvas().getContext2d(), boatFix.legType, boatFix.tack, isSelected(), 
                    canvasWidth, canvasHeight, boatDrawingAngle, boatSizeScaleFactor, color.getAsHtml());
            LatLng latLngPosition = LatLng.newInstance(boatFix.position.latDeg, boatFix.position.lngDeg);
            Point boatPositionInPx = mapProjection.fromLatLngToDivPixel(latLngPosition);
            setCanvasPosition(boatPositionInPx.getX() - getCanvas().getCoordinateSpaceWidth() / 2,
                    boatPositionInPx.getY() - getCanvas().getCoordinateSpaceHeight() / 2);
        }
    }
    
    public void setBoatFix(GPSFixDTO boatFix, long timeForPositionTransitionMillis) {
        if (timeForPositionTransitionMillis == -1) {
            removeCanvasPositionTransition();
        } else {
            setCanvasPositionTransition(timeForPositionTransitionMillis);
        }
        this.boatFix = boatFix;
    }

    public Pair<Double, Size> getBoatScaleAndSize(BoatClassDTO boatClass) {
        // the minimum boat length is related to the hull of the boat, not the overall length 
        double minBoatHullLengthInPx = 25;

        Size boatSizeHullInPixel = calculateBoundingBox(mapProjection,
                LatLng.newInstance(boatFix.position.latDeg, boatFix.position.lngDeg),
                boatVectorGraphics.getBoatHullLengthInMeters(), boatVectorGraphics.getBoatBeamInMeters());
        
        double boatHullLengthInPixel = boatSizeHullInPixel.getWidth();
        if(boatHullLengthInPixel < minBoatHullLengthInPx) {
            boatHullLengthInPixel = minBoatHullLengthInPx;
        }

        // The coordinates of the canvas drawing methods are based on the 'centimeter' unit (1px = 1cm).
        // To calculate the display real boat size the scale factor from canvas units to the real   
        double boatSizeScaleFactor = boatHullLengthInPixel / (boatVectorGraphics.getBoatHullLengthInMeters() * 100);

        // as the canvas contains the whole boat the canvas size relates to the overall length, not the hull length 
        double scaledCanvasSize = (boatVectorGraphics.getBoatOverallLengthInMeters() * 100) * boatSizeScaleFactor; 

        return new Pair<Double, Size>(boatSizeScaleFactor, Size.newInstance(scaledCanvasSize + scaledCanvasSize / 2.0, scaledCanvasSize + scaledCanvasSize / 2.0));
    }
}
