package com.sap.sailing.gwt.ui.client.shared.racemap;

import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.base.LatLng;
import com.google.gwt.maps.client.base.Point;
import com.google.gwt.maps.client.base.Size;
import com.sap.sailing.domain.common.dto.BoatClassDTO;
import com.sap.sailing.domain.common.dto.CompetitorDTO;
import com.sap.sailing.gwt.ui.shared.SpeedWithBearingDTO;
import com.sap.sailing.gwt.ui.shared.racemap.RowingBoatClassVectorGraphics;
import com.sap.sse.common.Color;
import com.sap.sse.common.Util;

/**
 * A google map overlay based on a HTML5 canvas for drawing rowing boats (images)
 * The rowing boats will be zoomed/scaled according to the current map state and rotated according to the bearing of the rowing boat.
 */
public class RowingBoatOverlay extends BoatOverlay {

    private final RowingBoatClassVectorGraphics rowingBoatVectorGraphics;

    private Integer lastWidth;
    private Integer lastHeight;
    private boolean isPullingOars;

    public RowingBoatOverlay(final MapWidget map, int zIndex, final CompetitorDTO competitorDTO, Color color, CoordinateSystem coordinateSystem) {
        super(map, zIndex,competitorDTO, color, coordinateSystem);
        rowingBoatVectorGraphics = RowingBoatClassVectorGraphicsResolver.resolveRowingBoatClassVectorGraphics(boatClass.getName());
    }
    
    @Override
    public void draw() {
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
            isPullingOars = !isPullingOars;
            rowingBoatVectorGraphics.drawRowingBoatToCanvas(getCanvas().getContext2d(), isPullingOars, isSelected(), 
                    canvasWidth, canvasHeight, boatSizeScaleFactor, color);
            
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
        
    public Util.Pair<Double, Size> getBoatScaleAndSize(BoatClassDTO boatClass) {
        // the minimum boat length is related to the hull of the boat, not the overall length 
        double minBoatHullLengthInPx = rowingBoatVectorGraphics.getMinHullLengthInPx();
        double boatHullLengthInPixel = calculateDistanceAlongX(mapProjection, boatFix.position, boatClass.getHullLengthInMeters());
        if (boatHullLengthInPixel < minBoatHullLengthInPx) {
            boatHullLengthInPixel = minBoatHullLengthInPx;
        }
        double boatSizeScaleFactor = boatHullLengthInPixel / (rowingBoatVectorGraphics.getHullLengthInPx());
        // as the canvas contains the whole boat the canvas size relates to the overall length, not the hull length 
        double scaledCanvasSize = (rowingBoatVectorGraphics.getHullLengthInPx()) * boatSizeScaleFactor; 
        return new Util.Pair<Double, Size>(boatSizeScaleFactor, Size.newInstance(scaledCanvasSize + scaledCanvasSize / 2.0, scaledCanvasSize + scaledCanvasSize / 2.0));
    }
}
